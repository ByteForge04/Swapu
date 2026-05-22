# SwapU 校园闲置物品交易平台 - 核心架构与高级技术解析

本文档旨在全面总结 SwapU 平台中除基础 CRUD 外的核心技术架构、数据库设计以及高级技术栈的落地实现。可作为项目答辩、技术复盘或开发文档的权威参考。

---

## 一、 系统技术架构设计

SwapU 采用前后端分离的主流微服务化单体架构，融合了多种中间件以应对高并发与复杂业务场景。

### 1.1 技术栈选型
*   **前端端**：Vue 3 + Vite + Element Plus + Pinia (状态管理) + Vue Router。
*   **后端服务**：Spring Boot 3 + MyBatis-Plus + Java 17。
*   **数据存储**：MySQL 8.0 (主业务数据) + PostgreSQL/PgVector (向量数据库，用于 AI 检索) + Elasticsearch (全文检索)。
*   **缓存与分布式**：Redis (Redisson) 用于热点数据缓存、全局未读数计算与分布式锁。
*   **消息中间件**：RocketMQ 用于异步解耦（商品数据同步）和延迟队列（订单超时取消）。
*   **智能 AI 层**：Spring AI 接入 DeepSeek 大语言模型，结合 RAG（检索增强生成）技术实现智能导购助手。
*   **实时通信**：WebSocket 实现双向即时通讯（私信系统）。

### 1.2 部署与网络架构
系统支持 Docker Compose 一键容器化编排。在网络层，前端使用 Vite/Nginx 代理 `80` 和 `3001` 端口，将 `/api` 和 `/ws` 请求反向代理至后端 `8080` 端口，完美解决了跨域 (CORS) 问题，并结合 Cpolar 实现内网穿透与跨设备访问。

*(此处可插入：系统整体架构图)*

---

## 二、 核心数据库设计

SwapU 的数据库设计遵循范式与反范式结合的原则，重点保证交易链路的完整性和 AI 扩展性。

### 2.1 核心表关系 (ER 关系)
*   **用户 (`sys_user`)**：平台核心实体，与认证、物品、订单、聊天记录等均有一对多关系。
*   **物品 (`item`)**：记录商品详情（价格、分类、图片数组），状态分为：0-下架, 1-在售, 2-交易中, 3-已售出。
*   **订单 (`trade_order`)**：关联买家与卖家，记录订单流转状态（0-待支付, 1-已支付/进行中, 2-已完成, 3-已取消）。
*   **聊天记录 (`chat_message`)**：记录实时通讯信息，包含发送者、接收者、消息类型（文本/图片/卡片）和已读状态。

### 2.2 关键表结构节选
```sql
-- 交易订单表 (记录交易生命周期与支付状态)
CREATE TABLE `trade_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL COMMENT '业务订单号(防重)',
  `item_id` bigint NOT NULL COMMENT '关联商品ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `amount` decimal(10,2) NOT NULL COMMENT '交易金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0:待支付 1:进行中 2:已完成 3:已取消',
  `payment_method` tinyint DEFAULT NULL COMMENT '1:线上(支付宝) 2:线下',
  `payment_status` tinyint DEFAULT '0' COMMENT '0:未支付 1:已支付 2:已退款',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `idx_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

*(此处可插入：数据库 ER 模型图)*

---

## 三、 高级技术点一：基于 WebSocket 的实时双向通信 (私信系统)

### 3.1 业务痛点与方案
二手交易极度依赖买卖双方的沟通（砍价、验货）。若使用 HTTP 轮询不仅实时性差且极耗服务器性能。SwapU 采用全双工 **WebSocket** 协议，结合前端动态寻址与心跳保活，实现了企业级的私信系统。

### 3.2 核心实现
*   **连接与鉴权**：用户登录后建立 `wss://` 连接，后端通过 `ConcurrentHashMap` 将 `UserId` 与 `Session` 绑定，维护在线状态。
*   **离线与漫游**：发消息时，先持久化到 MySQL `chat_message` 并标记未读（`is_read=0`）。若对方在线则直接推送，对方离线则静默落库，对方上线时拉取未读消息。
*   **内网穿透兼容**：前端代码通过 `window.location.host` 动态计算 WebSocket 地址，并由 Vite 的 proxy 代理 `/ws`，完美解决不同网络环境下的连接失败问题。

