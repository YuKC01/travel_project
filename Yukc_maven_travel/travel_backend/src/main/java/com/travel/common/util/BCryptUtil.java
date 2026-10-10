package com.travel.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密工具类
 * 用于用户密码加密存储、登录密码比对
 */
public class BCryptUtil {

    // 创建加密器对象，全局单例，重复使用
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 明文密码加密
     * @param rawPassword 前端传的明文密码
     * @return BCrypt加密后的密文字符串
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 密码比对
     * @param rawPassword 前端传的明文密码
     * @param dbPassword 数据库里存的加密密码
     * @return true 匹配成功，false 匹配失败
     */
    public static boolean matches(String rawPassword, String dbPassword) {
        return ENCODER.matches(rawPassword, dbPassword);
    }
}
