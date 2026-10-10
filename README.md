# 旅图足迹 - 旅游攻略分享平台

基于 Spring Boot 3 的旅游攻略分享社区，支持攻略发布、搜索、点赞收藏、评论互动、管理员审核、AI 智能问答、实时天气等功能。

## 技术栈

| 类别 | 技术 |
|------|------|
| 核心框架 | Spring Boot 3.1.5 + Java 17 |
| ORM | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8 |
| 缓存 | Redis + Spring Cache |
| 认证 | JWT + BCrypt 密码加密 |
| 文件存储 | 七牛云对象存储 |
| 第三方 API | 高德天气接口 |
| 接口文档 | SpringDoc OpenAPI (Swagger) |
| 限流 | AOP + Guava RateLimiter 令牌桶 |
| 前端 | 原生 HTML + Tailwind CSS (单页演示) |

## 功能模块

### 用户端
- ✅ 用户注册 / 登录（JWT 无状态认证）
- ✅ 攻略搜索（按城市精确匹配 / 景点模糊匹配）
- ✅ 攻略详情（含实时天气 + 出行提示）
- ✅ 分类 / 标签筛选 + 分页
- ✅ 热门攻略 / 轮播图推荐
- ✅ 点赞 / 收藏 / 二级评论
- ✅ 个人中心（我发布的 / 我点赞的 / 我收藏的）
- ✅ AI 旅游智能问答（交通/美食/住宿/门票等场景）

### 管理端
- ✅ 数据统计（用户数、攻略数、浏览量、热门 Top5）
- ✅ 用户管理（启用 / 禁用账号）
- ✅ 攻略管理（审核、上下线、删除）

## 项目亮点

1. **Redis 多级缓存 + 故障降级**
   - 使用 Spring Cache 注解（`@Cacheable` / `@CacheEvict`）缓存热门攻略、轮播图、城市列表
   - 实现 `CacheErrorHandler`，Redis 宕机时自动回源 MySQL，接口不 500
   - 禁用 null 缓存防止缓存穿透

2. **AOP 接口限流**
   - 基于 Guava `RateLimiter` 实现令牌桶限流
   - 自定义 `@RateLimit` 注解，发布接口限 1 QPS、点赞限 2 QPS
   - 每个方法独立限流器实例，`ConcurrentHashMap` 懒加载

3. **JWT 无状态认证**
   - 拦截器统一校验 Token，用户信息存入 ThreadLocal
   - 请求结束自动清理 ThreadLocal，防止线程池复用串用户

4. **并发安全的计数操作**
   - 点赞数 / 浏览量 / 评论数采用 SQL 原子递增（`setSql("like_count = like_count + 1")`）
   - 避免 Java 层 `get + set` 导致的并发丢失更新
   - 递减时用 `GREATEST(count - n, 0)` 防止负数

5. **天气服务本地缓存**
   - 高德天气接口结果本地缓存 30 分钟
   - 同城攻略只调用一次接口，批量回填
   - 接口失败降级返回友好默认值

## 项目结构

```
travel_backend/
├── src/main/java/com/travel/
│   ├── common/              # 通用模块
│   │   ├── annotation/      # 自定义注解 (@RateLimit)
│   │   ├── aspect/          # AOP 切面 (限流)
│   │   ├── config/          # 配置类 (Redis、CORS、MyBatis-Plus 等)
│   │   ├── exception/       # 全局异常处理
│   │   └── util/            # 工具类 (JWT、BCrypt、用户上下文)
│   ├── controller/          # 控制层
│   ├── domain/
│   │   ├── entity/          # 数据库实体
│   │   ├── dto/             # 数据传输对象
│   │   └── vo/              # 视图对象
│   ├── interceptor/         # JWT 拦截器
│   ├── mapper/              # MyBatis-Plus Mapper
│   └── service/             # 业务逻辑层
├── src/main/resources/
│   ├── application.yml      # 主配置
│   └── static/index.html    # 前端演示页
└── pom.xml
```

## 数据库表设计

| 表名 | 说明 |
|------|------|
| `user` | 用户表（含 isAdmin 管理员标识） |
| `travel_strategy` | 攻略表（城市/景点/内容/浏览量/状态） |
| `travel_like` | 点赞记录表（user_id + strategy_id 唯一索引） |
| `user_collect` | 收藏记录表 |
| `travel_comment` | 评论表（parent_id 支持二级回复） |
| `ai_chat` | AI 问答记录表 |

## 快速启动

### 1. 环境准备
- JDK 17
- MySQL 8.x
- Redis 6+（可选，未配置时自动降级回源数据库）

### 2. 导入数据库

```bash
mysql -uroot -p < travel_map.sql
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改以下内容：

```yaml
spring:
  datasource:
    username: root
    password: 你的MySQL密码    # 改成你的密码
  data:
    redis:
      host: 127.0.0.1          # Redis 地址
      port: 6379
```

> ⚠️ 七牛云 / 高德天气 / JWT 密钥建议通过环境变量注入，不要硬编码提交到仓库。

### 4. 启动项目

```bash
cd Yukc_maven_travel/travel_backend
mvn spring-boot:run
```

### 5. 验证

- 接口文档：http://localhost:8080/swagger-ui.html
- 前端页面：http://localhost:8080/index.html
- 攻略列表接口：http://localhost:8080/strategy/list

## 接口概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户 | POST | /user/register | 注册 |
| 用户 | POST | /user/login | 登录 |
| 用户 | GET | /user/center/* | 个人中心（我的发布/点赞/收藏/评论） |
| 攻略 | GET | /strategy/search | 关键词搜索 |
| 攻略 | GET | /strategy/detail/{id} | 攻略详情 |
| 攻略 | GET | /strategy/page | 分页列表 |
| 攻略 | GET | /strategy/filter | 分类/标签筛选 |
| 攻略 | POST | /strategy/publish | 发布攻略 |
| 攻略 | POST | /strategy/like/{id} | 点赞切换 |
| 攻略 | POST | /strategy/collect/{id} | 收藏切换 |
| 攻略 | POST | /strategy/comment | 发表评论 |
| 攻略 | GET | /strategy/comment/list/{id} | 评论列表 |
| 问答 | POST | /ai/ask | AI 智能问答 |
| 后台 | GET | /admin/stats | 数据统计 |
| 后台 | PUT | /admin/strategies/{id}/audit | 审核攻略 |
| 文件 | POST | /file/upload | 上传图片 |

> 完整接口请访问 Swagger 文档。