### 3.3 后端核心代码 (`ChatWebSocketEndpoint.java`)
```java
@ServerEndpoint("/ws/chat/{userId}")
@Component
public class ChatWebSocketEndpoint {
    // 线程安全的在线用户 Session 映射表
    private static ConcurrentHashMap<Long, Session> webSocketMap = new ConcurrentHashMap<>();

    @OnMessage
    public void onMessage(String message, Session session) {
        // 心跳处理，防止连接被 Nginx/网关 切断
        if ("ping".equals(message)) {
            try { session.getBasicRemote().sendText("pong"); } catch (IOException ignored) {}
            return;
        }
        
        // 1. 反序列化消息并持久化
        ChatMessage chatMsg = JSON.parseObject(message, ChatMessage.class);
        chatMsg.setIsRead(0);
        chatMessageMapper.insert(chatMsg);

        // 2. 实时路由投递
        Long receiverId = chatMsg.getReceiverId();
        if (webSocketMap.containsKey(receiverId)) {
            Session receiverSession = webSocketMap.get(receiverId);
            receiverSession.getBasicRemote().sendText(JSON.toJSONString(chatMsg));
        }
    }
}
```

---

## 四、 高级技术点二：基于 RocketMQ 的延迟队列与订单自动取消

### 4.1 业务场景
买家下单后如果迟迟不支付，商品会被一直锁定（状态=2 交易中），导致其他用户无法购买。需要实现“下单后 30 分钟未支付自动取消，并释放库存”。

### 4.2 技术方案
没有使用传统的 Spring `@Scheduled` 定时任务（扫表延迟高且消耗 DB 性能），而是引入了 **RocketMQ 的延迟消息 (Delay Message)** 机制。

1.  **生产者**：创建订单时，向 RocketMQ 发送一条 `messageDelayLevel` 的延迟消息（例如 30 分钟）。
2.  **消费者**：30 分钟后，消费者监听到该消息，查询数据库验证订单状态。如果仍为“待支付”，则执行取消逻辑并将商品状态回退为“在售(1)”。

### 4.3 核心代码实现
```java
// 1. 生产者：创建订单后发送延迟消息
Message<String> message = MessageBuilder.withPayload(order.getOrderId().toString()).build();
// messageDelayLevel = 16 对应 RocketMQ 默认的 30 分钟
rocketMQTemplate.syncSend("order-cancel-topic", message, 3000, 16);

// 2. 消费者：监听延迟队列
@Component
@RocketMQMessageListener(topic = "order-cancel-topic", consumerGroup = "order-cancel-group")
public class OrderCancelListener implements RocketMQListener<String> {
    @Override
    public void onMessage(String orderIdStr) {
        Long orderId = Long.parseLong(orderIdStr);
        TradeOrder order = tradeOrderService.getById(orderId);
        
        // 幂等性与状态检查：如果订单仍未支付，则执行取消
        if (order != null && order.getStatus() == 0 && order.getPaymentStatus() == 0) {
            order.setStatus(3); // 3: 已取消
            tradeOrderService.updateById(order);
            // 释放商品库存状态
            itemService.updateStatus(order.getItemId(), 1);
            log.info("订单超时未支付，已自动取消并释放商品. 订单ID: {}", orderId);
        }
    }
}
```

---

## 五、 高级技术点三：支付宝沙箱安全支付闭环

### 5.1 业务场景
支持线上交易，集成支付宝电脑网站支付（沙箱环境），实现完整的资金流转。

