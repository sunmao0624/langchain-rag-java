package com.xiong.common.utils;

public class UserContext {
    private static final ThreadLocal<Long> CONTEXT = new ThreadLocal<>();
    public static void setUserId(Long userId) { CONTEXT.set(userId); }
    public static Long getUserId() { return CONTEXT.get(); }
    public static void clear() { CONTEXT.remove(); }
}
