package com.swapu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("item_want")
public class ItemWant implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "want_id", type = IdType.AUTO)
    private Long wantId;

    private Long userId;

    private Long itemId;

    private LocalDateTime createdAt;

    // Manual Getters and Setters
    public Long getWantId() { return wantId; }
    public void setWantId(Long wantId) { this.wantId = wantId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
