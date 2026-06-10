package com.xiong.user.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiong.common.result.Result;
import com.xiong.user.dto.UserLoginDTO;
import com.xiong.user.dto.UserRegisterDTO;
import com.xiong.user.entity.User;
import com.xiong.user.mapper.UserMapper;
import com.xiong.user.service.UserService;
import com.xiong.user.vo.LoginUserVO;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public Result<String> register(UserRegisterDTO dto) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",dto.getUsername());
        User exisuser = this.baseMapper.selectOne(queryWrapper);

        if (exisuser!=null){
            return Result.error(400,"用户名已被注册");
        }

        String hasPassword = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());

        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(hasPassword);

        this.baseMapper.insert(newUser);

        return Result.success("注册成功");
    }

    @Override
    public Result<LoginUserVO> login(UserLoginDTO dto) {
        // 1. 根据用户名去数据库查询用户
        User user = this.lambdaQuery()
                .eq(User::getUsername,dto.getUsername())
                .one();

        //校验密码：因为数据库存的是 BCrypt 加密后的乱码，必须使用 checkpw 方法进行底层比对
        if (user == null){
            Result.error(400,"用户名或者密码错误");
        }
        boolean isMatch = BCrypt.checkpw(dto.getPassword(), user.getPassword());
        if (!isMatch){
            return Result.error(400,"用户名或者密码错误");
        }
        //密码正确！开始生成 JWT Token
        HashMap<String, Object> payload = new HashMap<>();
        // 将不敏感的非机密数据放入 Token 载荷中
        payload.put("userId",user.getId());
        payload.put("username",user.getUsername());
        // 设置过期时间，这里设置为 24 小时后过期
        payload.put("expire_time",System.currentTimeMillis() + 1000 * 60 * 60 * 24);

        // 生成 Token。注意："xiong_secret_key" 是签发密钥，真实生产环境中这个密钥会配在 Nacos 配置文件里，绝不能泄露
        String token = JWTUtil.createToken(payload, "xiong_secret_key".getBytes());

        //封装 VO 返回给前端
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(user.getId());
        loginUserVO.setUsername(user.getUsername());
        loginUserVO.setToken(token);

        return Result.success(loginUserVO);
    }
}
