package com.travel.common.aspect;

import com.google.common.util.concurrent.RateLimiter;
import com.travel.common.annotation.RateLimit;
import com.travel.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 接口限流切面（AOP）
 *
 * 【核心原理】
 * Spring 启动时扫描所有 Bean，发现这个切面后注册为 AOP 代理工厂。
 * 当外部调用某个带 @RateLimit 的方法时，Spring 自动生成代理对象拦截调用，
 * 在方法执行前先走我们的 around() 逻辑：取令牌→放行，或没令牌→拒绝。
 *
 * 【RateLimiter 实例管理】
 * 每个加注解的方法对应一个独立的 RateLimiter 实例（否则不同接口会共享令牌桶互相影响）。
 * 用 ConcurrentHashMap 缓存，key = "类名#方法名"，保证线程安全 + 懒加载。
 *
 * 【为什么不用 @PostConstruct 启动时扫描所有方法】
 * 懒加载更简单：第一次调用才创建，无业务调用的方法不占内存。
 *
 * 【自调用失效提醒】
 * AOP 基于代理，同类内部 this.xxx() 调用不走代理，注解不生效。
 * 必须是"外部调用"（Controller → Service 跨 Bean）才生效。
 */
@Slf4j
@Aspect       // 声明为切面
@Component    // 注册到 Spring 容器
public class RateLimitAspect {

    /**
     * RateLimiter 实例缓存
     * key：方法唯一标识（类名#方法名）
     * value：该方法对应的令牌桶
     *
     * ConcurrentHashMap 保证多线程并发初始化时不会重复创建
     * （computeIfAbsent 内部用 CAS 保证原子性）
     */
    private final ConcurrentHashMap<String, RateLimiter> limiterMap = new ConcurrentHashMap<>();

    /**
     * 环绕通知：拦截所有带 @RateLimit 注解的方法
     *
     * 【@Around 执行流程】
     * 1. 切面拿到 ProceedingJoinPoint（能控制是否执行原方法）
     * 2. 方法执行前：取令牌
     *    - 拿到 → joinPoint.proceed() 放行原方法
     *    - 拿不到 → 抛 BusinessException，原方法不执行
     *
     * 【切点表达式 @annotation(rateLimit)】
     * 这是一种简写：绑定方法上的注解对象到参数 rateLimit，
     * 这样在切面里能直接读到 qps、timeout 等属性值。
     */
    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 1. 生成方法唯一标识：类名#方法名
        String methodName = joinPoint.getSignature().getDeclaringTypeName()
                + "#" + joinPoint.getSignature().getName();

        // 2. 懒加载获取（或创建）该方法对应的 RateLimiter
        //    computeIfAbsent：key 不存在时执行 lambda 创建并 put，已存在直接返回
        RateLimiter limiter = limiterMap.computeIfAbsent(methodName, k -> {
            log.info("初始化限流器：{}，QPS={}", k, rateLimit.qps());
            return RateLimiter.create(rateLimit.qps());
        });

        // 3. 尝试获取令牌
        //    tryAcquire(timeout, unit)：在 timeout 时间内尝试拿令牌，拿到返回 true
        //    timeout=0 时立即返回，不阻塞（推荐：接口限流不要阻塞线程）
        boolean acquired;
        if (rateLimit.timeout() > 0) {
            acquired = limiter.tryAcquire(rateLimit.timeout(), java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            acquired = limiter.tryAcquire();  // 立即返回，不等待
        }

        // 4. 拿不到令牌 → 抛异常，由 GlobalExceptionHandler 统一返回 429
        if (!acquired) {
            log.warn("接口限流触发：{}，当前 QPS 超过 {}", methodName, rateLimit.qps());
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        // 5. 拿到令牌 → 放行原方法
        return joinPoint.proceed();
    }
}
