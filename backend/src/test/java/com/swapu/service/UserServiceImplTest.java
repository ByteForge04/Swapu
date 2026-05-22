package com.swapu.service;

import com.swapu.common.Result;
import com.swapu.entity.LoginVO;
import com.swapu.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // 保证测试数据不污染数据库
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterAndLoginSuccess() {
        // 1. 注册新用户
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPassword("password123");
        
        Result<?> regResult = userService.register(newUser);
        assertTrue(regResult.isSuccess(), "注册应该成功");

        // 2. 正常登录
        User loginUser = new User();
        loginUser.setUsername("testuser");
        loginUser.setPassword("password123");

        Result<LoginVO> loginResult = userService.login(loginUser);
        assertTrue(loginResult.isSuccess(), "登录应该成功");
        assertNotNull(loginResult.getData().getToken(), "登录成功应返回Token");
        assertEquals("testuser", loginResult.getData().getUser().getUsername());
    }

    @Test
    public void testLoginWrongPassword() {
        // 1. 注册新用户
        User newUser = new User();
        newUser.setUsername("testuser_wrong");
        newUser.setPassword("password123");
        userService.register(newUser);

        // 2. 错误密码登录
        User loginUser = new User();
        loginUser.setUsername("testuser_wrong");
        loginUser.setPassword("wrongpassword");

        Result<LoginVO> loginResult = userService.login(loginUser);
        assertFalse(loginResult.isSuccess(), "密码错误登录应失败");
        assertEquals("密码错误", loginResult.getMsg());
    }

    @Test
    public void testLoginUserNotFound() {
        User loginUser = new User();
        loginUser.setUsername("non_existent_user");
        loginUser.setPassword("any");

        Result<LoginVO> loginResult = userService.login(loginUser);
        assertFalse(loginResult.isSuccess(), "用户不存在登录应失败");
        assertEquals("用户不存在", loginResult.getMsg());
    }
}
