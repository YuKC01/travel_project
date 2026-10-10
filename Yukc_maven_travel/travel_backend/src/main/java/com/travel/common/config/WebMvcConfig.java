package com.travel.common.config;

import com.travel.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有请求，默认都需要登录
                .addPathPatterns("/**")
                // 只放行公开接口和静态资源
                .excludePathPatterns(
                        // ========== 认证相关公开接口 ==========
                        "/user/login",
                        "/user/register",

                        // ========== 首页与静态页面 ==========
                        "/",                  // 根路径，Spring Boot 会自动映射到 index.html
                        "/index.html",
                        "/error",             // Spring Boot 默认错误页，不拦截避免掩盖真实错误
                        "/api/test/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",   // Swagger UI 入口（springdoc 会重定向）
                        "/v3/api-docs/**",
                        "/v3/api-docs",       // 不带后缀的也要放行
                        "/webjars/**",
                        // 静态资源后缀放行（如果以后有 .js/.css/.png 等）
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.jpeg",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.ico",
                        "/**/*.woff",
                        "/**/*.woff2",
                        "/**/*.ttf",
                        "/favicon.ico",

                        // ========== 攻略公开查询接口（无需登录即可浏览） ==========
                        "/strategy/list",
                        "/strategy/page",
                        "/strategy/search",
                        "/strategy/detail/**",
                        "/strategy/comment/list/**",
                        "/strategy/filter",
                        "/strategy/hot",
                        "/strategy/banner",
                        "/strategy/cities"
                );
    }
}
