package com.xiong.note.config;

import cn.hutool.core.util.StrUtil;
import com.xiong.note.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String userId = request.getHeader("X-User-Id");

        if (StrUtil.isNotBlank(userId)) {
            UserContext.setUserId(Long.parseLong(userId));
        }

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
