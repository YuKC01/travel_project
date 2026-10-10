package com.travel.common.util;

/**
 * 当前登录用户上下文
 * 基于ThreadLocal实现，线程隔离，保证多线程环境下用户信息不串
 */
public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    /**
     * 当前用户管理员身份：1-管理员，0-普通用户
     */
    private static final ThreadLocal<Integer> CURRENT_IS_ADMIN = new ThreadLocal<>();

    /**
     * 设置当前登录用户ID
     */
    public static void setUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回null
     */
    public static Long getUserId() {
        return CURRENT_USER.get();
    }

    /**
     * 设置当前用户管理员身份
     * @param isAdmin 1-管理员，0-普通用户
     */
    public static void setIsAdmin(Integer isAdmin) {
        CURRENT_IS_ADMIN.set(isAdmin);
    }

    /**
     * 获取当前用户管理员身份
     * @return 1-管理员，0-普通用户，未设置返回null
     */
    public static Integer getIsAdmin() {
        return CURRENT_IS_ADMIN.get();
    }

    /**
     * 清空当前用户信息
     * 请求结束后必须调用，避免内存泄漏
     */
    public static void clear() {
        CURRENT_USER.remove();
        CURRENT_IS_ADMIN.remove();
    }
}
