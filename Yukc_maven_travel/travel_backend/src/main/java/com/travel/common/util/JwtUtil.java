package com.travel.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具类
 * 负责生成令牌、解析令牌、校验合法性
 */
@Component
public class JwtUtil {

    // 从配置文件读取密钥
    @Value("${jwt.secret}")
    private String secretStr;

    // 从配置文件读取过期时间
    @Value("${jwt.expire-time}")
    private Long expireTime;

    // 签名密钥对象
    private SecretKey secretKey;

    /**
     * @PostConstruct：对象创建完成、依赖注入完成后执行
     * 把配置里的字符串密钥转换成 SecretKey 对象
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretStr);
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 token
     * @param userId 用户ID
     * @param isAdmin 是否管理员
     * @return JWT 令牌字符串
     */
    public String generateToken(Long userId, Integer isAdmin) {
        // 1. 组装负载数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("isAdmin", isAdmin);

        // 2. 计算过期时间
        long now = System.currentTimeMillis();
        Date expireDate = new Date(now + expireTime);

        // 3. 构建 token
        return Jwts.builder()
                .setClaims(claims)          // 设置负载
                .setExpiration(expireDate)  // 设置过期时间
                .signWith(secretKey, SignatureAlgorithm.HS256) // 设置签名算法和密钥
                .compact();//这步就是拼接
    }

    /**
     * 解析 token
     * @param token 前端传的令牌
     * @return 解析成功返回 Claims（负载数据），失败/过期/篡改返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)  // 设置校验密钥
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 捕获所有异常：过期、签名错误、格式错误、篡改
            // 统一返回 null，代表 token 无效
            return null;
        }
    }
}
