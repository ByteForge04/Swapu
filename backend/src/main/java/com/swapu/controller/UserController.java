package com.swapu.controller;

import com.swapu.common.Result;
import com.swapu.entity.User;
import com.swapu.entity.LoginVO;
import com.swapu.entity.ChangePasswordDTO;
import com.swapu.service.UserService;
import com.swapu.common.validation.Groups;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedissonClient redissonClient;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated User user, HttpServletRequest request) {
        // IP 级别登录限流：每分钟最多 5 次请求
        String ip = request.getRemoteAddr();
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("login:rate:" + ip);
        rateLimiter.trySetRate(RateType.OVERALL, 5, 1, RateIntervalUnit.MINUTES);
        
        if (!rateLimiter.tryAcquire()) {
            return Result.error("登录尝试过于频繁，请稍后再试");
        }
        return userService.login(user);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody @Validated(Groups.Create.class) User user) {
        return userService.register(user);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }
    
    @PutMapping("/info")
    public Result<?> update(@RequestBody @Validated User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        user.setUserId(userId);
        // 不允许通过此接口修改密码和敏感信息
        user.setPassword(null); 
        user.setUsername(null);
        user.setRole(null);
        user.setStatus(null);
        user.setCreditScore(null);
        
        userService.updateById(user);
        return Result.success();
    }

    @PostMapping("/password")
    public Result<?> changePassword(@RequestBody @Validated ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.changePassword(userId, dto);
    }

    @PostMapping("/update")
    public Result<?> updatePost(@RequestBody @Validated User user, HttpServletRequest request) {
        return update(user, request);
    }
    
    // 公开接口：获取用户信息（用于卖家详情页）
    @GetMapping("/public/{userId}")
    public Result<User> getPublicUserInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            // 脱敏处理
            user.setPassword(null);
            user.setPhone(null); // 不返回手机号
            user.setWechatId(null); // 不返回微信号
            user.setQqId(null); // 不返回QQ号
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    // 提供给内部其他模块调用的简单信息查询接口
    @GetMapping("/info/{userId}")
    public Result<User> getBasicUserInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }
}
