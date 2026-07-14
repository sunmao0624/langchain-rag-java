package com.xiong.gateway;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
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

    private static final byte[] KEY = "xiong_secret_key".getBytes();

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
            // 校验 Token
            if (!JWTUtil.verify(token, KEY)) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            // 解析 Token
            JWT jwt = JWTUtil.parseToken(token);

            // 获取 userId（注意这里的 key 要与你生成 Token 时一致）
            Object userId = jwt.getPayload("userId");

            if (userId == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            // 将 userId 放入请求头
            ServerHttpRequest newRequest = request.mutate()
                    .header("X-User-Id", userId.toString())
                    .build();

            // 使用新的 Request 继续向下游转发
            return chain.filter(
                    exchange.mutate()
                            .request(newRequest)
                            .build()
            );
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }
    @Override
    public int getOrder() {
        return 0;
    }
}
