package com.xiong.gateway;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();

        // 1. 白名单放行：登录和注册接口不需要查 Token
        if (path.contains("/user/login") || path.contains("/user/register") || path.contains("/user/ping")) {
            return chain.filter(exchange);
        }

        // 2. 获取请求头中的 Token (约定前端放在 Authorization 头里)
        String token = request.getHeaders().getFirst("Authorization");

        // 3. 校验 Token 是否存在
        if (StrUtil.isBlank(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // HTTP 401 没权限
            return response.setComplete();
        }

        // 4. 校验 Token 的合法性 (必须和 user-service 签发时用的密钥一致)
        try {
            boolean verify = JWTUtil.verify(token, "xiong_secret_key".getBytes());
            if (!verify) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 5. 校验通过，放行请求给下游的微服务
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
