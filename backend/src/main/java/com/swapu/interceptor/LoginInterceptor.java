package com.swapu.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapu.common.Result;
import com.swapu.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取请求头中的令牌 (Token)
        String token = request.getHeader("Authorization");

        // 2. 判断令牌是否存在
        if (!StringUtils.hasLength(token)) {
            // 返回未登录错误
            Result<String> error = Result.error(401, "未登录");
            String notLogin = new ObjectMapper().writeValueAsString(error);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(notLogin);
            return false;
        }

        // 3. 解析令牌
        try {
            Claims claims = JwtUtils.validateToken(token);
            if (claims == null) {
                throw new RuntimeException("Token无效");
            }
            
            // 将用户信息存入 Request 域，方便后续 Controller 获取
            // claims.get("userId") 返回的是 Integer 类型，需要转换为 Long
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Integer) {
                request.setAttribute("userId", ((Integer) userIdObj).longValue());
            } else if (userIdObj instanceof Long) {
                request.setAttribute("userId", (Long) userIdObj);
            } else {
                 request.setAttribute("userId", Long.valueOf(userIdObj.toString()));
            }
            request.setAttribute("username", claims.getSubject());
            request.setAttribute("role", claims.get("role"));
            
            return true;
        } catch (Exception e) {
            // 返回 Token 无效错误
            Result<String> error = Result.error(401, "未登录或Token已过期");
            String notLogin = new ObjectMapper().writeValueAsString(error);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(notLogin);
            return false;
        }
    }
}
