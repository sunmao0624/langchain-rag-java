package com.xiong.user.vo;

import lombok.Data;

@Data
public class LoginUserVO {

    private Long id;
    private String username;
    private String token; // 核心：我们将要生成的 JWT 令牌
}
