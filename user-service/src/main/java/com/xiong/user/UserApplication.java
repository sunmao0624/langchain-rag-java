package com.xiong.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient   // 开启 Nacos 服务注册与发现
@RestController       // 让这个类可以接收 HTTP 请求
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    // 写一个测试接口
    @GetMapping("/user/ping")
    public String ping() {
        return "User Service is running successfully!";
    }
}
