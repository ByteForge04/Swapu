package com.swapu.service.impl;

import com.swapu.entity.SysNotification;
import com.swapu.mapper.SysNotificationMapper;
import com.swapu.service.SysNotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysNotificationServiceImpl extends ServiceImpl<SysNotificationMapper, SysNotification> implements SysNotificationService {

    @Override
    @Async // 异步发送通知，避免阻塞主流程
    public void send(Long userId, Integer type, String title, String content, Long relatedId) {
        SysNotification notification = new SysNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreatedAt(LocalDateTime.now());
        this.save(notification);
    }
}
