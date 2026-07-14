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

    /**
     * 扣除用户积分 (供其他微服务内部调用)
     */
    @PostMapping("/deduct")
    public Result<String> deductPoint(@RequestParam("userId") Long userId,
                                      @RequestParam("points") Integer points) {
        // 先写死逻辑，不连数据库
        System.out.println("========== 微服务内部调用成功 ==========");
        System.out.println("正在为用户 ID: " + userId + " 扣除积分: " + points);
        System.out.println("积分扣除完毕！");
        System.out.println("=====================================");

        return Result.success("积分扣除成功");
    }

}
