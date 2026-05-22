package com.swapu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_order")
public class TradeOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    private String orderNo;

    private Long buyerId;

    private Long sellerId;

    private Long itemId;

    private BigDecimal amount;

    private Integer transactionMethod; // 1-自提, 2-送货上门, 3-快递/邮寄

    private String shippingAddress;

    private Integer status; // 0-待卖家确认, 1-进行中/待交付, 2-已完成, 3-已取消

    private Integer paymentStatus; // 0-未支付, 1-已支付

    private Integer paymentMethod; // 1-线上支付, 2-线下自行交易

    private String tradeNo; // 支付宝交易号

    private LocalDateTime payTime; // 支付时间

    private String buyerNote;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    // Additional fields for display
    @TableField(exist = false)
    private Item item;

    @TableField(exist = false)
    private User buyer;

    @TableField(exist = false)
    private User seller;
}
