package com.swapu.controller;

import com.swapu.common.Result;
import com.swapu.entity.ItemMessage;
import com.swapu.service.ItemMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/item-message")
public class ItemMessageController {

    @Autowired
    private ItemMessageService itemMessageService;

    @PostMapping("/add")
    public Result<?> addMessage(@RequestBody ItemMessage message, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        message.setUserId(userId);
        return itemMessageService.addMessage(message);
    }

    @GetMapping("/list/{itemId}")
    public Result<List<ItemMessage>> getMessages(@PathVariable Long itemId) {
        return itemMessageService.getMessagesByItem(itemId);
    }

    @DeleteMapping("/delete/{messageId}")
    public Result<?> deleteMessage(@PathVariable Long messageId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return itemMessageService.deleteMessage(messageId, userId);
    }
}