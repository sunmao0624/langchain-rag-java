package com.xiong.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiong.common.result.Result;
import com.xiong.user.dto.UserLoginDTO;
import com.xiong.user.dto.UserRegisterDTO;
import com.xiong.user.entity.User;
import com.xiong.user.vo.LoginUserVO;

public interface UserService extends IService<User> {

    //注册方法
    Result<String> register(UserRegisterDTO dto);
    //登入方法
    Result<LoginUserVO> login(UserLoginDTO dto);

}
