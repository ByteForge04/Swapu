package com.swapu.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.entity.User;
import com.swapu.service.ItemService;
import com.swapu.service.TradeOrderService;
import com.swapu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.swapu.entity.SearchHistory;
import com.swapu.entity.TradeOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private TradeOrderService tradeOrderService;

    @Autowired
    private com.swapu.service.SysNotificationService notificationService;
    
    @Autowired
    private com.swapu.service.SearchHistoryService searchHistoryService;

    // --- 用户管理 ---

    /**
     * 管理员获取用户信息 (包含详细)
     */
    @GetMapping("/user/{userId}")
    public Result<User> getUserDetail(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    /**
     * 用户列表
     */
    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) String username) {
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            queryWrapper.like("username", username);
        }
        queryWrapper.orderByDesc("created_at");
        return Result.success(userService.page(pageParam, queryWrapper));
    }

    /**
     * 更新用户状态 (禁用/启用)
     */
    @PostMapping("/user/status/{userId}/{status}")
    public Result<?> updateUserStatus(@PathVariable Long userId, @PathVariable Integer status) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 不能禁用自己 (简单判断，实际应判断当前登录用户ID)
        // 这里只是示例
        
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    // --- 物品管理 ---

    /**
     * 物品列表
     */
    @GetMapping("/item/list")
    public Result<?> itemList(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) String title,
                              @RequestParam(required = false) Integer status) {
        Page<Item> pageParam = new Page<>(page, size);
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            queryWrapper.like("title", title);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("created_at");
        return Result.success(itemService.page(pageParam, queryWrapper));
    }

    /**
     * 强制下架/删除物品/审核通过
     */
    @PostMapping("/item/status/{itemId}/{status}")
    public Result<?> updateItemStatus(@PathVariable Long itemId, @PathVariable Integer status) {
        Item item = itemService.getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }
        item.setStatus(status);
        itemService.updateById(item);
        
        // 同步 ES
        if (status == 1) {
            itemService.syncToEs(itemId);
        } else {
            itemService.deleteFromEs(itemId);
        }
        
        // 发送系统通知
        String title = "系统通知";
        String content = "";
        if (status == 1) {
            title = "审核通过";
            content = "您的物品 [" + item.getTitle() + "] 已通过审核，现已上架。";
        } else if (status == 4) {
            title = "物品下架";
            content = "您的物品 [" + item.getTitle() + "] 已被管理员下架/驳回，请检查是否违规。";
        }
        
        if (!content.isEmpty()) {
            notificationService.send(item.getUserId(), 1, title, content, itemId);
        }

        return Result.success();
    }

    /**
     * 全量同步 ES 数据
     */
    @PostMapping("/es/sync")
    public Result<?> syncEs() {
        itemService.syncAllToEs();
        return Result.success("同步完成");
    }
    
    // --- 统计数据 ---
    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        long userCount = userService.count();
        long itemCount = itemService.count();
        
        // 1. DAU (Today)
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        QueryWrapper<User> dauWrapper = new QueryWrapper<>();
        dauWrapper.ge("last_login_time", todayStart);
        long dau = userService.count(dauWrapper);
        
        // 2. New Items Today
        QueryWrapper<Item> newItemWrapper = new QueryWrapper<>();
        newItemWrapper.ge("created_at", todayStart);
        long newItemsToday = itemService.count(newItemWrapper);
        
        // 3. Transaction Amount (Total Completed)
        QueryWrapper<com.swapu.entity.TradeOrder> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("status", 2); // Completed
        List<com.swapu.entity.TradeOrder> orders = tradeOrderService.list(orderWrapper);
        BigDecimal totalAmount = orders.stream()
                .map(com.swapu.entity.TradeOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        // 4. Hot Search Terms (Top 10)
        QueryWrapper<SearchHistory> searchWrapper = new QueryWrapper<>();
        searchWrapper.orderByDesc("count").last("LIMIT 10");
        List<SearchHistory> hotSearch = searchHistoryService.list(searchWrapper);

        List<Map<String, Object>> chartData = tradeOrderService.getOrderStatistics(7);
        
        return Result.success(new DashboardData(
            userCount, itemCount, dau, newItemsToday, totalAmount, hotSearch, chartData
        ));
    }
    
    static class DashboardData {
        public long userCount;
        public long itemCount;
        public long dau;
        public long newItemsToday;
        public BigDecimal totalAmount;
        public List<SearchHistory> hotSearch;
        public List<Map<String, Object>> chartData;
        
        public DashboardData(long userCount, long itemCount, long dau, long newItemsToday, 
                             BigDecimal totalAmount, List<SearchHistory> hotSearch, 
                             List<Map<String, Object>> chartData) {
            this.userCount = userCount;
            this.itemCount = itemCount;
            this.dau = dau;
            this.newItemsToday = newItemsToday;
            this.totalAmount = totalAmount;
            this.hotSearch = hotSearch;
            this.chartData = chartData;
        }
    }
}
