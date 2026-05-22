package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.entity.ItemMessage;
import com.swapu.entity.User;
import com.swapu.mapper.ItemMapper;
import com.swapu.mapper.ItemMessageMapper;
import com.swapu.mapper.UserMapper;
import com.swapu.service.ItemMessageService;
import com.swapu.service.SysNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ItemMessageServiceImpl extends ServiceImpl<ItemMessageMapper, ItemMessage> implements ItemMessageService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysNotificationService notificationService;

    @Override
    public Result<?> addMessage(ItemMessage message) {
        Item item = itemMapper.selectById(message.getItemId());
        if (item == null) {
            return Result.error("物品不存在");
        }

        message.setCreatedAt(LocalDateTime.now());
        save(message);

        // 发送通知
        Long notifyTargetId = null;
        String notifyTitle = "";
        String notifyContent = "";

        if (message.getParentId() != null && message.getReplyToUserId() != null) {
            // 这是回复他人的留言
            notifyTargetId = message.getReplyToUserId();
            notifyTitle = "收到留言回复";
            notifyContent = "有人在物品 [" + item.getTitle() + "] 下回复了您的留言。";
        } else {
            // 这是对物品的直接留言
            if (!item.getUserId().equals(message.getUserId())) {
                notifyTargetId = item.getUserId();
                notifyTitle = "收到新留言";
                notifyContent = "您的物品 [" + item.getTitle() + "] 收到了新留言。";
            }
        }

        if (notifyTargetId != null && !notifyTargetId.equals(message.getUserId())) {
            notificationService.send(notifyTargetId, 3, notifyTitle, notifyContent, item.getId());
        }

        return Result.success();
    }

    @Override
    public Result<List<ItemMessage>> getMessagesByItem(Long itemId) {
        QueryWrapper<ItemMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("item_id", itemId);
        wrapper.orderByAsc("created_at"); // 按时间顺序

        List<ItemMessage> allMessages = list(wrapper);

        // 组装成树状结构
        List<ItemMessage> rootMessages = new ArrayList<>();
        
        for (ItemMessage msg : allMessages) {
            // 填充用户信息
            User user = userMapper.selectById(msg.getUserId());
            if (user != null) {
                user.setPassword(null);
                msg.setUser(user);
            }
            if (msg.getReplyToUserId() != null) {
                User replyToUser = userMapper.selectById(msg.getReplyToUserId());
                if (replyToUser != null) {
                    replyToUser.setPassword(null);
                    msg.setReplyToUser(replyToUser);
                }
            }
            
            if (msg.getParentId() == null) {
                rootMessages.add(msg);
            } else {
                // 找到父节点并加入
                for (ItemMessage root : rootMessages) {
                    if (root.getMessageId().equals(msg.getParentId())) {
                        if (root.getReplies() == null) {
                            root.setReplies(new ArrayList<>());
                        }
                        root.getReplies().add(msg);
                        break;
                    }
                }
            }
        }

        return Result.success(rootMessages);
    }

    @Override
    public Result<?> deleteMessage(Long messageId, Long userId) {
        ItemMessage message = getById(messageId);
        if (message == null) {
            return Result.error("留言不存在");
        }
        
        // 只有留言发布者可以删除（后续可加入管理员或者物品主人的删除权限）
        if (!message.getUserId().equals(userId)) {
            return Result.error("无权删除");
        }
        
        // 物理删除或者逻辑删除
        removeById(messageId);
        
        // 删除子留言
        QueryWrapper<ItemMessage> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", messageId);
        remove(childWrapper);
        
        return Result.success();
    }
}