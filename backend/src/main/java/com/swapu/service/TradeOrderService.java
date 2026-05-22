package com.swapu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.common.Result;
import com.swapu.entity.TradeOrder;

import java.util.List;

public interface TradeOrderService extends IService<TradeOrder> {
    /**
     * 创建订单 (买家下单)
     */
    Result<?> createOrder(TradeOrder order);

    /**
     * 确认订单 (卖家确认)
     */
    Result<?> confirmOrder(Long userId, Long orderId);

    /**
     * 取消订单 (买家或卖家)
     */
    Result<?> cancelOrder(Long userId, Long orderId);

    /**
     * 完成订单 (确认收货)
     */
    Result<?> completeOrder(Long userId, Long orderId);

    /**
     * 支付成功回调处理
     * @param orderNo 订单号
     * @param tradeNo 支付宝交易号
     * @param payTime 支付时间
     */
    void paySuccess(String orderNo, String tradeNo, java.time.LocalDateTime payTime);

    /**
     * 查询我的订单列表
     * @param type 1-我买到的, 2-我卖出的
     */
    Result<List<TradeOrder>> listMyOrders(Long userId, Integer type);

    /**
     * 获取订单详情
     */
    Result<TradeOrder> getOrderDetail(Long userId, Long orderId);

    /**
     * 获取最近几天的订单统计数据
     */
    List<java.util.Map<String, Object>> getOrderStatistics(int days);
}
