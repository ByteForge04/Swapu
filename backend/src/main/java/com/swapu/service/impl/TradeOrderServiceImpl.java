package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.entity.TradeOrder;
import com.swapu.entity.User;
import com.swapu.mapper.ItemMapper;
import com.swapu.mapper.TradeOrderMapper;
import com.swapu.mapper.UserMapper;
import com.swapu.service.TradeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.transaction.support.TransactionTemplate;

import com.swapu.entity.mq.OrderTimeoutMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {

    private static final Logger log = LoggerFactory.getLogger(TradeOrderServiceImpl.class);

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.swapu.service.SysNotificationService notificationService;

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public Result<?> createOrder(TradeOrder order) {
        // 使用分布式锁防止超卖 (key: item:lock:{itemId})
        String lockKey = "item:lock:" + order.getItemId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，等待3秒，锁定10秒
            boolean isLocked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (isLocked) {
                try {
                    return transactionTemplate.execute(status -> {
                        // 1. 检查物品状态
                        Item item = itemMapper.selectById(order.getItemId());
                        if (item == null) {
                            return Result.error("物品不存在");
                        }
                        if (item.getStatus() != 1) {
                            return Result.error("物品当前不可购买");
                        }
                        if (item.getUserId().equals(order.getBuyerId())) {
                            return Result.error("不能购买自己发布的物品");
                        }

                        // 2. 校验订单金额
                        if (order.getAmount() != null && order.getAmount().compareTo(item.getPrice()) != 0) {
                            return Result.error("订单金额与商品价格不一致");
                        }

                        // 3. 填充订单信息
                        order.setSellerId(item.getUserId());
                        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
                        order.setStatus(0); // 待卖家确认
                        if (order.getAmount() == null) {
                            order.setAmount(item.getPrice()); // 默认按物品价格
                        }
                        if (order.getPaymentMethod() == null) {
                            order.setPaymentMethod(1); // 默认线上支付
                        }
                        order.setCreatedAt(LocalDateTime.now());
                        order.setUpdatedAt(LocalDateTime.now());

                        // 3. 保存订单
                        save(order);

                        // 4. 更新物品状态为 "交易中" (2)
                        item.setStatus(2);
                        itemMapper.updateById(item);
                        
                        // 5. 通知卖家
                        notificationService.send(item.getUserId(), 2, "新订单提醒", 
                            "您发布的 [" + item.getTitle() + "] 有买家下单了，请及时确认。", order.getOrderId());

                        // 6. 发送延迟消息到 RocketMQ，实现 30 分钟未支付自动取消订单 (仅线上支付)
                        if (order.getPaymentMethod() == 1 && rocketMQTemplate != null) {
                            try {
                                OrderTimeoutMessage msg = new OrderTimeoutMessage(order.getOrderId());
                                org.apache.rocketmq.common.message.Message mqMsg = new org.apache.rocketmq.common.message.Message(
                                        "order-timeout-topic",
                                        com.alibaba.fastjson.JSON.toJSONBytes(msg)
                                );
                                mqMsg.setDelayTimeLevel(16); // 30 minutes
                                rocketMQTemplate.getProducer().send(mqMsg, 3000);
                            } catch (Exception e) {
                                    log.warn("MQ Send Warning (order timeout): Topic 'order-timeout-topic' may not exist. Order timeout will rely on fallback task. orderId: {}", order.getOrderId());
                                }
                        }

                        return Result.success(order.getOrderId());
                    });
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            } else {
                return Result.error("当前下单人数过多，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.error("系统繁忙，请重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> confirmOrder(Long userId, Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getSellerId().equals(userId)) {
            return Result.error("无权操作");
        }
        if (order.getStatus() != 0) {
            return Result.error("订单状态不正确");
        }

        if (order.getPaymentMethod() != null && order.getPaymentMethod() == 2) {
            // 线下交易，卖家确认接单 -> 进行中
            order.setStatus(1); // 进行中
            order.setPaymentStatus(1); // 标记线下交易资金状态为已确认（符合统一进行中状态模型）
            order.setUpdatedAt(LocalDateTime.now());
            updateById(order);

            // 通知买家
            Item item = itemMapper.selectById(order.getItemId());
            notificationService.send(order.getBuyerId(), 2, "订单已确认", 
                "卖家已确认您的订单 [" + (item != null ? item.getTitle() : "未知物品") + "]，请联系卖家完成线下交易。", orderId);
        } else {
            // 线上交易，必须由买家先支付，然后买家确认收货才能完成
            return Result.error("线上交易需等待买家付款，支付成功后将自动流转为进行中");
        }

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> cancelOrder(Long userId, Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        // 买家和卖家都可以取消
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return Result.error("无权操作");
        }
        // 只能取消 "待确认" 或 "进行中" 的订单
        if (order.getStatus() > 1) {
            return Result.error("订单已完成或已取消，无法再次取消");
        }

        order.setStatus(3); // 已取消
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);

        // 恢复物品状态为 "在售" (1)
        Item item = itemMapper.selectById(order.getItemId());
        if (item != null) {
            item.setStatus(1);
            itemMapper.updateById(item);
        }
        
        // 通知对方
        Long targetUserId = userId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId();
        String operator = userId.equals(order.getBuyerId()) ? "买家" : "卖家";
        notificationService.send(targetUserId, 2, "订单取消", 
            operator + "取消了订单 [" + (item != null ? item.getTitle() : "未知物品") + "]。", orderId);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> completeOrder(Long userId, Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        // 通常由买家确认收货，但这里简化，双方都可点击完成? 
        // 需求文档: "双方确认收货/交付" -> 这里暂定买家确认收货
        if (!order.getBuyerId().equals(userId)) {
            return Result.error("请由买家确认收货");
        }
        if (order.getStatus() != 1) {
            return Result.error("订单状态不正确");
        }

        order.setStatus(2); // 已完成
        order.setUpdatedAt(LocalDateTime.now());
        // compatible method
        order.setCompletedAt(LocalDateTime.now()); 
        updateById(order);

        // 更新物品状态为 "已售出" (3)
        Item item = itemMapper.selectById(order.getItemId());
        if (item != null) {
            item.setStatus(3);
            itemMapper.updateById(item);
        }
        
        // 通知卖家
        notificationService.send(order.getSellerId(), 2, "交易完成", 
            "买家已确认收货，订单 [" + (item != null ? item.getTitle() : "未知物品") + "] 已完成。", orderId);
        
        // TODO: 增加卖家信用分?

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo, String tradeNo, java.time.LocalDateTime payTime) {
        QueryWrapper<TradeOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        TradeOrder order = getOne(queryWrapper);
        
        if (order == null) {
            return;
        }
        
        // 幂等性检查：如果已经支付，直接返回
        if (order.getPaymentStatus() != null && order.getPaymentStatus() == 1) {
            return;
        }
        
        order.setPaymentStatus(1);
        order.setTradeNo(tradeNo);
        order.setPayTime(payTime);
        order.setStatus(1); // 支付成功后，状态变为 "进行中/待交付" (或者可以新增状态 "已支付待发货")
        order.setUpdatedAt(java.time.LocalDateTime.now());
        
        updateById(order);
        
        // 通知卖家
        Item item = itemMapper.selectById(order.getItemId());
        notificationService.send(order.getSellerId(), 2, "订单已支付", 
            "买家已完成支付，订单 [" + (item != null ? item.getTitle() : "未知物品") + "]，请尽快联系买家交付。", order.getOrderId());
    }

    @Override
    public Result<List<TradeOrder>> listMyOrders(Long userId, Integer type) {
        QueryWrapper<TradeOrder> queryWrapper = new QueryWrapper<>();
        
        if (type == 1) {
            // 我买到的
            queryWrapper.eq("buyer_id", userId);
        } else if (type == 2) {
            // 我卖出的
            queryWrapper.eq("seller_id", userId);
        } else {
            return Result.error("参数错误");
        }
        
        queryWrapper.orderByDesc("created_at");
        List<TradeOrder> list = list(queryWrapper);
        
        if (list == null || list.isEmpty()) {
            return Result.success(list);
        }

        // N+1 查询优化：批量查询物品和用户信息
        java.util.Set<Long> itemIds = list.stream().map(TradeOrder::getItemId).collect(java.util.stream.Collectors.toSet());
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        list.forEach(order -> {
            userIds.add(order.getBuyerId());
            userIds.add(order.getSellerId());
        });

        Map<Long, Item> itemMap = itemIds.isEmpty() ? new HashMap<>() : 
            itemMapper.selectBatchIds(itemIds).stream().collect(java.util.stream.Collectors.toMap(Item::getItemId, item -> item));
        
        Map<Long, User> userMap = userIds.isEmpty() ? new HashMap<>() : 
            userMapper.selectBatchIds(userIds).stream().peek(user -> user.setPassword(null))
            .collect(java.util.stream.Collectors.toMap(User::getUserId, user -> user));

        // 填充关联信息
        for (TradeOrder order : list) {
            order.setItem(itemMap.get(order.getItemId()));
            order.setBuyer(userMap.get(order.getBuyerId()));
            order.setSeller(userMap.get(order.getSellerId()));
        }
        
        return Result.success(list);
    }

    @Override
    public Result<TradeOrder> getOrderDetail(Long userId, Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return Result.error("无权查看此订单");
        }
        
        fillOrderInfo(order);
        return Result.success(order);
    }
    
    private void fillOrderInfo(TradeOrder order) {
        // 填充物品信息
        Item item = itemMapper.selectById(order.getItemId());
        order.setItem(item);
        
        // 填充买家信息
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            buyer.setPassword(null);
            order.setBuyer(buyer);
        }
        
        // 填充卖家信息
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            seller.setPassword(null);
            order.setSeller(seller);
        }
    }

    @Override
    public List<Map<String, Object>> getOrderStatistics(int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        // 统计最近 days 天（包含今天）
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            
            QueryWrapper<TradeOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.ge("created_at", start);
            queryWrapper.lt("created_at", end);
            long count = count(queryWrapper);
            
            Map<String, Object> map = new HashMap<>();
            map.put("date", date.toString());
            map.put("count", count);
            result.add(map);
        }
        return result;
    }
}
