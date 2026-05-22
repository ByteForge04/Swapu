# SwapU 校园闲置物品交易平台 - 答辩关键技术点解析

这份文档为你梳理了 SwapU 项目中最核心、最能体现技术深度的 **5 个关键技术点**。每个技术点都包含了**业务场景痛点、完整的处理流程说明以及核心实现代码**。建议你在答辩 PPT 和现场讲解中重点展示这些内容，以体现你的工程能力和架构思维。

---

## 核心技术点一：基于 RAG 架构的 AI 智能导购助手 (Spring AI + PgVector + DeepSeek)

### 1. 业务场景与痛点
传统的二手交易平台只能依靠精确的关键字去搜索商品，当买家不清楚具体商品名称或有模糊需求（如：“我想买一本适合考研复习的书”）时，传统数据库的 `LIKE` 查询无能为力。

### 2. 详细流程说明
为了让搜索具备“语义理解”能力并能进行自然对话，项目引入了 **RAG (检索增强生成)** 架构：
1. **数据向量化存储**：用户每发布一件闲置商品，系统会在后台通过本地大模型（Ollama + BGE-m3）将商品的“标题+描述”转化为多维向量（Embedding），并存入 PostgreSQL 的 `pgvector` 扩展库中。
2. **用户意图检索**：买家在 AI 助手页面输入模糊需求，系统同样将其转化为向量，并在 PgVector 中进行余弦相似度检索，找出最匹配的 Top 3 商品。
3. **Prompt 上下文组装**：将检索出的真实商品信息拼接到 System Prompt（系统提示词）中。
4. **大模型生成**：调用远端能力更强的 DeepSeek 模型，大模型结合 Prompt 中的真实商品信息生成自然语言推荐文本。
5. **多模态返回**：后端不仅返回大模型的对话文本，还将携带了结构化的商品卡片数据返回前端渲染。

### 3. 核心代码
```java
// ChatController.java - AI 对话核心逻辑
@PostMapping("/chat")
public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> request) {
    String userMessage = (String) request.get("message");
    
    // 1. 通过 RAG 从向量数据库检索语义相关的商品 (Top 3)
    List<Map<String, Object>> relevantItems = ragService.retrieveRelevantItemDetails(userMessage, 3);
    
    // 2. 构建包含业务规则的系统提示词
    StringBuilder promptBuilder = new StringBuilder();
    promptBuilder.append("你是 SwapU 校园闲置平台的智能助手。请结合以下查找到的商品进行推荐...\n");
    if (!relevantItems.isEmpty()) {
        promptBuilder.append("【商品上下文开始】\n");
        for (int i = 0; i < relevantItems.size(); i++) {
            Map<String, Object> item = relevantItems.get(i);
            promptBuilder.append("标题: ").append(item.get("title"))
                         .append(", 价格: ").append(item.get("price")).append("\n");
        }
        promptBuilder.append("【商品上下文结束】\n");
    }
    
    // 3. 将上下文包装成 UserMessage，调用 DeepSeek 大模型
    Message message = new UserMessage(promptBuilder.toString() + " 用户问题: " + userMessage);
    Prompt prompt = new Prompt(List.of(message), OpenAiChatOptions.builder().build());
    String aiResponse = chatClient.call(prompt).getResult().getOutput().getContent();
    
    // 4. 将 AI 的文本回复和关联的商品卡片数据一并返回前端渲染
    Map<String, Object> responseData = new HashMap<>();
    responseData.put("text", aiResponse);
    responseData.put("items", relevantItems);
    return Result.success(responseData);
}
```

---

## 核心技术点二：基于 RocketMQ 延迟队列的防锁单机制

### 1. 业务场景与痛点
在二手交易中，商品通常只有 1 件库存。如果恶意买家疯狂下单但不支付，就会导致商品被“锁死”（状态变为交易中），其他真实买家无法购买。如果采用数据库定时任务（如每分钟扫描所有订单），在订单量大时会严重拖垮数据库性能。

