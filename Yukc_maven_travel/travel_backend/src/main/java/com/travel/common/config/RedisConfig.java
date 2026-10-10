package com.travel.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

/**
 * Redis 配置类
 *
 * 【整体定位】
 * Spring Boot 提供了 Redis 自动配置，但默认的 RedisTemplate 用 JDK 序列化（二进制），
 * redis-cli 里看 key 是 \xAC\xED... 乱码，无法阅读，所以必须手动覆盖配置。
 *
 * 【两个核心 Bean 的分工】
 * 1. RedisTemplate：手动操作 Redis 时用（如 redisTemplate.opsForValue().set(k, v)），
 *    后续做接口限流、分布式锁会用到。
 * 2. RedisCacheManager：给 @Cacheable / @CacheEvict / @CachePut 注解用，
 *    Spring Cache 框架通过它来读写 Redis。
 *    两条路各自需要序列化配置，所以 ObjectMapper 配置写了两遍。
 *
 * 【AOP 代理原理】
 * @EnableCaching 是总开关。开启后 Spring 启动时会扫描所有 Bean，
 * 发现方法上有缓存注解，就给这个类生成代理对象包在外面。
 * 调用方拿到的是代理，调用方法时代理先查 Redis：
 *   - 命中 → 直接返回，原方法体根本不执行
 *   - 未命中 → 调用原方法体 → 结果写入 Redis → 返回
 * 由此推出一个坑：同类内部 this.xxx() 调用不走代理，缓存注解失效。
 *
 * 【缓存三大问题对应配置】
 * - 缓存穿透：disableCachingNullValues() 不缓存 null，避免恶意查询数据库不存在的数据
 * - 缓存击穿：可给 @Cacheable 加 sync=true 单线程重建（项目暂未用）
 * - 缓存雪崩：目前统一 TTL 30 分钟，进阶方案是 TTL 加随机值
 */
@Slf4j
@Configuration
@EnableCaching  // 总开关：没有这行，所有 @Cacheable / @CacheEvict 注解全部失效
// 实现 CachingConfigurer 是为了重写 errorHandler()：Redis 故障时降级回源数据库，而不是接口直接 500
public class RedisConfig implements CachingConfigurer {

    /**
     * 自定义 RedisTemplate，key 用字符串序列化，value 用 JSON 序列化
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // ============ value 序列化器：Jackson JSON ============
        ObjectMapper om = new ObjectMapper();

        // ① 可见性：ALL 表示序列化所有成员（field/get/set，ANY 包括 private）
        //    不设的话 private 字段可能序列化不出来，存进去的 JSON 是空的 {}
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // ② 序列化时把对象的类名也写进 JSON（多一个 "@class" 属性）
        //    例如存 List<Strategy>，JSON 会记录 "java.util.ArrayList" 和 "com.travel.domain.entity.Strategy"
        //    反序列化时 Jackson 才知道要还原成什么类型，否则只能还原成 LinkedHashMap，强转就报错
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);

        // ③ 注册 Java 8 时间模块：支持 LocalDateTime / LocalDate
        //    不注册的话遇到 Strategy.createTime 直接抛异常：
        //    "Java 8 date/time type not supported by default" → 接口 500
        //    （昨天踩的坑就是漏了这行）
        om.registerModule(new JavaTimeModule());

        // ④ 时间序列化成 "2026-09-16T21:00:00" 这种可读字符串，而不是 [2026,9,16,21,0,0] 数字数组
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Spring Data Redis 3.x 推荐写法：ObjectMapper 直接通过构造器传入
        // （旧的 setObjectMapper() 方法已标记 @Deprecated）
        Jackson2JsonRedisSerializer<Object> jacksonSerializer =
                new Jackson2JsonRedisSerializer<>(om, Object.class);

        // 字符串序列化器（key 用这个）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // key / hashKey 用字符串
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // value / hashValue 用 JSON
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Spring Cache 的 Redis 缓存管理器
     * 给 @Cacheable 等注解使用，统一设置过期时间
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        // 缓存管理器也要用支持 LocalDateTime 的 Jackson 序列化器
        ObjectMapper cacheOm = new ObjectMapper();
        cacheOm.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        cacheOm.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        cacheOm.registerModule(new JavaTimeModule());
        cacheOm.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Jackson2JsonRedisSerializer<Object> cacheJacksonSerializer =
                new Jackson2JsonRedisSerializer<>(cacheOm, Object.class);

        // ============ Spring Cache 全局规则配置 ============
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // ① 缓存默认过期时间：30 分钟（防雪崩进阶方案：TTL + 随机值）
                .entryTtl(Duration.ofMinutes(30))
                // ② key 前缀规则：cacheName + "::"
                //    决定了 @Cacheable(value="strategy:hot", key="#limit") 最终的 Redis key 是 strategy:hot::5
                //    结构：cacheName + :: + SpEL 计算结果
                .computePrefixWith(cacheName -> cacheName + "::")
                // ③ key 用字符串序列化（和 RedisTemplate 保持一致）
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                // ④ value 用 JSON 序列化（支持 LocalDateTime）
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(cacheJacksonSerializer))
                // ⑤ 不缓存 null 值：方法返回 null 时直接抛异常而不是存进 Redis
                //    防止缓存穿透（恶意查询数据库不存在的数据导致缓存永远不命中）
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 自定义缓存异常处理器 —— Redis 故障降级
     *
     * 【默认行为的问题】
     * Spring Cache 默认使用 SimpleCacheErrorHandler，Redis 一抛异常就直接往外抛。
     * 而缓存读取发生在方法执行之前，异常一抛，@Cacheable 方法体根本没机会执行，
     * 接口直接 500 —— Redis 从"加速层"变成了"单点依赖"。
     *
     * 【降级策略】四类异常只记日志、不外抛：
     * - handleCacheGetError：当作"缓存未命中"，Spring 继续执行原方法，回源 MySQL
     * - handleCachePutError：数据库查到的数据正常返回，只是没能写进缓存
     * - handleCacheEvictError：增删改正常提交，只是旧缓存没删掉（Redis 恢复后 TTL 到期也会刷新）
     * - handleCacheClearError：同上，清空失败不影响业务
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("[缓存降级] Redis 读取失败，回源数据库 | cache={}, key={}, 原因={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("[缓存降级] Redis 写入失败，不影响本次返回 | cache={}, key={}, 原因={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("[缓存降级] Redis 淘汰失败，数据已正常提交 | cache={}, key={}, 原因={}",
                        cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("[缓存降级] Redis 清空失败 | cache={}, 原因={}",
                        cache.getName(), exception.getMessage());
            }
        };
    }
}
