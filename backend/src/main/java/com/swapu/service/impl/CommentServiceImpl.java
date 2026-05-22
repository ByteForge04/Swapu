package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.common.Result;
import com.swapu.entity.Comment;
import com.swapu.entity.TradeOrder;
import com.swapu.mapper.CommentMapper;
import com.swapu.mapper.TradeOrderMapper;
import com.swapu.service.CommentService;
import com.swapu.entity.User;
import com.swapu.mapper.UserMapper;
import com.swapu.entity.Item;
import com.swapu.mapper.ItemMapper;
import com.swapu.utils.DeepSeekUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private TradeOrderMapper tradeOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private DeepSeekUtils deepSeekUtils;

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Result<?> createComment(Comment comment) {
        // 1. 检查订单状态
        TradeOrder order = tradeOrderMapper.selectById(comment.getOrderId());
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 2) { // 必须是已完成的订单
            return Result.error("订单未完成，无法评价");
        }
        
        // 2. 检查是否已评价
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", comment.getOrderId());
        queryWrapper.eq("user_id", comment.getUserId());
        if (count(queryWrapper) > 0) {
            return Result.error("您已评价过此订单");
        }

        // --- 新增: AI 审核评价内容 ---
        if (comment.getContent() != null && !comment.getContent().isEmpty()) {
            try {
                log.info("Start AI check for comment. OrderId: {}, Content: {}", comment.getOrderId(), comment.getContent());
                String aiResultStr = deepSeekUtils.checkComment(comment.getContent());
                if (aiResultStr.startsWith("{")) {
                    JSONObject aiResult = JSON.parseObject(aiResultStr);
                    if (aiResult != null && !aiResult.getBooleanValue("allow")) {
                        log.warn("Comment rejected by AI. OrderId: {}, Reason: {}", comment.getOrderId(), aiResult.getString("reason"));
                        return Result.error("评价涉嫌违规被 AI 拦截: " + aiResult.getString("reason"));
                    }
                    log.info("Comment approved by AI. OrderId: {}", comment.getOrderId());
                } else {
                    log.warn("AI returned non-JSON format for comment check: {}", aiResultStr);
                }
            } catch (Exception e) {
                log.error("AI check for comment failed: {}", e.getMessage(), e);
                // 审核出错不阻断，直接放行
            }
        }

        // 3. 填充信息
        comment.setItemId(order.getItemId());
        // 如果评价人是买家，则被评价人是卖家；反之亦然
        if (comment.getUserId().equals(order.getBuyerId())) {
            comment.setTargetUserId(order.getSellerId());
        } else if (comment.getUserId().equals(order.getSellerId())) {
            comment.setTargetUserId(order.getBuyerId());
        } else {
            return Result.error("您不是此订单的参与者");
        }
        
        comment.setCreatedAt(LocalDateTime.now());
        save(comment);

        // 更新被评价人的信用分
        User targetUser = userMapper.selectById(comment.getTargetUserId());
        if (targetUser != null) {
            int score = targetUser.getCreditScore();
            if (comment.getRating() >= 4) {
                score += 1; // 好评加分
            } else if (comment.getRating() <= 2) {
                score -= 1; // 差评扣分
            }
            targetUser.setCreditScore(score);
            userMapper.updateById(targetUser);
        }
        
        return Result.success();
    }

    @Override
    public Result<Comment> getCommentByOrder(Long orderId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        // 这里可能返回多条（买家评价和卖家评价），实际业务看需求
        // 简单起见，返回第一条
        return Result.success(getOne(queryWrapper, false));
    }

    @Override
    public Result<List<Comment>> getCommentsByItem(Long itemId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("item_id", itemId);
        queryWrapper.orderByDesc("created_at");
        return Result.success(list(queryWrapper));
    }
    
    @Override
    public Result<List<Comment>> getCommentsByUser(Long userId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_user_id", userId);
        queryWrapper.orderByDesc("created_at");
        List<Comment> list = list(queryWrapper);
        
        for (Comment comment : list) {
            // 填充物品信息
            Item item = itemMapper.selectById(comment.getItemId());
            comment.setItem(item);
            
            // 填充评价人信息
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                user.setPassword(null);
                comment.setUser(user);
            }
        }
        
        return Result.success(list);
    }
}
