package com.swapu.service;

import com.swapu.entity.SysNotification;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysNotificationService extends IService<SysNotification> {
    /**
     * 发送通知
     * @param userId 接收用户ID
     * @param type 消息类型: 1-系统通知(审核等), 2-交易物流, 3-互动消息
     * @param title 标题
     * @param content 内容
     * @param relatedId 关联ID
     */
    void send(Long userId, Integer type, String title, String content, Long relatedId);
}
