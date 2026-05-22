package com.swapu.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapu.common.Result;
import com.swapu.entity.SysNotification;
import com.swapu.service.SysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/notification")
public class SysNotificationController {

    @Autowired
    private SysNotificationService notificationService;

    // 获取当前用户的通知列表
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        Page<SysNotification> pageParam = new Page<>(page, size);
        QueryWrapper<SysNotification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                    .orderByDesc("created_at");
        
        return Result.success(notificationService.page(pageParam, queryWrapper));
    }

    // 获取未读消息数量
    @GetMapping("/unread-count")
    public Result<?> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.success(0);
        }
        
        QueryWrapper<SysNotification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                    .eq("is_read", 0);
        
        return Result.success(notificationService.count(queryWrapper));
    }

    // 标记为已读
    @PutMapping("/read/{id}")
    public Result<?> read(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        SysNotification notification = notificationService.getById(id);
        
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setIsRead(1);
            notificationService.updateById(notification);
            return Result.success();
        }
        return Result.error("操作失败");
    }

    // 全部标记为已读
    @PutMapping("/read-all")
    public Result<?> readAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        
        SysNotification updateEntity = new SysNotification();
        updateEntity.setIsRead(1);
        
        QueryWrapper<SysNotification> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("user_id", userId).eq("is_read", 0);
        
        notificationService.update(updateEntity, updateWrapper);
        return Result.success();
    }
}
