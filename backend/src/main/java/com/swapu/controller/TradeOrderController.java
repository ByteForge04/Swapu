package com.swapu.controller;

import com.swapu.common.Result;
import com.swapu.entity.TradeOrder;
import com.swapu.service.TradeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/order")
public class TradeOrderController {

    @Autowired
    private TradeOrderService orderService;

    @Autowired
    private RedissonClient redissonClient;

    @PostMapping("/create")
    public Result<?> create(@RequestBody @Validated TradeOrder order, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        
        // 用户级别下单限流：每分钟最多 3 次请求，防止恶意刷单
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("order:rate:" + userId);
        rateLimiter.trySetRate(RateType.OVERALL, 3, 1, RateIntervalUnit.MINUTES);
        
        if (!rateLimiter.tryAcquire()) {
            return Result.error("下单过于频繁，请稍后再试");
        }

        order.setBuyerId(userId);
        return orderService.createOrder(order);
    }

    @PostMapping("/confirm/{orderId}")
    public Result<?> confirm(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.confirmOrder(userId, orderId);
    }

    @PostMapping("/cancel/{orderId}")
    public Result<?> cancel(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.cancelOrder(userId, orderId);
    }

    @PostMapping("/complete/{orderId}")
    public Result<?> complete(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.completeOrder(userId, orderId);
    }

    @GetMapping("/list/{type}")
    public Result<List<TradeOrder>> list(@PathVariable Integer type, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.listMyOrders(userId, type);
    }

    @GetMapping("/detail/{orderId}")
    public Result<TradeOrder> detail(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return orderService.getOrderDetail(userId, orderId);
    }
}
