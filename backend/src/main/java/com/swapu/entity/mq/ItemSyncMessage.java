package com.swapu.entity.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemSyncMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 物品ID
     */
    private Long itemId;

    /**
     * 操作类型: 1-上架/更新(同步), 2-下架/删除(删除)
     */
    private Integer type;

    // Manual Getters/Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
}