### 2. 详细流程说明
本项目采用 **RocketMQ 延迟消息** 实现高性能的超时自动取消：
1. **创建订单并锁库存**：买家下单选择“线上支付”后，系统创建订单并将商品状态改为“交易中”。
2. **投递延迟消息**：后端立即向 RocketMQ 的 `order-timeout-topic` 发送一条延迟级别为 `16`（对应 30 分钟）的消息，消息体为 `orderId`。
3. **消息滞空**：该消息会在 MQ 的 Broker 内部驻留 30 分钟，在此期间不会被消费者拉取到，完全不占用业务系统的 CPU 和数据库资源。
4. **到期消费与状态回滚**：30 分钟后，消息对消费者可见。系统监听到该消息，查询数据库中该订单的支付状态。如果仍为“未支付”，则执行“取消订单、商品重新上架”逻辑。

### 3. 核心代码
```java
// 1. 发送延迟消息 (TradeOrderServiceImpl.java)
@Transactional(rollbackFor = Exception.class)
public Result<?> createOrder(TradeOrder order) {
    // ...保存订单逻辑...
    
    // 发送延迟消息到 RocketMQ，实现 30 分钟未支付自动取消订单
    // 级别 16 对应 30m延迟
    if (order.getPaymentMethod() == 1 && rocketMQTemplate != null) {
        OrderTimeoutMessage msg = new OrderTimeoutMessage(order.getOrderId());
        Message<OrderTimeoutMessage> message = MessageBuilder.withPayload(msg).build();
        rocketMQTemplate.syncSend("order-timeout-topic", message, 3000, 16);
    }
    return Result.success(order);
}

// 2. 延迟消息消费者监听 (OrderTimeoutListener.java)
@Component
@RocketMQMessageListener(topic = "order-timeout-topic", consumerGroup = "order-timeout-group")
public class OrderTimeoutListener implements RocketMQListener<OrderTimeoutMessage> {
    @Override
    public void onMessage(OrderTimeoutMessage message) {
        Long orderId = message.getOrderId();
        TradeOrder order = tradeOrderService.getById(orderId);
        
        // 如果订单仍处于 0 (待确认/未支付) 状态，则执行自动取消
        if (order != null && order.getStatus() == 0 && 
           (order.getPaymentStatus() == null || order.getPaymentStatus() == 0)) {
            tradeOrderService.cancelOrder(order.getBuyerId(), orderId);
            // ...发送通知给买家...
        }
    }
}
```

---

## 核心技术点三：基于 WebSocket 的实时双向通信 (私信系统)

### 1. 业务场景与痛点
二手交易极度依赖买卖双方的沟通（如砍价、确认新旧程度、约定当面交易地点）。如果仅依靠站内信或轮询请求（HTTP Polling）来获取消息，不仅实时性差，还会产生大量的无效网络开销。

### 2. 详细流程说明
采用全双工通信协议 **WebSocket** 实现实时聊天：
1. **建立连接与身份绑定**：用户登录后，前端携带 `userId` 发起 `ws://` 握手请求。后端拦截器校验通过后，将 `Session` 与 `userId` 绑定，存入全局的 `ConcurrentHashMap` 中。
2. **心跳保活机制**：前端每 20 秒发送一次 `"ping"`，后端立即回复 `"pong"`，防止连接因代理服务器超时限制而被强行切断。
3. **消息实时路由与离线持久化**：
   - A 发消息给 B，后端收到消息先存入 MySQL 数据库（`chat_message` 表），标记为未读（`is_read=0`）。
   - 后端检查 `ConcurrentHashMap` 中是否存在 B 的 Session。如果 B 在线，直接通过 `session.getBasicRemote().sendText()` 推送消息；如果 B 离线，则静默存储。
4. **前端动态红点**：前端监听消息，如果当前未在对应的聊天窗口，立刻更新顶栏的全局红点数量。

