package com.swapu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.common.Result;
import com.swapu.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    /**
     * 发表评价
     */
    Result<?> createComment(Comment comment);

    /**
     * 获取订单的评价
     */
    Result<Comment> getCommentByOrder(Long orderId);

    /**
     * 获取物品的评价列表
     */
    Result<List<Comment>> getCommentsByItem(Long itemId);
    
    /**
     * 获取用户的评价列表 (作为买家或卖家)
     */
    Result<List<Comment>> getCommentsByUser(Long userId);
}
