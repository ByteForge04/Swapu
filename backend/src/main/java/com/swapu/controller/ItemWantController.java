package com.swapu.controller;

import com.swapu.common.Result;
import com.swapu.service.ItemWantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/item/want")
public class ItemWantController {

    @Autowired
    private ItemWantService itemWantService;

    @PostMapping("/{itemId}")
    public Result<?> toggle(@PathVariable Long itemId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return itemWantService.toggleWant(userId, itemId);
    }

    @GetMapping("/check/{itemId}")
    public Result<Boolean> check(@PathVariable Long itemId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.success(false);
        }
        return itemWantService.checkWant(userId, itemId);
    }
}
