# SwapU 校园二手交易平台 — Code Wiki

> 基于大模型（RAG架构）与微服务中间件体系的现代化校园二手物品交易平台

---

## 目录

- [1. 项目概览](#1-项目概览)
- [2. 技术栈总览](#2-技术栈总览)
- [3. 项目目录结构](#3-项目目录结构)
- [4. 系统架构设计](#4-系统架构设计)
- [5. 后端模块详解](#5-后端模块详解)
  - [5.1 启动入口与全局配置](#51-启动入口与全局配置)
  - [5.2 Controller 层（API 接口）](#52-controller-层api-接口)
  - [5.3 Service 层（业务逻辑）](#53-service-层业务逻辑)
  - [5.4 Entity 层（数据模型）](#54-entity-层数据模型)
  - [5.5 Mapper 层（数据访问）](#55-mapper-层数据访问)
  - [5.6 Config 层（配置类）](#56-config-层配置类)
  - [5.7 拦截器与鉴权](#57-拦截器与鉴权)
  - [5.8 消息队列监听器](#58-消息队列监听器)
  - [5.9 工具类](#59-工具类)
  - [5.10 异常处理与AOP](#510-异常处理与aop)
  - [5.11 WebSocket 实时通讯](#511-websocket-实时通讯)
- [6. 前端模块详解](#6-前端模块详解)
  - [6.1 技术架构](#61-技术架构)
  - [6.2 路由设计](#62-路由设计)
  - [6.3 状态管理](#63-状态管理)
  - [6.4 页面组件](#64-页面组件)
  - [6.5 工具函数](#65-工具函数)
- [7. 数据库设计](#7-数据库设计)
- [8. 中间件与基础设施](#8-中间件与基础设施)
- [9. 核心业务流程](#9-核心业务流程)
- [10. 部署与运行](#10-部署与运行)
- [11. 依赖关系图](#11-依赖关系图)

---

## 1. 项目概览

SwapU 是一个面向校园场景的二手物品交易平台，核心定位：

| 维度 | 说明 |
|------|------|
| **目标用户** | 校园学生群体 |
| **核心功能** | 商品发布、搜索、购买、即时聊天、订单管理 |
| **AI 特性** | RAG 智能导购助手 + AI 一键文案生成 + AI 内容审核 |
| **架构特色** | 分布式锁防超卖、延迟消息自动取消订单、ES 全文检索、PgVector 向量检索 |

---

## 2. 技术栈总览

### 后端

| 类别 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 3.2.4 |
| JDK | OpenJDK | 17 |
| ORM | MyBatis-Plus | 3.5.5 |
| 关系数据库 | MySQL | 8.0 |
| 向量数据库 | PostgreSQL + PgVector | — |
| 缓存 | Redis + Lettuce | — |
| 分布式锁 | Redisson | 3.27.2 |
| 消息队列 | RocketMQ | 5.x (starter 2.3.0) |
| 搜索引擎 | Elasticsearch | 8.x |
| AI 框架 | Spring AI | 0.8.1 |
| 大模型 | DeepSeek (OpenAI 兼容) | deepseek-chat |
| Embedding | Ollama (bge-m3) | — |
| 支付 | 支付宝沙箱 SDK | 4.38.200 |
| JWT | jjwt | 0.12.5 |
| JSON | Fastjson 2 | 2.0.47 |
| 实时通讯 | WebSocket (Jakarta) | — |
| 加密 | Spring Security Crypto (BCrypt) | — |
| 构建工具 | Maven | — |

### 前端

| 类别 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Vue 3 (Composition API) | 3.4.21 |
| 构建工具 | Vite | 5.2.0 |
| 状态管理 | Pinia | 2.1.7 |
| 路由 | Vue Router | 4.3.0 |
| UI 组件库 | Element Plus | 2.6.1 |
| HTTP 客户端 | Axios | 1.6.8 |
| 图表 | ECharts | 6.0.0 |
| 日期处理 | Day.js | 1.11.10 |
| CSS 预处理 | Sass | 1.72.0 |

---

## 3. 项目目录结构

```
SwapU/
├── backend/                          # 后端 Spring Boot 项目
│   ├── src/main/java/com/swapu/
│   │   ├── SwapUApplication.java     # 启动入口
│   │   ├── aspect/                   # AOP 切面（全局日志）
│   │   ├── common/                   # 通用类（Result 响应封装、校验分组）
│   │   ├── config/                   # 配置类（数据源、中间件、Web）
│   │   ├── controller/               # REST 控制器
│   │   │   └── ws/                   # WebSocket 端点
│   │   ├── entity/                   # 实体类 / DTO / VO
│   │   │   ├── es/                   # Elasticsearch 文档模型
│   │   │   └── mq/                   # 消息队列消息体
│   │   ├── exception/                # 异常定义与全局处理
│   │   ├── handler/                  # MyBatis TypeHandler
│   │   ├── interceptor/              # HTTP 拦截器（登录/管理员鉴权）
│   │   ├── listener/                 # MQ 消息监听器
│   │   ├── mapper/                   # MyBatis-Plus Mapper 接口
│   │   ├── repository/               # Spring Data ES Repository
│   │   ├── service/                  # Service 接口
│   │   │   └── impl/                 # Service 实现
│   │   └── utils/                    # 工具类（JWT、AES、DeepSeek）
│   ├── src/main/resources/
│   │   ├── application.yml           # 主配置文件
│   │   ├── application-prod.yml      # 生产环境配置
│   │   └── logback-spring.xml        # 日志配置
│   ├── src/test/                     # 单元测试
│   ├── Dockerfile                    # 后端 Docker 镜像构建
│   └── pom.xml                       # Maven 依赖
├── frontend/                         # 前端 Vue 3 项目
│   ├── src/
│   │   ├── main.js                   # 应用入口
│   │   ├── App.vue                   # 根组件
│   │   ├── router/index.js           # 路由配置
│   │   ├── stores/user.js            # Pinia 用户状态
│   │   ├── utils/                    # 工具函数（request、format）
│   │   └── views/                    # 页面组件
│   │       └── admin/                # 管理员页面
│   ├── Dockerfile                    # 前端 Docker 镜像构建
│   ├── nginx.conf                    # Nginx 反向代理配置
│   ├── vite.config.js                # Vite 构建配置
│   └── package.json                  # NPM 依赖
├── database/                         # 数据库脚本
│   ├── init.sql                      # MySQL 建表与初始数据
│   ├── pgvector_init.sql             # PgVector 初始化
│   └── mock_data*.sql                # 模拟数据
├── docker/                           # Docker 辅助配置
│   ├── elasticsearch/Dockerfile      # ES + IK 分词器
│   └── rocketmq/broker.conf          # RocketMQ Broker 配置
├── docs/                             # 项目文档
│   ├── api/                          # API 文档
│   ├── architecture/                 # 架构设计文档
│   ├── deployment/                   # 部署文档
│   └── requirements/                 # 需求文档
└── uploads/                          # 用户上传文件存储目录
```

---

## 4. 系统架构设计

### 4.1 整体架构图

```
┌──────────────────────────────────────────────────────────────────┐
│                        客户端 (Browser)                          │
│              Vue 3 + Element Plus + Pinia + Axios               │
└────────────────────────┬─────────────────────────────────────────┘
                         │ HTTP / WebSocket
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                     Nginx (反向代理 / 静态资源)                    │
│   /api/* ──► 后端 Spring Boot                                    │
│   /pay/* ──► 后端 Spring Boot                                    │
│   /files/* ──► 后端静态资源                                       │
│   其他 ──► 前端 SPA                                              │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                  Spring Boot 3.2 (Port 8080)                     │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │
│  │Controller │ │Interceptor│ │  AOP     │ │ WebSocket Endpoint│   │
│  │   层      │ │  鉴权层   │ │  日志    │ │   实时通讯        │   │
│  └────┬─────┘ └──────────┘ └──────────┘ └──────────────────┘   │
│       │                                                         │
│  ┌────▼─────┐                                                   │
│  │ Service  │ ←──── 事务管理 / 缓存注解                          │
│  │   层     │                                                   │
│  └────┬─────┘                                                   │
│       │                                                         │
│  ┌────▼──────────────────────────────────────────────────────┐  │
│  │                    数据访问层                               │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐  │  │
│  │  │MyBatis-  │ │Spring    │ │Spring AI │ │  Redisson   │  │  │
│  │  │Plus      │ │Data ES   │ │PgVector  │ │  分布式锁   │  │  │
│  │  │(MySQL)   │ │(ES)      │ │(PG)      │ │  (Redis)    │  │  │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └──────┬─────┘  │  │
│  └───────┼─────────────┼────────────┼──────────────┼────────┘  │
└──────────┼─────────────┼────────────┼──────────────┼────────────┘
           │             │            │              │
     ┌─────▼──┐   ┌──────▼──┐  ┌─────▼─────┐  ┌────▼────┐
     │ MySQL  │   │  ES 8.x │  │PgVector   │  │  Redis  │
     │ 8.0    │   │ +IK分词  │  │(PostgreSQL)│  │         │
     └────────┘   └─────────┘  └───────────┘  └─────────┘

     ┌──────────────────┐   ┌──────────────────────────┐
     │   RocketMQ 5.x   │   │  DeepSeek / Ollama API   │
     │ (延迟消息/数据同步) │   │  (大模型推理/Embedding)   │
     └──────────────────┘   └──────────────────────────┘

     ┌──────────────────┐
     │  支付宝沙箱环境    │
     │  (当面付)         │
     └──────────────────┘
```

### 4.2 核心架构亮点

| 架构特性 | 实现方式 | 说明 |
|----------|----------|------|
| **RAG 智能导购** | Spring AI + PgVector + DeepSeek | 用户自然语言 → 向量检索 → 上下文注入 → LLM 推荐 |
| **AI 文案生成** | DeepSeek (OpenAI 兼容接口) | 商品标题+描述 → LLM 润色 → 500字内精炼文案 |
| **AI 内容审核** | DeepSeek | 发布商品时自动审核合规性，违规拦截 |
| **防超卖** | Redisson 分布式锁 | `item:lock:{itemId}` 锁粒度，tryLock(3s, 10s) |
| **延迟取消订单** | RocketMQ 延迟消息 (Level 16 = 30min) | 创建订单时发送延迟消息，超时未支付自动取消 |
| **ES 全文检索** | Elasticsearch + IK 分词 | 分词匹配 + 通配符查询 + 前缀补全 + 高亮 |
| **实时聊天** | WebSocket + JWT 鉴权 | ConcurrentHashMap 管理在线连接，消息持久化到 MySQL |
| **下单限流** | Redisson RateLimiter | 每用户每分钟最多 3 次下单请求 |
| **双数据源** | MySQL (主) + PostgreSQL/PgVector (向量) | 手动配置 PgVector JdbcTemplate，避免干扰 MyBatis-Plus |

---

## 5. 后端模块详解

### 5.1 启动入口与全局配置

**[SwapUApplication.java](backend/src/main/java/com/swapu/SwapUApplication.java)**

```java
@SpringBootApplication
@EnableCaching
@MapperScan("com.swapu.mapper")
public class SwapUApplication { ... }
```

| 注解 | 作用 |
|------|------|
| `@SpringBootApplication` | Spring Boot 自动配置入口 |
| `@EnableCaching` | 启用 Spring 缓存（Redis） |
| `@MapperScan("com.swapu.mapper")` | 扫描 MyBatis Mapper 接口 |

**[application.yml](backend/src/main/resources/application.yml)** — 核心配置项：

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `server.port` | 8080 | 后端服务端口 |
| `spring.datasource` | MySQL (swapu) | 主数据源 |
| `spring.pgvector.datasource` | PostgreSQL | 向量数据库数据源 |
| `spring.ai.openai` | DeepSeek API | 大模型对话接口 |
| `spring.ai.ollama` | bge-m3:latest | Embedding 模型 |
| `spring.data.redis` | Lettuce 连接池 | 缓存与分布式锁 |
| `spring.elasticsearch` | ES 8.x | 全文检索 |
| `rocketmq.name-server` | 9876 | 消息队列 |
| `file.upload-path` | D:/SwapU/uploads/ | 文件上传路径 |
| `alipay.*` | 沙箱配置 | 支付宝沙箱支付 |

---

### 5.2 Controller 层（API 接口）

#### UserController — `/user`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/user/login` | 用户登录，返回 JWT Token | 否 |
| POST | `/user/register` | 用户注册 | 否 |
| PUT | `/user/profile` | 修改个人信息 | 是 |
| PUT | `/user/password` | 修改密码 | 是 |
| GET | `/user/info` | 获取当前用户信息 | 是 |
| GET | `/user/info/{id}` | 获取指定用户公开信息 | 是 |

#### ItemController — `/item`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/item/publish` | 发布物品（含 AI 审核） | 是 |
| GET | `/item/list` | 物品列表（多条件筛选） | 否 |
| GET | `/item/search` | ES 全文搜索（分页+高亮） | 否 |
| GET | `/item/suggest` | 搜索前缀补全 | 否 |
| GET | `/item/detail/{id}` | 物品详情（含浏览量+1） | 否 |
| GET | `/item/my/publish` | 我发布的物品 | 是 |
| GET | `/item/my/want` | 我收藏的物品 | 是 |
| POST | `/item/status/{itemId}/{status}` | 更新物品状态 | 是 |
| DELETE | `/item/{itemId}` | 删除物品 | 是 |
| GET | `/item/user/{userId}/selling` | 用户在售物品（公开） | 否 |
| POST | `/item/update` | 编辑物品 | 是 |

#### ChatController — `/ai`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/ai/polish` | AI 文案润色 | 否 |
| POST | `/ai/chat` | AI 智能导购对话（RAG） | 否 |

**AI 对话流程**：
1. 接收用户消息 + 历史对话
2. 通过 `RagService` 在 PgVector 中检索 Top-3 相关商品
3. 将商品上下文注入 System Prompt
4. 拼接最近 6 条历史记录 + 当前消息
5. 调用 DeepSeek 大模型推理
6. 返回 AI 文本回复 + 商品卡片数据

#### TradeOrderController — `/order`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/order/create` | 创建订单（含限流+分布式锁） | 是 |
| POST | `/order/confirm/{orderId}` | 卖家确认订单 | 是 |
| POST | `/order/cancel/{orderId}` | 取消订单 | 是 |
| POST | `/order/complete/{orderId}` | 买家确认收货 | 是 |
| GET | `/order/list/{type}` | 我的订单列表 (1=买入, 2=卖出) | 是 |
| GET | `/order/detail/{orderId}` | 订单详情 | 是 |

#### PaymentController — `/pay`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/pay/alipay` | 发起支付宝支付（返回支付表单） | 否 |
| GET | `/pay/return` | 支付宝同步回调（验签+跳转） | 否 |
| POST | `/pay/notify` | 支付宝异步回调（验签+更新订单） | 否 |

#### AdminController — `/admin`

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| GET | `/admin/dashboard` | 管理员数据看板统计 | 管理员 |
| GET | `/admin/users` | 用户列表（分页） | 管理员 |
| PUT | `/admin/user/status` | 修改用户状态 | 管理员 |
| GET | `/admin/items` | 物品列表（分页） | 管理员 |
| PUT | `/admin/item/status` | 修改物品状态 | 管理员 |
| GET | `/admin/reports` | 举报列表 | 管理员 |
| PUT | `/admin/report/handle` | 处理举报 | 管理员 |

#### 其他 Controller

| Controller | 路径前缀 | 说明 |
|------------|----------|------|
| `CommentController` | `/comment` | 评价：发表评价、获取订单评价、物品评价列表 |
| `CommonController` | `/common` | 通用：文件上传 |
| `ItemCategoryController` | `/category` | 分类：列表、新增、修改、删除 |
| `ItemWantController` | `/want` | 收藏：添加/取消收藏、收藏列表 |
| `ItemMessageController` | `/item-message` | 物品留言板：发表留言、留言列表 |
| `ReportController` | `/report` | 举报：提交举报、举报列表 |
| `SysAnnouncementController` | `/announcement` | 公告：列表、新增、修改、删除 |
| `SysNotificationController` | `/notification` | 通知：未读数、列表、标记已读 |
| `ChatMessageController` | `/chat-message` | 聊天：历史消息、未读数、标记已读 |

---

### 5.3 Service 层（业务逻辑）

#### ItemService / ItemServiceImpl

核心业务类，负责物品全生命周期管理。

| 方法 | 说明 | 关键技术 |
|------|------|----------|
| `publish(Item)` | 发布物品 | AI 审核 → 入库 → 异步 RAG 向量化 → MQ 同步 ES |
| `listItemsWithFilters(...)` | 多条件筛选列表 | MyBatis-Plus QueryWrapper 动态条件 |
| `search(keyword, categoryId, page, size)` | ES 全文搜索 | NativeQuery + bool + match + wildcard + 高亮 |
| `suggest(keyword)` | 搜索补全 | match_phrase_prefix 查询 |
| `getItemDetail(id)` | 物品详情 | 浏览量 +1，填充发布者信息 |
| `updateStatus(userId, itemId, status)` | 更新状态 | 状态变更后从 ES 删除 |
| `deleteItem(userId, itemId)` | 删除物品 | 同步删除 ES 文档 |
| `updateItem(Item)` | 编辑物品 | 修改后重置为待审核，删除 ES 文档 |
| `syncToEs(itemId)` | 同步到 ES | 通过 RocketMQ 异步同步 |
| `syncAllToEs()` | 全量同步 | 启动时批量同步 ES + 向量库 |

**发布流程**：
```
用户提交 → AI内容审核(DeepSeek) → 违规则拦截(status=4) → 通过则入库(status=1)
  → 异步写入PgVector(CompletableFuture) → MQ消息同步到ES
```

#### TradeOrderService / TradeOrderServiceImpl

| 方法 | 说明 | 关键技术 |
|------|------|----------|
| `createOrder(TradeOrder)` | 创建订单 | Redisson 分布式锁 + 编程式事务 + MQ 延迟消息 |
| `confirmOrder(userId, orderId)` | 卖家确认 | 线下交易直接完成，线上交易变更为"进行中" |
| `cancelOrder(userId, orderId)` | 取消订单 | 恢复物品状态为在售 + 通知对方 |
| `completeOrder(userId, orderId)` | 确认收货 | 买家确认 → 物品状态变为已售出 |
| `paySuccess(orderNo, tradeNo, payTime)` | 支付成功回调 | 幂等性检查 + 更新支付状态 |
| `listMyOrders(userId, type)` | 订单列表 | N+1 优化：批量查询物品和用户信息 |
| `getOrderStatistics(days)` | 订单统计 | 按天统计最近 N 天订单数 |

**创建订单流程**：
```
获取分布式锁(item:lock:{itemId}) → 检查物品状态 → 校验金额 → 填充订单 → 保存
  → 物品状态改为"交易中" → 通知卖家 → 发送30分钟延迟消息 → 释放锁
```

#### RagService / RagServiceImpl

| 方法 | 说明 |
|------|------|
| `addDocument(Item)` | 将商品标题+描述向量化存入 PgVector |
| `retrieveRelevantItems(query, topK)` | 向量相似度检索，返回文本内容 |
| `retrieveRelevantItemDetails(query, topK)` | 向量相似度检索，返回含元数据的完整详情 |

**RAG 检索流程**：
```
用户查询 → Ollama(bge-m3)生成向量 → PgVector HNSW 近似检索 → 返回 Top-K 文档
```

#### UserService / UserServiceImpl

| 方法 | 说明 |
|------|------|
| `login(LoginVO)` | 登录验证（BCrypt 密码校验 + JWT 生成） |
| `register(User)` | 注册（用户名/邮箱唯一性检查 + BCrypt 加密） |
| `changePassword(ChangePasswordDTO)` | 修改密码 |

#### 其他 Service

| Service | 说明 |
|---------|------|
| `CommentService` | 评价管理：发表评价、按订单/物品查询 |
| `ReportService` | 举报管理：提交举报、分页查询、处理举报 |
| `ItemCategoryService` | 物品分类 CRUD |
| `ItemWantService` | 收藏/想要：添加、取消、列表 |
| `ItemMessageService` | 物品留言板：发表留言（支持回复）、列表查询 |
| `ChatMessageService` | 聊天消息：历史记录、未读数、标记已读 |
| `SearchHistoryService` | 搜索历史：记录搜索关键词及频次 |
| `SysNotificationService` | 系统通知：发送通知、未读数、标记已读 |
| `SysAnnouncementService` | 系统公告 CRUD |

---

### 5.4 Entity 层（数据模型）

#### 核心实体

| 实体类 | 对应表 | 说明 |
|--------|--------|------|
| `User` | `sys_user` | 用户（含角色、信用分、联系方式） |
| `Item` | `item` | 物品（含状态机：0待审核→1在售→2交易中→3已售出→4下架） |
| `TradeOrder` | `trade_order` | 交易订单（含支付状态、交易方式） |
| `Comment` | `comment` | 评价（1-5星评分） |
| `Report` | `report` | 举报投诉 |
| `ItemCategory` | `item_category` | 物品分类 |
| `ItemWant` | `item_want` | 收藏/想要 |
| `ChatMessage` | `chat_message` | 私信聊天记录 |
| `ItemMessage` | `item_message` | 物品留言板（支持嵌套回复） |
| `SysNotification` | `sys_notification` | 系统通知 |
| `SysAnnouncement` | `sys_announcement` | 系统公告 |
| `SearchHistory` | `search_history` | 搜索历史 |

#### DTO / VO

| 类名 | 说明 |
|------|------|
| `LoginVO` | 登录请求体（username + password） |
| `ChangePasswordDTO` | 修改密码请求体 |

#### ES 文档模型

| 类名 | 说明 |
|------|------|
| `ItemDoc` | Elasticsearch 索引文档，字段与 Item 对应 |

#### MQ 消息体

| 类名 | 说明 |
|------|------|
| `ItemSyncMessage` | 物品同步消息（itemId + type: 1=上架, 2=删除） |
| `OrderTimeoutMessage` | 订单超时消息（orderId） |

---

### 5.5 Mapper 层（数据访问）

所有 Mapper 接口继承 `BaseMapper<T>`（MyBatis-Plus），提供标准 CRUD。

| Mapper | 对应实体 |
|--------|----------|
| `UserMapper` | User |
| `ItemMapper` | Item |
| `ItemCategoryMapper` | ItemCategory |
| `ItemWantMapper` | ItemWant |
| `TradeOrderMapper` | TradeOrder |
| `CommentMapper` | Comment |
| `ReportMapper` | Report |
| `ChatMessageMapper` | ChatMessage |
| `ItemMessageMapper` | ItemMessage |
| `SysNotificationMapper` | SysNotification |
| `SysAnnouncementMapper` | SysAnnouncement |
| `SearchHistoryMapper` | SearchHistory |

#### Spring Data ES Repository

| 接口 | 说明 |
|------|------|
| `ItemRepository` | 继承 `ElasticsearchRepository<ItemDoc, Long>`，提供 ES 文档 CRUD |

---

### 5.6 Config 层（配置类）

| 配置类 | 说明 |
|--------|------|
| `MybatisPlusConfig` | MyBatis-Plus 分页插件配置 |
| `WebConfig` | CORS 跨域、拦截器注册、静态资源映射（`/files/**` → uploads 目录） |
| `WebSocketConfig` | WebSocket 端点注册 |
| `PgVectorConfig` | 手动配置 PgVector 双数据源：`pgJdbcTemplate` + `pgVectorStore` Bean |
| `RedissonConfig` | Redisson 客户端配置 |
| `RestClientConfig` | REST 客户端配置 |
| `AlipayConfig` | 支付宝沙箱参数（AppID、密钥、回调地址等） |
| `StartupRunner` | 应用启动后执行：初始化 RocketMQ Topic、ES 数据同步 |

**PgVectorConfig 关键设计**：
- 不将 PostgreSQL DataSource 注册为 Spring Bean，避免干扰 MyBatis-Plus 默认数据源
- 手动创建 `DriverManagerDataSource` → `JdbcTemplate` → `PgVectorStore`
- 使用 `@Qualifier("ollamaEmbeddingClient")` 注入 Embedding 客户端

---

### 5.7 拦截器与鉴权

#### LoginInterceptor

- 从 `Authorization` 请求头获取 JWT Token
- 调用 `JwtUtils.validateToken()` 解析并验证
- 将 `userId`、`username`、`role` 存入 `HttpServletRequest` 属性
- 无效 Token 返回 HTTP 401

**放行路径**：
```
/user/login, /user/register, /item/list, /item/detail/**,
/item-message/list/**, /announcement/list, /ws/**, /ai/**,
/common/upload, /files/**, /pay/notify, /pay/return, /error
```

#### AdminInterceptor

- 检查 `request.getAttribute("role")` 是否为管理员 (1)
- 拦截 `/admin/**` 和分类/公告的写操作

---

### 5.8 消息队列监听器

#### ItemSyncListener

| 属性 | 值 |
|------|-----|
| Topic | `item-update-topic` |
| ConsumerGroup | `item-sync-consumer-group` |

| 消息类型 | 处理逻辑 |
|----------|----------|
| Type=1 (上架/更新) | 调用 `itemService.doSyncToEs()` → 写入 ES + PgVector |
| Type=2 (下架/删除) | 调用 `itemService.doDeleteFromEs()` → 从 ES 删除 |

#### OrderTimeoutListener

| 属性 | 值 |
|------|-----|
| Topic | `order-timeout-topic` |
| ConsumerGroup | `order-timeout-consumer-group` |

**处理逻辑**：检查订单是否仍未支付，若未支付则自动调用 `cancelOrder()` 取消订单并恢复物品状态。

---

### 5.9 工具类

#### JwtUtils

| 方法 | 说明 |
|------|------|
| `generateToken(userId, username, role)` | 生成 JWT（HMAC-SHA256，24h 过期） |
| `validateToken(token)` | 验证 Token，返回 Claims 或 null |
| `getUsername(token)` | 从 Token 提取用户名 |
| `getUserId(token)` | 从 Token 提取用户 ID |

**密钥**：`SwapU_Secret_Key_For_Campus_Trading_Platform_Needs_To_Be_Long_Enough`

#### AesEncryptUtils

AES 对称加密/解密工具，用于敏感字段（如手机号）的加密存储。

#### DeepSeekUtils

封装 DeepSeek API 调用，主要方法：
- `checkItemPublish(title, description)` — AI 内容合规审核

---

### 5.10 异常处理与AOP

#### CustomException

自定义业务异常类。

#### GlobalExceptionHandler

`@RestControllerAdvice` 全局异常处理器，捕获各类异常并返回统一的 `Result` 格式。

#### GlobalLogAspect

`@Aspect` 全局日志切面，记录：
- 请求参数
- 方法执行耗时
- 响应结果

#### AesEncryptTypeHandler

MyBatis TypeHandler，在数据库读写时自动进行 AES 加密/解密，用于 `phone` 等敏感字段。

#### Result

统一响应封装类：

```java
public class Result<T> {
    private Integer code;    // 200=成功, 其他=失败
    private String msg;      // 提示信息
    private T data;          // 数据
}
```

#### Groups

校验分组接口：`Groups.Create`、`Groups.Update`，用于区分新增和更新场景的参数校验规则。

---

### 5.11 WebSocket 实时通讯

**[ChatWebSocketEndpoint](backend/src/main/java/com/swapu/controller/ws/ChatWebSocketEndpoint.java)**

| 属性 | 值 |
|------|-----|
| 端点路径 | `/ws/chat/{userId}` |
| 鉴权方式 | URL 参数 `?token=xxx`，JWT 验证 |

**核心机制**：
- `ConcurrentHashMap<Long, ChatWebSocketEndpoint>` 管理在线用户连接
- `@OnOpen`：验证 Token → 注册连接 → 在线计数 +1
- `@OnMessage`：解析 JSON → 持久化到 MySQL → 转发给目标用户（在线直接推送，离线存库）
- `@OnClose`：移除连接 → 在线计数 -1
- 心跳机制：收到 `ping` 回复 `pong`

---

## 6. 前端模块详解

### 6.1 技术架构

```
Vue 3 (Composition API)
├── Vue Router 4 — 路由管理 + 权限守卫
├── Pinia — 状态管理
├── Element Plus — UI 组件库
├── Axios — HTTP 请求（封装拦截器）
├── ECharts — 图表（管理员看板）
├── Day.js — 日期格式化
└── Sass — CSS 预处理
```

### 6.2 路由设计

| 路径 | 组件 | 说明 | 需登录 | 需管理员 |
|------|------|------|--------|----------|
| `/` | Home | 首页（瀑布流商品列表） | 否 | 否 |
| `/login` | Login | 登录页 | 否 | 否 |
| `/register` | Register | 注册页 | 否 | 否 |
| `/ai-assistant` | AiAssistant | AI 智能导购 | 否 | 否 |
| `/profile` | Profile | 个人中心 | 是 | 否 |
| `/publish` | Publish | 发布物品 | 是 | 否 |
| `/search` | SearchResult | 搜索结果页 | 否 | 否 |
| `/chat` | Chat | 即时通讯 | 是 | 否 |
| `/item/:id` | ItemDetail | 物品详情 | 否 | 否 |
| `/user/:id` | SellerDetail | 卖家主页 | 否 | 否 |
| `/order/detail/:id` | OrderDetail | 订单详情 | 是 | 否 |
| `/order/list` | OrderList | 订单列表 | 是 | 否 |
| `/announcement/list` | AnnouncementList | 公告列表 | 否 | 否 |
| `/notification` | NotificationList | 通知列表 | 是 | 否 |
| `/admin` | AdminLayout | 管理后台布局 | 是 | 是 |
| `/admin/dashboard` | Dashboard | 数据看板 | 是 | 是 |
| `/admin/user` | UserManage | 用户管理 | 是 | 是 |
| `/admin/item` | ItemManage | 物品管理 | 是 | 是 |
| `/admin/report` | ReportManage | 举报管理 | 是 | 是 |
| `/admin/category` | CategoryManage | 分类管理 | 是 | 是 |
| `/admin/announcement` | AnnouncementManage | 公告管理 | 是 | 是 |

**路由守卫逻辑**：
- `requiresAuth` 路由需要 Token，否则跳转登录页
- `requiresAdmin` 路由需要 `role === 1`，否则跳转首页
- 已登录用户访问 `/login` 或 `/register` 时自动跳转（管理员→后台，普通用户→首页）
- 管理员访问 `/` 自动导向 `/admin`

### 6.3 状态管理

**[stores/user.js](frontend/src/stores/user.js)** — Pinia Store

| 状态/方法 | 说明 |
|-----------|------|
| `user` | 当前用户信息（从 localStorage 读取） |
| `token` | JWT Token |
| `login()` | 登录并持久化到 localStorage |
| `register()` | 注册 |
| `logout()` | 登出并清除 localStorage |

### 6.4 页面组件

| 组件 | 功能描述 |
|------|----------|
| `Home.vue` | 首页瀑布流、分类筛选、搜索入口 |
| `Login.vue` | 登录表单 |
| `Register.vue` | 注册表单 |
| `AiAssistant.vue` | AI 对话界面，支持多轮对话 + 商品卡片展示 |
| `Profile.vue` | 个人信息编辑、我发布的/收藏的物品、密码修改 |
| `Publish.vue` | 物品发布表单（含 AI 文案润色按钮） |
| `SearchResult.vue` | ES 搜索结果展示（高亮、分页） |
| `Chat.vue` | WebSocket 实时聊天界面 |
| `ItemDetail.vue` | 物品详情、留言板、收藏、下单 |
| `SellerDetail.vue` | 卖家信息及在售物品 |
| `OrderDetail.vue` | 订单详情、支付、确认收货 |
| `OrderList.vue` | 买入/卖出订单列表 |
| `NotificationList.vue` | 系统通知列表 |
| `AnnouncementList.vue` | 系统公告列表 |
| `AdminLayout.vue` | 管理后台侧边栏布局 |
| `Dashboard.vue` | 数据看板（ECharts 图表） |
| `UserManage.vue` | 用户管理（启用/禁用） |
| `ItemManage.vue` | 物品管理（审核/下架） |
| `ReportManage.vue` | 举报处理 |
| `CategoryManage.vue` | 分类管理 |
| `AnnouncementManage.vue` | 公告管理 |

### 6.5 工具函数

**[utils/request.js](frontend/src/utils/request.js)**

- Axios 实例封装
- 请求拦截器：自动添加 `Authorization` 头
- 响应拦截器：401 自动跳转登录页

**[utils/format.js](frontend/src/utils/format.js)**

| 函数 | 说明 |
|------|------|
| 图片路径处理 | 补全相对路径为完整 URL |
| 时间格式化 | 基于 Day.js |
| 订单状态文本 | 状态码 → 中文文本映射 |

---

## 7. 数据库设计

### 7.1 MySQL 表结构（主库 `swapu`）

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `sys_user` | 用户表 | user_id(PK), username(UK), password(BCrypt), role(0学生/1管理员), credit_score |
| `student_auth` | 学生认证表 | auth_id(PK), user_id(UK), student_no(UK), real_name, status(0待审/1通过/2驳回) |
| `item_category` | 物品分类表 | category_id(PK), category_name, sort_order |
| `item` | 物品表 | item_id(PK), user_id(FK), category_id(FK), title, description, price, images(JSON), status(0-4), view_count, want_count |
| `item_want` | 收藏表 | want_id(PK), user_id+item_id(UK) |
| `trade_order` | 交易订单表 | order_id(PK), order_no(UK), buyer_id, seller_id, item_id, amount, status(0-3), payment_status(0/1), trade_no |
| `comment` | 评价表 | comment_id(PK), order_id, item_id, user_id, target_user_id, rating(1-5) |
| `report` | 举报表 | report_id(PK), reporter_id, target_id, type(1违规/2纠纷), status(0-2) |
| `sys_notification` | 系统通知表 | notification_id(PK), user_id, type(1系统/2交易/3互动), is_read |
| `sys_announcement` | 系统公告表 | announcement_id(PK), title, content, status(0草稿/1发布) |
| `search_history` | 搜索历史表 | id(PK), keyword(UK), count |
| `chat_message` | 聊天记录表 | msg_id(PK), sender_id, receiver_id, content, msg_type(1文本/2图片/3卡片), is_read |
| `item_message` | 物品留言表 | message_id(PK), item_id, user_id, content, parent_id(回复), reply_to_user_id |

### 7.2 物品状态机

```
0(待审核) ──AI审核通过──► 1(在售) ──买家下单──► 2(交易中) ──确认收货──► 3(已售出)
   │                        │                      │
   │                   手动下架                  取消订单
   │                        │                      │
   └──────────────────────► 4(下架) ◄──────────────┘ (恢复为1)
```

### 7.3 订单状态机

```
0(待卖家确认) ──卖家确认──► 1(进行中) ──买家确认收货──► 2(已完成)
   │                        │
   │ 取消(买卖双方)         │ 取消(买卖双方)
   │                        │
   └──────────────────────► 3(已取消) ◄─────────────┘
```

### 7.4 PgVector 向量库

- 数据库：`postgres`（PostgreSQL）
- 扩展：`vector`
- 表：`vector_store`（Spring AI 自动创建）
- 索引：HNSW（近似最近邻）
- 距离度量：COSINE_DISTANCE
- 向量维度：1024（bge-m3）
- 文档内容：`商品名称: {title}\n商品描述: {description}`
- 元数据：itemId, title, price, description, images

---

## 8. 中间件与基础设施

### 8.1 Redis

| 用途 | Key 模式 | 说明 |
|------|----------|------|
| 分布式锁 | `item:lock:{itemId}` | 防超卖，tryLock(3s, 10s) |
| 下单限流 | `order:rate:{userId}` | 每用户每分钟 3 次 |
| Spring Cache | `item::*` | 物品列表/详情缓存 |
| Session | — | Lettuce 连接池 |

### 8.2 RocketMQ

| Topic | 生产者 | 消费者 | 说明 |
|-------|--------|--------|------|
| `item-update-topic` | ItemServiceImpl | ItemSyncListener | 物品数据同步到 ES（Type=1上架, Type=2删除） |
| `order-timeout-topic` | TradeOrderServiceImpl | OrderTimeoutListener | 30 分钟延迟消息，超时未支付自动取消 |

**延迟级别**：Level 16 = 30 分钟

### 8.3 Elasticsearch

| 配置 | 值 |
|------|-----|
| 版本 | 8.x |
| 分词器 | IK Analyzer (7.17.7) |
| 索引 | ItemDoc（字段：title, description, status, categoryId 等） |
| 查询类型 | match + wildcard + match_phrase_prefix + 高亮 |

### 8.4 支付宝沙箱

| 配置 | 说明 |
|------|------|
| 产品 | 当面付（PC 网页支付） |
| 回调 | 同步 `/pay/return` + 异步 `/pay/notify` |
| 验签 | RSA2 签名验证 |

---

## 9. 核心业务流程

### 9.1 商品发布流程

```
用户填写信息 → 点击发布
  → ItemController.publish()
    → ItemServiceImpl.publish()
      → DeepSeekUtils.checkItemPublish() [AI审核]
        → 违规: status=4, 返回拦截提示
        → 通过: status=1
      → save(item) [写入MySQL]
      → CompletableFuture.runAsync → RagService.addDocument() [异步写入PgVector]
      → syncToEs() → RocketMQ → ItemSyncListener → doSyncToEs() [异步写入ES]
```

### 9.2 下单支付流程

```
买家点击购买 → TradeOrderController.create()
  → Redisson RateLimiter [限流检查]
  → Redisson Lock(item:lock:{itemId}) [分布式锁]
    → TransactionTemplate.execute() [编程式事务]
      → 检查物品状态 + 校验金额
      → 保存订单 + 物品状态→2(交易中)
      → 发送通知给卖家
      → RocketMQ 延迟消息(30min) → OrderTimeoutListener
    → 释放锁

卖家确认 → confirmOrder() → 通知买家

买家支付 → PaymentController.pay/alipay → 支付宝沙箱
  → 异步回调 /pay/notify → paySuccess() → 更新支付状态

买家确认收货 → completeOrder() → 物品状态→3(已售出)

超时未支付 → OrderTimeoutListener.onMessage() → cancelOrder()
```

### 9.3 AI 智能导购流程

```
用户输入消息 → ChatController.chat()
  → RagService.retrieveRelevantItemDetails(query, 3) [PgVector向量检索]
  → 构建System Prompt + 注入商品上下文
  → 拼接历史对话(最近6条) + 当前消息
  → DeepSeek ChatClient.call() [大模型推理]
  → 返回 {text: AI回复, items: 商品卡片数据}
```

### 9.4 实时聊天流程

```
用户A连接WebSocket → /ws/chat/{userId}?token=xxx
  → ChatWebSocketEndpoint.onOpen() [JWT鉴权 + 注册连接]

用户A发送消息 → onMessage()
  → 解析JSON → ChatMessageMapper.insert() [持久化]
  → 检查用户B是否在线
    → 在线: webSocketMap.get(B).sendMessage() [实时推送]
    → 离线: 消息已存库，等B上线后拉取

心跳: 客户端定期发送 ping → 服务端回复 pong
```

---

## 10. 部署与运行

### 10.1 开发环境启动

**前置依赖**：

| 服务 | 版本 | 默认端口 |
|------|------|----------|
| JDK | 17+ | — |
| Node.js | 18+ | — |
| MySQL | 8.0 | 3306 |
| Redis | — | 6379 |
| RocketMQ | 5.x (NameServer + Broker) | 9876 |
| Elasticsearch | 8.x + IK 分词 | 9200 |
| PostgreSQL + PgVector | — | 5432 |
| Ollama | bge-m3 模型 | 11434 |

**后端启动**：

```bash
cd backend
# 1. 修改 application.yml 中的数据库/Redis/MQ/ES 等连接信息
# 2. 配置 Spring AI 的 api-key 或 Ollama 地址
mvn spring-boot:run
# 或直接运行 SwapUApplication.java
```

**前端启动**：

```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

**数据库初始化**：

```sql
-- MySQL
CREATE DATABASE swapu;
SOURCE database/init.sql;

-- PostgreSQL
CREATE EXTENSION vector;
-- 执行 database/pgvector_init.sql
```

### 10.2 Docker 部署

**后端 Dockerfile**：
- 基于 `eclipse-temurin:17-jre` 运行时镜像
- Maven 多阶段构建
- 暴露 8080 端口

**前端 Dockerfile**：
- Node.js 构建 → Nginx 运行
- Nginx 反向代理配置：
  - `/api/` → 后端服务
  - `/pay/` → 后端服务
  - `/files/` → 后端静态资源
  - 其他 → 前端 SPA

**Elasticsearch Dockerfile**：
- 基于 ES 7.17.7 镜像
- 安装 IK 中文分词插件

### 10.3 Vite 开发代理

[vite.config.js](frontend/vite.config.js) 配置开发环境代理：
- `/api` → `http://localhost:8080`
- `/pay` → `http://localhost:8080`
- `/files` → `http://localhost:8080`
- `/ws` → `ws://localhost:8080` (WebSocket)

---

## 11. 依赖关系图

### 11.1 后端模块依赖

```
SwapUApplication
  ├── controller/
  │   ├── UserController ──────► UserService
  │   ├── ItemController ──────► ItemService
  │   ├── ChatController ──────► RagService + ChatClient
  │   ├── TradeOrderController ► TradeOrderService + RedissonClient
  │   ├── PaymentController ───► TradeOrderService + AlipayClient
  │   ├── AdminController ─────► UserService + ItemService + ReportService
  │   ├── CommentController ───► CommentService
  │   ├── ReportController ────► ReportService
  │   ├── ItemWantController ──► ItemWantService
  │   ├── ItemMessageController► ItemMessageService
  │   ├── ChatMessageController► ChatMessageService
  │   ├── ItemCategoryController► ItemCategoryService
  │   ├── SysNotificationController► SysNotificationService
  │   ├── SysAnnouncementController► SysAnnouncementService
  │   └── ws/ChatWebSocketEndpoint ► ChatMessageMapper + JwtUtils
  │
  ├── service/impl/
  │   ├── ItemServiceImpl ─────► ItemMapper + ItemRepository + ElasticsearchOperations
  │   │                        + RocketMQTemplate + RagService + DeepSeekUtils
  │   │                        + SearchHistoryService + UserMapper + ItemWantMapper
  │   ├── TradeOrderServiceImpl► TradeOrderMapper + ItemMapper + UserMapper
  │   │                        + RedissonClient + RocketMQTemplate
  │   │                        + TransactionTemplate + SysNotificationService
  │   ├── RagServiceImpl ─────► VectorStore (PgVectorStore)
  │   ├── UserServiceImpl ────► UserMapper + JwtUtils
  │   └── ...
  │
  ├── config/
  │   ├── PgVectorConfig ─────► pgJdbcTemplate + PgVectorStore + EmbeddingClient
  │   ├── WebConfig ──────────► LoginInterceptor + AdminInterceptor
  │   ├── WebSocketConfig ────► ChatWebSocketEndpoint
  │   ├── RedissonConfig ────► RedissonClient
  │   ├── AlipayConfig ──────► AlipayClient
  │   └── StartupRunner ──────► ItemService + RocketMQTemplate
  │
  └── interceptor/
      ├── LoginInterceptor ───► JwtUtils
      └── AdminInterceptor ───► (检查 request attribute)
```

### 11.2 数据流依赖

```
用户请求 → Controller → Service → Mapper → MySQL
                     → Service → ElasticsearchOperations → Elasticsearch
                     → Service → VectorStore → PgVector/PostgreSQL
                     → Service → RedissonClient → Redis
                     → Service → RocketMQTemplate → RocketMQ → Listener → Service
                     → Service → ChatClient → DeepSeek API
                     → Service → EmbeddingClient → Ollama API
```

### 11.3 前端依赖关系

```
App.vue
  ├── router/index.js ───► views/* (懒加载)
  ├── stores/user.js ────► localStorage + Axios
  └── utils/
      ├── request.js ─────► Axios + JWT Token
      └── format.js ──────► Day.js
```

---

> 文档生成时间：2026-05-06 | 基于 SwapU 项目源码自动分析
