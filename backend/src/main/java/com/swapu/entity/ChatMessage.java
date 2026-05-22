package com.swapu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "msg_id", type = IdType.AUTO)
    private Long msgId;

    private Long senderId;

    private Long receiverId;

    private String content;

    private Integer msgType; // 1-文本, 2-图片, 3-商品卡片

    private Long relatedId;

    private Integer isRead;

    private LocalDateTime createdAt;
}