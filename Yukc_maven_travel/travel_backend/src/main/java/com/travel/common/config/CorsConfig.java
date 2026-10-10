package com.travel.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有请求来源，开发环境使用；生产环境建议替换为具体前端域名
                .allowedOriginPatterns("*")
                // 允许所有HTTP请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有请求头，支持自定义请求头（比如后续的token请求头）
                .allowedHeaders("*")
                // 允许携带凭证（Cookie、认证信息）
                .allowCredentials(true)
                // 预检请求缓存时间，单位秒；缓存期间不用重复发送预检请求
                .maxAge(3600);
    }
}
//第二步：配置说明
//为什么会有跨域
//浏览器的「同源策略」规定：协议、域名、端口三者完全一致的地址才能互相请求。前后端分离项目中，前端通常运行在 localhost:8081 等端口，后端在 8080，端口不同就会触发跨域拦截，导致请求失败。
//关键配置项解释
//addMapping("/**")：对项目所有接口路径都生效
//allowedOriginPatterns("*")：允许所有来源的请求。开发阶段用 * 最方便；上线生产时建议改成具体的前端域名，比如 .allowedOriginPatterns("http://localhost:8081")，更安全
//allowCredentials(true)：允许请求携带认证信息，后续我们做 token 鉴权时，token 放在请求头里需要这个配置支持
//maxAge(3600)：浏览器的预检 OPTIONS 请求缓存 1 小时，避免每次请求都先发一次预检，提升性能
//和 @CrossOrigin 的区别
//@CrossOrigin 是加在单个 Controller / 方法上的局部跨域注解
//我们用的全局配置是项目统一管理，不用每个 Controller 都加，更规范，也避免遗漏