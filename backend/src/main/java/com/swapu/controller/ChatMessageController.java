package com.swapu.controller;

import com.swapu.common.Result;
import com.swapu.entity.ChatMessage;
import com.swapu.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/history/{contactId}")
    public Result<List<ChatMessage>> getHistory(@PathVariable Long contactId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatMessageService.getHistory(userId, contactId);
    }

    @PostMapping("/read/{contactId}")
    public Result<?> markAsRead(@PathVariable Long contactId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatMessageService.markAsRead(userId, contactId);
    }

    @GetMapping("/contacts")
    public Result<List<Map<String, Object>>> getContacts(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return chatMessageService.getContactList(userId);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.success(0L);
        }
        return chatMessageService.getUnreadTotalCount(userId);
    }
}