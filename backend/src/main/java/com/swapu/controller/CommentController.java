package com.swapu.controller;

import com.swapu.common.Result;
import com.swapu.entity.Comment;
import com.swapu.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/create")
    public Result<?> create(@RequestBody @Validated Comment comment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        comment.setUserId(userId);
        return commentService.createComment(comment);
    }

    @GetMapping("/order/{orderId}")
    public Result<Comment> getByOrder(@PathVariable Long orderId) {
        return commentService.getCommentByOrder(orderId);
    }

    @GetMapping("/item/{itemId}")
    public Result<List<Comment>> getByItem(@PathVariable Long itemId) {
        return commentService.getCommentsByItem(itemId);
    }
    
    @GetMapping("/user/{userId}")
    public Result<List<Comment>> getByUser(@PathVariable Long userId) {
        return commentService.getCommentsByUser(userId);
    }

    // 公开接口：获取卖家的评价
    @GetMapping("/public/seller/{userId}")
    public Result<List<Comment>> getSellerComments(@PathVariable Long userId) {
        return commentService.getCommentsByUser(userId);
    }
}
