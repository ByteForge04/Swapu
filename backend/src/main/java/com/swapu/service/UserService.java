package com.swapu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.entity.User;
import com.swapu.entity.LoginVO;
import com.swapu.entity.ChangePasswordDTO;
import com.swapu.common.Result;

public interface UserService extends IService<User> {
    Result<LoginVO> login(User user);
    Result<?> register(User user);
    Result<?> changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}
