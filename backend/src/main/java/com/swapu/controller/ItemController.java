package com.swapu.controller;

import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.service.ItemService;
import com.swapu.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("/publish")
    public Result<?> publish(@RequestBody @Validated Item item, HttpServletRequest request) {
        // 从 Token 获取当前用户 ID
        Long userId = (Long) request.getAttribute("userId");
        item.setUserId(userId);
        
        return itemService.publish(item);
    }

    @GetMapping("/list")
    public Result<List<Item>> list(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer categoryId,
                                   @RequestParam(required = false) Integer conditionRate,
                                   @RequestParam(required = false) java.math.BigDecimal minPrice,
                                   @RequestParam(required = false) java.math.BigDecimal maxPrice,
                                   @RequestParam(required = false) String sortField,
                                   @RequestParam(required = false) String sortOrder,
                                   @RequestParam(required = false) Long userId) {
        return itemService.listItemsWithFilters(keyword, categoryId, conditionRate, minPrice, maxPrice, sortField, sortOrder, userId);
    }

    @GetMapping("/search")
    public Result<?> search(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) Integer categoryId,
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(itemService.search(keyword, categoryId, page, size));
    }

    @GetMapping("/suggest")
    public Result<?> suggest(@RequestParam String keyword) {
        return Result.success(itemService.suggest(keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<Item> detail(@PathVariable Long id) {
        return itemService.getItemDetail(id);
    }

    @GetMapping("/my/publish")
    public Result<List<Item>> myPublish(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return itemService.listMyItems(userId);
    }

    @GetMapping("/my/want")
    public Result<List<Item>> myWant(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return itemService.listMyWants(userId);
    }

    @PostMapping("/status/{itemId}/{status}")
    public Result<?> updateStatus(@PathVariable Long itemId, @PathVariable Integer status, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return itemService.updateStatus(userId, itemId, status);
    }

    @DeleteMapping("/{itemId}")
    public Result<?> delete(@PathVariable Long itemId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return itemService.deleteItem(userId, itemId);
    }
    
    // 获取用户发布的在售物品 (公开接口)
    @GetMapping("/user/{userId}/selling")
    public Result<List<Item>> listUserSelling(@PathVariable Long userId) {
        Item query = new Item();
        query.setUserId(userId);
        query.setStatus(1); // 在售
        return itemService.listItems(query);
    }
    
    @PostMapping("/update")
    public Result<?> update(@RequestBody @Validated Item item, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        item.setUserId(userId);
        return itemService.updateItem(item);
    }
}
