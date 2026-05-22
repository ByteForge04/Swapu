package com.swapu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.common.Result;
import com.swapu.entity.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatMessageService extends IService<ChatMessage> {
    
    Result<List<ChatMessage>> getHistory(Long userId, Long contactId);

    Result<?> markAsRead(Long userId, Long contactId);

    Result<List<Map<String, Object>>> getContactList(Long userId);

    Result<Long> getUnreadTotalCount(Long userId);
}