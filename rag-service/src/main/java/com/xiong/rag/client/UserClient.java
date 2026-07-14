package com.xiong.rag.client;

import com.xiong.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// name 必须是 user-service 在 Nacos 里注册的服务名！
@FeignClient(name = "user-service")
public interface UserClient {

    // 这里的方法签名必须和 user-service 里的 UserController 一模一样
    @PostMapping("/user/deduct")
    Result<String> deductPoint(@RequestParam("userId") Long userId,
                               @RequestParam("points") Integer points);
}
