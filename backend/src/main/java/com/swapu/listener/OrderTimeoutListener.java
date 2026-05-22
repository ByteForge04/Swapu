package com.swapu.listener;

import com.swapu.common.Result;
import com.swapu.entity.TradeOrder;
import com.swapu.entity.mq.OrderTimeoutMessage;
import com.swapu.service.TradeOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "order-timeout-topic", consumerGroup = "order-timeout-consumer-group")
public class OrderTimeoutListener implements RocketMQListener<OrderTimeoutMessage> {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutListener.class);

    @Autowired
    private TradeOrderService tradeOrderService;

    @Override
    public void onMessage(OrderTimeoutMessage message) {
        if (message == null || message.getOrderId() == null) {
            return;
        }

        Long orderId = message.getOrderId();
        try {
            // 检查订单是否仍未支付，如果未支付则自动取消
            TradeOrder order = tradeOrderService.getById(orderId);
            if (order != null && order.getStatus() == 0 && (order.getPaymentStatus() == null || order.getPaymentStatus() == 0)) {
                log.info("订单超时未支付，执行自动取消: {}", orderId);
                // 模拟系统自动取消 (传入 buyerId 作为操作人)
                tradeOrderService.cancelOrder(order.getBuyerId(), orderId);
            }
        } catch (Exception e) {
            log.error("订单超时处理失败，orderId: {}", orderId, e);
        }
    }
}