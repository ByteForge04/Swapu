package com.swapu.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swapu.common.Result;
import com.swapu.entity.SysAnnouncement;
import com.swapu.service.SysAnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/announcement")
public class SysAnnouncementController {

    @Autowired
    private SysAnnouncementService announcementService;

    // Public list (Active only)
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        Page<SysAnnouncement> pageParam = new Page<>(page, size);
        QueryWrapper<SysAnnouncement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1).orderByDesc("created_at");
        return Result.success(announcementService.page(pageParam, queryWrapper));
    }

    // Admin list (All)
    @GetMapping("/admin/list")
    public Result<?> adminList(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        Page<SysAnnouncement> pageParam = new Page<>(page, size);
        QueryWrapper<SysAnnouncement> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        return Result.success(announcementService.page(pageParam, queryWrapper));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody SysAnnouncement announcement) {
        announcementService.save(announcement);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody SysAnnouncement announcement) {
        announcementService.updateById(announcement);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }
}
