package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.common.Result;
import com.swapu.entity.ChatMessage;
import com.swapu.entity.User;
import com.swapu.mapper.ChatMessageMapper;
import com.swapu.mapper.UserMapper;
import com.swapu.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<List<ChatMessage>> getHistory(Long userId, Long contactId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.and(i -> i.eq("sender_id", userId).eq("receiver_id", contactId))
               .or(i -> i.eq("sender_id", contactId).eq("receiver_id", userId));
        wrapper.orderByAsc("created_at"); // 历史记录正序
        
        return Result.success(list(wrapper));
    }

    @Override
    public Result<?> markAsRead(Long userId, Long contactId) {
        UpdateWrapper<ChatMessage> wrapper = new UpdateWrapper<>();
        wrapper.eq("receiver_id", userId).eq("sender_id", contactId).eq("is_read", 0);
        wrapper.set("is_read", 1);
        update(wrapper);
        return Result.success();
    }

    @Override
    public Result<List<Map<String, Object>>> getContactList(Long userId) {
        // 利用自定义SQL获取最近聊天的联系人列表
        List<Map<String, Object>> recentUsers = baseMapper.getRecentChatUsers(userId);
        
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Long, Map<String, Object>> userMap = new HashMap<>();

        for (Map<String, Object> map : recentUsers) {
            Long contactId = ((Number) map.get("sender_id")).longValue();
            if (userMap.containsKey(contactId)) {
                // 如果已经存在（由于UNION去重问题，可能存在同一个用户多条记录），合并unread
                Map<String, Object> existing = userMap.get(contactId);
                
                int unreadExisting = existing.get("unread_count") != null ? ((Number) existing.get("unread_count")).intValue() : 0;
                int unreadNew = map.get("unread_count") != null ? ((Number) map.get("unread_count")).intValue() : 0;
                
                existing.put("unread_count", unreadExisting + unreadNew);
                continue;
            }

            User user = userMapper.selectById(contactId);
            if (user != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("contactId", contactId);
                item.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
                item.put("avatar", user.getAvatar());
                item.put("lastTime", map.get("last_time"));
                
                int unreadCount = map.get("unread_count") != null ? ((Number) map.get("unread_count")).intValue() : 0;
                item.put("unreadCount", unreadCount);
                
                result.add(item);
                userMap.put(contactId, item);
            }
        }
        
        return Result.success(result);
    }

    @Override
    public Result<Long> getUnreadTotalCount(Long userId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId).eq("is_read", 0);
        return Result.success(count(wrapper));
    }
}