### 5.2 安全设计与流程
1.  **非对称加密**：采用 RSA2 算法，商户私钥签名请求，支付宝公钥验签回调，防止数据被篡改。
2.  **发起支付**：后端组装 `out_trade_no` 和 `total_amount`，调用 `alipayClient.pageExecute` 生成 HTML 表单，前端接收并自动 Submit 唤起收银台。
3.  **异步通知 (`notify`)**：支付宝服务器主动 POST 回调后端，后端使用 `AlipaySignature.rsaCheckV1` 严格验签后，修改订单 `payment_status=1`。
4.  **同步跳转 (`return`)**：买家支付完成后，重定向至前端的订单详情页。

*(此处可插入：支付宝支付时序图)*

---

## 六、 高级技术点四：AOP 全局日志与统一异常处理

### 6.1 业务痛点
随着接口增多，排查线上 Bug 变得困难；且 Controller 抛出的异常（如 NPE）会导致前端收到原生 HTML 报错页面，引发前端解析崩溃。

### 6.2 解决方案
1.  **全局日志切面 (`GlobalLogAspect`)**：利用 Spring AOP 的 `@Around` 环绕通知，拦截所有 Controller。记录请求 URL、方法名、执行耗时。为了防止序列化 `HttpServletResponse` 引发 `IllegalStateException` 错误，特意对 Servlet 对象进行了过滤过滤。
2.  **全局异常处理器 (`GlobalExceptionHandler`)**：使用 `@RestControllerAdvice` 拦截业务级 `ServiceException` 和系统级 `Exception`，统装转为 `Result.error(msg)` JSON 格式，保证前后端交互协议的一致性。

### 6.3 核心代码
```java
// 过滤请求响应对象，防止 FastJSON 序列化流崩溃
Object[] filteredArgs = new Object[args.length];
for (int i = 0; i < args.length; i++) {
    if (args[i] instanceof jakarta.servlet.ServletRequest || 
        args[i] instanceof jakarta.servlet.ServletResponse) {
        filteredArgs[i] = "HttpServletRequest/Response";
    } else {
        filteredArgs[i] = args[i];
    }
}
String params = JSON.toJSONString(filteredArgs);
log.info("URL: {}, Method: {}, Params: {}", url, classMethod, params);
```

---

## 七、 高级技术点五：Spring AI 与 PgVector 构建 RAG 智能助手

### 7.1 业务场景
为平台提供“AI 智能一键发布润色”以及“智能导购”功能。

### 7.2 架构实现 (RAG - 检索增强生成)
1.  **向量化 (Embedding)**：当用户发布商品时，将商品文本通过 BGE 模型转化为高维向量，存入 **PostgreSQL (PgVector)** 数据库。
2.  **语义检索 (Retrieval)**：用户向 AI 提问时，将问题向量化，并在 PgVector 中进行余弦相似度计算，召回 Top K 相关的二手商品。
3.  **大模型生成 (Generation)**：将检索到的商品数据作为“背景上下文”与严格的 System Prompt 结合，提交给 DeepSeek LLM，最终流式输出具有特定业务逻辑的回复，并在前端渲染为“商品推荐卡片”。

*(此处可插入：RAG 检索增强生成流程图)*

---

## 八、 高级技术点六：Elasticsearch 全文检索与搜索建议补全

### 8.1 业务场景
首页商品搜索若使用传统 MySQL `LIKE` 不仅性能极差且不支持分词。需实现毫秒级商品全文检索，并提供类似百度的搜索下拉补全（Suggestion）功能。

### 8.2 核心实现
1.  **复合查询策略**：采用 `match_phrase_prefix` 匹配商品标题前缀，结合 `match` 对标题和描述进行分词搜索，最后利用 `wildcard` 作为通配符兜底，极大提升了召回率和准确率。
2.  **下拉建议**：前端输入框键入内容时，后端使用 `match_phrase_prefix` 对 Title 字段进行实时高亮建议。
3.  **数据同步 (双写/异步)**：引入 RocketMQ 保证数据最终一致性。商品发布/下架时向 `item-update-topic` 投递消息，消费者监听到后异步同步至 ES 索引；并且在 Spring Boot 启动时配置 `StartupRunner` 支持人工或异常情况下的兜底全量同步。

