package com.swapu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapu.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    @Select("SELECT sender_id, MAX(created_at) as last_time, COUNT(CASE WHEN is_read = 0 THEN 1 END) as unread_count " +
            "FROM chat_message WHERE receiver_id = #{userId} GROUP BY sender_id " +
            "UNION " +
            "SELECT receiver_id as sender_id, MAX(created_at) as last_time, 0 as unread_count " +
            "FROM chat_message WHERE sender_id = #{userId} GROUP BY receiver_id " +
            "ORDER BY last_time DESC")
    List<Map<String, Object>> getRecentChatUsers(@Param("userId") Long userId);
}