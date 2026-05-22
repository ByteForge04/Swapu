package com.swapu.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.swapu.common.Result;
import com.swapu.entity.ItemCategory;
import com.swapu.service.ItemCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/category")
public class ItemCategoryController {

    @Autowired
    private ItemCategoryService itemCategoryService;

    // List all categories (Public)
    @GetMapping("/list")
    public Result<List<ItemCategory>> list() {
        QueryWrapper<ItemCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1).orderByAsc("sort_order");
        return Result.success(itemCategoryService.list(queryWrapper));
    }
    
    // List all for admin (including disabled)
    @GetMapping("/admin/list")
    public Result<List<ItemCategory>> adminList() {
        QueryWrapper<ItemCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order");
        return Result.success(itemCategoryService.list(queryWrapper));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody ItemCategory category) {
        itemCategoryService.save(category);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody ItemCategory category) {
        itemCategoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        itemCategoryService.removeById(id);
        return Result.success();
    }
}
