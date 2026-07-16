package com.xiong.common.interceptor;

import com.xiong.common.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 1. 获取请求头
        String userIdStr = request.getHeader("userId");

        // 2. 放入 ThreadLocal
        if (StringUtils.hasText(userIdStr)) {
            UserContext.setUserId(Long.parseLong(userIdStr));
        }

        // 3. 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        UserContext.clear();
    }
}
