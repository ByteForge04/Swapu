package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.entity.ItemWant;
import com.swapu.mapper.ItemMapper;
import com.swapu.mapper.ItemWantMapper;
import com.swapu.service.ItemWantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemWantServiceImpl extends ServiceImpl<ItemWantMapper, ItemWant> implements ItemWantService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private com.swapu.service.SysNotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @org.springframework.cache.annotation.CacheEvict(value = "item", key = "#itemId")
    public Result<?> toggleWant(Long userId, Long itemId) {
        // 检查是否已经收藏
        QueryWrapper<ItemWant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("item_id", itemId);
        ItemWant existingWant = getOne(queryWrapper);

        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }

        if (existingWant != null) {
            // 已收藏，则取消收藏
            removeById(existingWant.getWantId());
            // 更新物品想要数
            Integer wantCount = item.getWantCount();
            item.setWantCount(wantCount == null ? 0 : Math.max(0, wantCount - 1));
            itemMapper.updateById(item);
            return Result.success("已取消收藏");
        } else {
            // 未收藏，则添加收藏
            ItemWant newWant = new ItemWant();
            newWant.setUserId(userId);
            newWant.setItemId(itemId);
            save(newWant);
            // 更新物品想要数
            Integer wantCount = item.getWantCount();
            item.setWantCount(wantCount == null ? 1 : wantCount + 1);
            itemMapper.updateById(item);
            
            // 发送通知给卖家
            if (!item.getUserId().equals(userId)) { // 自己想要自己的物品不发通知
                notificationService.send(item.getUserId(), 3, "收到新意向", 
                    "有用户想要您的物品 [" + item.getTitle() + "]，快去看看吧！", itemId);
            }
            
            return Result.success("收藏成功");
        }
    }

    @Override
    public Result<Boolean> checkWant(Long userId, Long itemId) {
        QueryWrapper<ItemWant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("item_id", itemId);
        long count = count(queryWrapper);
        return Result.success(count > 0);
    }
}