### 3. 核心代码
```java
// ChatWebSocketEndpoint.java - WebSocket 端点
@ServerEndpoint("/ws/chat/{userId}")
@Component
public class ChatWebSocketEndpoint {
    // 线程安全的在线用户 Session 映射表
    private static ConcurrentHashMap<Long, Session> webSocketMap = new ConcurrentHashMap<>();

    @OnMessage
    public void onMessage(String message, Session session) {
        // 心跳处理，直接返回 pong 维持长连接
        if ("ping".equals(message)) {
            try { session.getBasicRemote().sendText("pong"); } catch (IOException ignored) {}
            return;
        }
        
        // 1. 反序列化消息并持久化到数据库
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

## 核心技术点四：非对称加密的安全支付闭环 (支付宝沙箱)

### 1. 业务场景与痛点
校园交易虽然存在线下当面付，但针对金额较大或不见面的交易，必须提供平台担保的线上支付手段。同时，在支付成功后，必须确保回调通知是支付宝官方发出的，防止黑客伪造支付成功请求（越权篡改订单）。

### 2. 详细流程说明
引入了支付宝开放平台沙箱环境，并实现了基于 **RSA2 非对称加密** 的回调验签：
1. **发起支付**：后端根据订单信息，使用私钥签名构造 `AlipayTradePagePayRequest`，调用支付宝网关生成包含 HTML 支付表单的响应直接刷给前端。
2. **前端重定向**：前端接收到 HTML 后自动提交表单，重定向至支付宝收银台。
3. **异步安全回调验签 (Notify)**：用户支付成功后，支付宝服务器会主动向我们的 `/pay/notify` 接口发送 POST 请求。
4. **RSA 验签验证**：后端提取所有请求参数，利用支付宝的公钥（Public Key）调用 SDK 方法 `AlipaySignature.rsaCheckV1` 进行签名比对。如果验签通过，证明消息未被篡改，且确由支付宝发出，此时再修改数据库订单状态为“已支付”。

### 3. 核心代码
```java
// PaymentController.java - 异步回调防伪造处理
@PostMapping("/notify")
public String notify(HttpServletRequest request) {
    Map<String, String> params = new HashMap<>();
    // ...将 request.getParameterMap() 展平存入 params...
    
    try {
        // 核心：使用支付宝公钥进行 RSA2 验签，防止伪造支付回调
        boolean verify = AlipaySignature.rsaCheckV1(params, 
                alipayConfig.getAlipayPublicKey(), 
                alipayConfig.getCharset(), 
                alipayConfig.getSignType());
        
        if (verify) {
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                String outTradeNo = params.get("out_trade_no");
                String tradeNo = params.get("trade_no");
                // 验签通过，更新本地订单状态并通知卖家发货
                tradeOrderService.paySuccess(outTradeNo, tradeNo, LocalDateTime.now());
            }
            // 必须返回 success 字符串，否则支付宝会按阶梯时间不断重发
            return "success"; 
        }
        return "failure";
    } catch (AlipayApiException e) {
        return "failure";
    }
}
```

---

## 核心技术点五：双向数据一致性与全局拦截体系 (AOP + 拦截器)

### 1. 业务场景与痛点
项目 API 众多，如果每个 Controller 接口都去手动校验用户是否登录（Token 是否合法），代码会极其冗余。同时，由于采用了前后端分离，如果程序在运行中发生了不可预知的错误（如空指针），前端收到原生的 HTML 错误页面会导致解析崩溃。

### 2. 详细流程说明
1. **无侵入的权限校验**：通过实现 `HandlerInterceptor` 并在 `WebMvcConfig` 中注册，拦截所有 `/api/**` 请求。在拦截器中提取 Header 里的 JWT Token 进行解析，若合法则将 `userId` 存入 `request.setAttribute` 供后续链路无感获取，否则直接拒绝请求返回 401 状态码。
2. **AOP 全局日志切面**：使用 `@Aspect` 和 `@Around` 环绕通知，动态代理所有 Controller 的执行。在方法执行前打印入参和 URL，执行完毕后统计并打印接口耗时与返回值，极大地提升了线上排查 Bug 的效率。
3. **全局统一异常处理**：使用 `@RestControllerAdvice` 和 `@ExceptionHandler` 捕获包括 `NullPointerException` 在内的所有抛出异常，并将其统一包装成业务标准格式（`Result.error(msg)`）返回给前端，确保前端始终能收到 JSON 格式的数据。

### 3. 核心代码
```java
// 1. 全局异常处理 (GlobalExceptionHandler.java)
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        // 统一拦截封装为标准 Result 对象，防止前端报错
        return Result.error(500, "系统内部错误: " + e.getMessage());
    }
}

// 2. JWT 登录拦截器 (LoginInterceptor.java)
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                // 解析 Token 并在上下文中注入 userId
                Claims claims = JwtUtils.parseToken(token);
                request.setAttribute("userId", claims.get("userId", Long.class));
                return true;
            } catch (Exception ignored) {}
        }
        // 拦截并返回 401，通知前端踢回登录页
        response.setStatus(401);
        return false;
    }
}
```