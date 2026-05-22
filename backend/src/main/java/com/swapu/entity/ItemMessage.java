package com.swapu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("item_message")
public class ItemMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    private Long itemId;

    private Long userId;

    private String content;

    private Long parentId;

    private Long replyToUserId;

    private LocalDateTime createdAt;

    // 显示关联数据
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private User replyToUser;

    @TableField(exist = false)
    private List<ItemMessage> replies; // 子留言
}