*(此处可插入：ES 全文检索与双写同步架构图)*

---

## 九、 高级技术点七：多数据源优雅配置 (MySQL + PgVector 避坑指南)

### 9.1 业务痛点
由于系统混用了主业务库 MySQL 与向量库 PostgreSQL，若配置不当，Spring Boot 会抛出多数据源冲突（如 `jdbcUrl is required` 或 MyBatis-Plus 找不到主库路由）。

### 9.2 解决方案
遵循“主库自动装配，副库手动编排”的原则：**切勿**将副数据源（PgVector）通过 `@Bean` 暴露给 Spring 容器上下文。
**最佳实践**：保持 `application.yml` 中默认的 MySQL 配置不变以让 HikariCP 自动托管，而对于 PgVector，直接在实例化 `JdbcTemplate` 或相应的 AI 组件内部独立创建副数据源，完美规避 MyBatis-Plus 的主库覆盖问题。

---

## 十、 高级技术点八：大模型多轮对话的会话记忆 (Session Memory)

### 10.1 业务场景
用户与 AI 导购助手的对话通常是连续的（如：“有自行车吗？” -> “最便宜的多少钱？”）。HTTP 是无状态的，如果不做特殊处理，大模型无法理解“最便宜的”指的是“自行车”。

### 10.2 实现方案
1.  **前端持久化**：使用 Vue 3 的 `onMounted` 与 `watch` 监听对话数组，将其序列化存入 `localStorage` 中。
2.  **上下文截取**：每次发送新消息时，截取最近 3-5 轮的历史对话连同新问题一并发送（避免 Token 消耗过大）。
3.  **后端 Prompt 注入**：后端拦截历史记录，通过 Spring AI 的 `Message` 对象动态重组上下文，并作为 `Prompt` 的一部分发送给 LLM。

### 10.3 核心代码
```java
// 后端接收历史对话并拼装至 Prompt 示例
List<Message> messageList = new ArrayList<>();
// 1. 注入系统级角色设定
messageList.add(new SystemMessage("你是一个专业的校园二手导购助手..."));
// 2. 注入历史会话记录 (User & Assistant)
for (ChatMessage history : request.getHistory()) {
    if ("user".equals(history.getRole())) {
        messageList.add(new UserMessage(history.getContent()));
    } else {
        messageList.add(new AssistantMessage(history.getContent()));
    }
}
// 3. 注入本次提问
messageList.add(new UserMessage(request.getPrompt()));
Prompt prompt = new Prompt(messageList);
ChatResponse response = chatClient.call(prompt);
```

---

## 十一、 高级技术点九：基于 Docker 的生产级容器化部署

### 11.1 业务场景
为了消除开发环境与生产环境差异，解决“在我的机器上能跑”的问题，系统需要支持跨平台的快速自动化部署。

### 11.2 实现细节
1.  **一键容器编排**：编写 `docker-compose.yml` 统一管理后端、前端及所有中间件（MySQL, Redis, ES, RocketMQ 等），并配置网络桥接与数据卷挂载持久化。
2.  **前端多阶段构建 (Multi-stage Build)**：`frontend/Dockerfile` 首先使用 Node 环境编译 Vite 项目产出静态 `dist` 文件，再将其拷贝至 Nginx 镜像中。
3.  **Nginx 反向代理配置**：在生产级 `nginx.conf` 中配置 `/api` 代理到后端容器的 `8080` 端口，从部署层面优雅解决 CORS 跨域问题。
4.  **国内网络优化**：针对 Docker Pull 镜像超时，在宿主机配置国内加速器（如阿里云、腾讯云镜像源）以保障 CI/CD 流程的顺畅。
