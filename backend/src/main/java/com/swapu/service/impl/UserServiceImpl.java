package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.common.Result;
import com.swapu.entity.User;
import com.swapu.entity.LoginVO;
import com.swapu.entity.ChangePasswordDTO;
import com.swapu.mapper.UserMapper;
import com.swapu.service.UserService;
import com.swapu.utils.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result<LoginVO> login(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User dbUser = getOne(queryWrapper);

        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        // 兼容明文密码（老数据）和加密密码
        boolean passwordMatch = false;
        if (passwordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            passwordMatch = true;
        } else if (dbUser.getPassword().equals(user.getPassword())) {
            // 如果明文匹配，自动升级为加密存储
            dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
            updateById(dbUser);
            passwordMatch = true;
        }

        if (!passwordMatch) {
            return Result.error("密码错误");
        }

        if (dbUser.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        
        // 更新最后登录时间
        dbUser.setLastLoginTime(LocalDateTime.now());
        updateById(dbUser);

        // 生成 Token
        String token = JwtUtils.generateToken(dbUser.getUserId(), dbUser.getUsername(), dbUser.getRole());

        dbUser.setPassword(null); // 不返回密码
        
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(dbUser);
        
        return Result.success(loginVO);
    }

    @Override
    public Result<?> register(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (count(queryWrapper) > 0) {
            return Result.error("用户名已存在");
        }

        // 设置默认值
        if (user.getNickname() == null) {
            user.setNickname("User_" + System.currentTimeMillis());
        }
        user.setRole(0); // 默认普通学生
        user.setStatus(1); // 默认正常
        user.setCreditScore(100);

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        save(user);
        return Result.success();
    }

    @Override
    public Result<?> changePassword(Long userId, ChangePasswordDTO dto) {
        User user = getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证旧密码 (兼容明文和加密)
        boolean oldPasswordMatch = false;
        if (passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            oldPasswordMatch = true;
        } else if (user.getPassword().equals(dto.getOldPassword())) {
            oldPasswordMatch = true;
        }

        if (!oldPasswordMatch) {
            return Result.error("旧密码错误");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次输入的新密码不一致");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
        return Result.success();
    }
}
