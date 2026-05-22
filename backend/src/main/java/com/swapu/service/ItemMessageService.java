package com.swapu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.common.Result;
import com.swapu.entity.ItemMessage;

import java.util.List;

public interface ItemMessageService extends IService<ItemMessage> {
    
    Result<?> addMessage(ItemMessage message);

    Result<List<ItemMessage>> getMessagesByItem(Long itemId);

    Result<?> deleteMessage(Long messageId, Long userId);
}