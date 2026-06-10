package com.xiong.user.controller;

import com.xiong.common.result.Result;
import com.xiong.user.dto.UserLoginDTO;
import com.xiong.user.dto.UserRegisterDTO;
import com.xiong.user.entity.User;
import com.xiong.user.service.UserService;
import com.xiong.user.vo.LoginUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);

        if (user != null) {
            return Result.success(user);
        }else {
            return Result.error(404,"查无此人");
        }

    }

    @PostMapping("/register")
    public Result<String> register (@RequestBody UserRegisterDTO dto){
        return userService.register(dto);
    }

    @PostMapping("/login")
    public Result<LoginUserVO> login(@RequestBody UserLoginDTO dto){
        return userService.login(dto);
    }

}
