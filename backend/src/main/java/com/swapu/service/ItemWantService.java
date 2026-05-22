package com.swapu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.entity.ItemWant;
import com.swapu.common.Result;

public interface ItemWantService extends IService<ItemWant> {
    Result<?> toggleWant(Long userId, Long itemId);
    Result<Boolean> checkWant(Long userId, Long itemId);
}
