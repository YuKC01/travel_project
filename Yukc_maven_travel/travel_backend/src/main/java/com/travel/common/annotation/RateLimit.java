package com.travel.common.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解（基于 Guava RateLimiter 令牌桶算法）
 *
 * 【使用方式】
 * @RateLimit(qps = 1)  // 该接口每秒只允许 1 个请求通过
 * public Result<...> someMethod() { ... }
 *
 * 【工作流程】
 * 1. 请求到达 Controller 方法前，AOP 切面拦截
 * 2. 切面从缓存中取出该方法对应的 RateLimiter 实例（懒加载）
 * 3. 调用 tryAcquire()：
 *    - 拿到令牌 → 放行执行原方法
 *    - 拿不到   → 抛 BusinessException(429) → 走全局异常处理返回 429
 *
 * 【属性说明】
 * - qps：每秒允许的请求数，默认 1（最严格，适合写操作）
 * - timeout：等待令牌的超时时间，默认 0 表示不等待立即拒绝
 *
 * @Target(ElementType.METHOD)   只能加在方法上，类/字段都不行
 * @Retention(RUNTIME)          运行时保留，AOP 才能反射读到
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 每秒允许的请求数（QPS）
     * 例如 qps=1 表示每秒只放行 1 个请求；qps=5 表示每秒 5 个
     */
    double qps() default 1.0;

    /**
     * 等待令牌的超时时间（毫秒）
     * 默认 0 = 不等待，拿不到立即拒绝（推荐：接口限流场景不要阻塞）
     */
    int timeout() default 0;
}
