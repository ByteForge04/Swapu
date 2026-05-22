package com.swapu.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swapu.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;


public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 Request 域获取角色信息 (由 LoginInterceptor 放入)
        Object roleObj = request.getAttribute("role");
        
        // 判断是否为管理员 (role = 1)
        if (roleObj == null || !Integer.valueOf(1).equals(Integer.valueOf(roleObj.toString()))) {
            Result<String> error = Result.error(403, "无权访问，需要管理员权限");
            String forbidden = new ObjectMapper().writeValueAsString(error);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(forbidden);
            return false;
        }

        return true;
    }
}
