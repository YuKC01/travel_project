package com.travel.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 开发环境基础配置；生产环境可补充超时、连接池参数
        return new RestTemplate();
    }
}
//支持 GET、POST、PUT、DELETE 等所有常见请求方式
//自动把返回的 JSON 结果解析成 Java 对象（Map、实体类等）
//统一处理请求编码、响应格式转换
//可以全局配置超时、连接池、请求拦截器

//和 @Service/@Component 这类 “加在类上自动注册 Bean” 的注解不同，@Bean 是手动注册 Bean的方式
//适用对象：第三方库的类、需要自定义创建的对象
