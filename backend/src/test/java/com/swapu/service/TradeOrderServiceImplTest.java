package com.swapu.service;

import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.entity.TradeOrder;
import com.swapu.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TradeOrderServiceImplTest {

    @Autowired
    private TradeOrderService tradeOrderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private User buyer;
    private User seller;
    private Item item;

    @BeforeEach
    public void setup() {
        // 创建买家
        buyer = new User();
        buyer.setUsername("buyer123");
        buyer.setPassword("pass");
        userService.register(buyer);
        buyer = userService.getById(buyer.getUserId()); // fetch DB id

        // 创建卖家
        seller = new User();
        seller.setUsername("seller456");
        seller.setPassword("pass");
        userService.register(seller);
        seller = userService.getById(seller.getUserId());

        // 创建商品
        item = new Item();
        item.setUserId(seller.getUserId());
        item.setCategoryId(1);
        item.setTitle("Test Item");
        item.setDescription("Test Desc");
        item.setPrice(new BigDecimal("100.00"));
        item.setImages("[]");
        item.setStatus(1); // 1 = 在售
        itemService.save(item);
    }

    @Test
    public void testCreateOrderSuccess() {
        TradeOrder order = new TradeOrder();
        order.setItemId(item.getItemId());
        order.setBuyerId(buyer.getUserId());
        order.setAmount(new BigDecimal("100.00"));

        Result<?> result = tradeOrderService.createOrder(order);
        assertTrue(result.isSuccess(), "下单应成功");

        // 验证商品状态变为交易中 (2)
        Item updatedItem = itemService.getById(item.getItemId());
        assertEquals(2, updatedItem.getStatus(), "商品状态应变为交易中");
    }

    @Test
    public void testCreateOrderAmountMismatch() {
        TradeOrder order = new TradeOrder();
        order.setItemId(item.getItemId());
        order.setBuyerId(buyer.getUserId());
        order.setAmount(new BigDecimal("99.00")); // 金额不一致

        Result<?> result = tradeOrderService.createOrder(order);
        assertFalse(result.isSuccess(), "金额不一致下单应失败");
        assertEquals("订单金额与商品价格不一致", result.getMsg());

        // 验证商品状态仍为在售 (1)
        Item updatedItem = itemService.getById(item.getItemId());
        assertEquals(1, updatedItem.getStatus(), "商品状态应保持在售");
    }

    @Test
    public void testCreateOrderSelfBuy() {
        TradeOrder order = new TradeOrder();
        order.setItemId(item.getItemId());
        order.setBuyerId(seller.getUserId()); // 卖家自己买

        Result<?> result = tradeOrderService.createOrder(order);
        assertFalse(result.isSuccess(), "不能购买自己发布的物品");
        assertEquals("不能购买自己发布的物品", result.getMsg());
    }

    @Test
    public void testCreateOrderItemNotAvailable() {
        // 先修改商品为下架状态 (4)
        item.setStatus(4);
        itemService.updateById(item);

        TradeOrder order = new TradeOrder();
        order.setItemId(item.getItemId());
        order.setBuyerId(buyer.getUserId());

        Result<?> result = tradeOrderService.createOrder(order);
        assertFalse(result.isSuccess(), "下架商品不可购买");
        assertEquals("物品当前不可购买", result.getMsg());
    }
}