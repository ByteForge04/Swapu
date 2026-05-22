package com.swapu.controller.ws;

import com.alibaba.fastjson.JSON;
import com.swapu.entity.ChatMessage;
import com.swapu.mapper.ChatMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.swapu.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/chat/{userId}")
@Component
public class ChatWebSocketEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketEndpoint.class);

    // 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    // concurrent包的线程安全Set，用来存放每个客户端对应的ChatWebSocketEndpoint对象。
    private static ConcurrentHashMap<Long, ChatWebSocketEndpoint> webSocketMap = new ConcurrentHashMap<>();

    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    // 接收userId
    private Long userId;

    // 解决WebSocket无法直接注入Bean的问题
    private static ChatMessageMapper chatMessageMapper;

    @Autowired
    public void setChatMessageMapper(ChatMessageMapper chatMessageMapper) {
        ChatWebSocketEndpoint.chatMessageMapper = chatMessageMapper;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.session = session;
        
        // WebSocket 鉴权
        Map<String, List<String>> requestParameterMap = session.getRequestParameterMap();
        List<String> tokens = requestParameterMap.get("token");
        
        if (tokens == null || tokens.isEmpty()) {
            log.error("WebSocket 连接失败：未提供 Token. userId: {}", userId);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Missing Token"));
            } catch (IOException e) {
                log.error("关闭 session 异常", e);
            }
            return;
        }

        String token = tokens.get(0);
        try {
            Long tokenUserId = JwtUtils.getUserId(token);
            if (tokenUserId == null || !userId.equals(tokenUserId)) {
                log.error("WebSocket 连接失败：Token userId 不匹配或无效. url userId: {}, token userId: {}", userId, tokenUserId);
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Invalid Token UserId"));
                return;
            }
        } catch (Exception e) {
            log.error("WebSocket 连接失败：Token 解析异常. userId: {}, token: {}", userId, token, e);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Invalid Token"));
            } catch (IOException ex) {
                log.error("关闭 session 异常", ex);
            }
            return;
        }

        this.userId = userId;
        if(webSocketMap.containsKey(userId)){
            webSocketMap.remove(userId);
            webSocketMap.put(userId,this);
            //加入set中
        }else{
            webSocketMap.put(userId,this);
            addOnlineCount();
        }

        log.info("用户连接:"+userId+",当前在线人数为:" + getOnlineCount());
    }

    @OnClose
    public void onClose() {
        if(webSocketMap.containsKey(userId)){
            webSocketMap.remove(userId);
            subOnlineCount();
        }
        log.info("用户退出:"+userId+",当前在线人数为:" + getOnlineCount());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if ("ping".equals(message)) {
            try {
                session.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                log.error("发送pong异常", e);
            }
            return;
        }

        log.info("收到用户消息:"+userId+",报文:"+message);
        
        try {
            // 解析消息
            ChatMessage chatMsg = JSON.parseObject(message, ChatMessage.class);
            chatMsg.setSenderId(userId);
            chatMsg.setCreatedAt(LocalDateTime.now());
            chatMsg.setIsRead(0);
            
            // 存入数据库
            chatMessageMapper.insert(chatMsg);
            
            // 转发给目标用户
            Long receiverId = chatMsg.getReceiverId();
            if (webSocketMap.containsKey(receiverId)) {
                webSocketMap.get(receiverId).sendMessage(JSON.toJSONString(chatMsg));
            } else {
                // 不在线，可考虑推送离线通知或保存为未读即可（前面已落库）
                log.info("目标用户不在线: {}", receiverId);
            }
            
        } catch (Exception e) {
            log.error("解析发送消息异常", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:"+this.userId+",原因:"+error.getMessage());
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        ChatWebSocketEndpoint.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        ChatWebSocketEndpoint.onlineCount--;
    }
}