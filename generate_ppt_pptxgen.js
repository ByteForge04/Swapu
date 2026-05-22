const pptxgen = require("pptxgenjs");
const pres = new pptxgen();
pres.layout = "LAYOUT_WIDE";
pres.author = "SwapU";
pres.title = "基于Vue3+SpringBoot的校园闲置物品循环利用系统设计与实现";

const C = {
  navy: "1B3A5C", darkNavy: "0F2440", orange: "E86A17", white: "FFFFFF",
  lightGray: "F2F4F6", midGray: "8899AA", darkText: "2D3436",
  codeBg: "1E1E1E", codeText: "D4D4D4", green: "27AE60", red: "E74C3C",
  lightBlue: "EEF3F9", border: "DDE4EA",
};

const FONT = "Microsoft YaHei";
const MONO = "Consolas";

function darkSlide(text, subtitle) {
  const s = pres.addSlide();
  s.background = { color: C.navy };
  s.addShape(pres.shapes.RECTANGLE, { x: 0.7, y: 2.8, w: 1.8, h: 0.05, fill: { color: C.orange } });
  s.addText(text, { x: 0.7, y: 1.8, w: 11.5, h: 1.2, fontSize: 38, fontFace: FONT, color: C.white, bold: true, margin: 0 });
  if (subtitle) s.addText(subtitle, { x: 0.7, y: 3.1, w: 11.5, h: 1.5, fontSize: 18, fontFace: FONT, color: C.midGray, margin: 0 });
  s.addText("武汉理工大学 · 计算机与人工智能学院 · 2026届本科毕业设计答辩", { x: 0.7, y: 6.8, w: 11.5, h: 0.5, fontSize: 11, fontFace: FONT, color: C.midGray, margin: 0 });
  return s;
}

function slide(title, bullets, footnote) {
  const s = pres.addSlide();
  s.background = { color: C.white };
  s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 13.333, h: 0.04, fill: { color: C.orange } });
  s.addShape(pres.shapes.RECTANGLE, { x: 0.6, y: 0.7, w: 0.05, h: 1.0, fill: { color: C.navy } });
  s.addText(title, { x: 0.85, y: 0.6, w: 11.5, h: 0.9, fontSize: 28, fontFace: FONT, color: C.navy, bold: true, margin: 0 });
  s.addShape(pres.shapes.LINE, { x: 0.85, y: 1.4, w: 11.5, h: 0, line: { color: C.border, width: 1 } });
  var texts = bullets.map(function(b) {
    if (b.startsWith("## ")) return { text: b.slice(3), options: { fontSize: 20, fontFace: FONT, color: "2C5F8A", bold: true, breakLine: true, bullet: false } };
    if (b === "") return { text: "", options: { fontSize: 10, breakLine: true, bullet: false } };
    return { text: b, options: { fontSize: 15, fontFace: FONT, color: C.darkText, breakLine: true, bullet: false, paraSpaceAfter: 4 } };
  });
  s.addText(texts, { x: 0.85, y: 1.65, w: 11.5, h: 4.8, valign: "top", margin: [0, 0, 0, 0] });
  if (footnote) {
    s.addShape(pres.shapes.RECTANGLE, { x: 0.7, y: 5.8, w: 11.8, h: 0.7, fill: { color: C.lightBlue } });
    s.addText(footnote, { x: 0.9, y: 5.85, w: 11.5, h: 0.6, fontSize: 13, fontFace: FONT, color: C.navy, bold: true, margin: 0 });
  }
  s.addText(String(pres.slides.length), { x: 12.3, y: 7.15, w: 0.8, h: 0.3, fontSize: 10, fontFace: FONT, color: C.midGray, align: "right", margin: 0 });
  return s;
}

function codeSlide(title, lines, note) {
  const s = pres.addSlide();
  s.background = { color: C.codeBg };
  s.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 13.333, h: 0.04, fill: { color: C.orange } });
  s.addText(title, { x: 0.6, y: 0.4, w: 12, h: 0.7, fontSize: 22, fontFace: FONT, color: C.white, bold: true, margin: 0 });
  var codeTexts = lines.map(function(l) { return { text: l + "\n", options: { fontSize: 12, fontFace: MONO, color: C.codeText, breakLine: false, paraSpaceAfter: 0 } }; });
  s.addText(codeTexts, { x: 0.6, y: 1.3, w: 12, h: 4.5, valign: "top", margin: 0 });
  if (note) s.addText(note, { x: 0.6, y: 6.3, w: 12, h: 0.6, fontSize: 14, fontFace: FONT, color: "FFCC66", bold: true, margin: 0 });
  s.addText(String(pres.slides.length), { x: 12.3, y: 7.15, w: 0.8, h: 0.3, fontSize: 10, fontFace: MONO, color: "666666", align: "right", margin: 0 });
  return s;
}

// SLIDE 1: Cover
darkSlide(
  "基于Vue3 + Spring Boot的校园闲置物品\n循环利用系统设计与实现",
  "SwapU — 校园智能二手交易平台\n\n答辩人：XXX    指导教师：XXX"
);

// SLIDE 2: Background
slide("项目背景与痛点", [
  "## 校园闲置物品的周期性积压",
  "每学期末大量教材、电子产品、生活用品被闲置 —— 资源错配是结构性的",
  "",
  "## 现有平台为什么没解决？",
  "1) 信息匹配低效 —— 全国大市场逻辑，无法按校区半径筛选，搜索不懂自然语言",
  "2) 发布门槛偏高 —— 写吸引人的商品描述需要精力，平台没有任何辅助工具",
  "3) 沟通未场景化 —— 当面交易为主的校园场景缺少本地化适配",
  "",
  "## SwapU 的设计思路",
  "用 AI 降低发布门槛 + 用 RAG 提升搜索精度 + 用 WebSocket 定制校园化沟通",
], "\u{1F4CC} 核心命题：将大模型从「接入」变为「用好」，在垂直场景中工程化落地");

// SLIDE 3: Features
slide("系统功能总览", [
  "## 前台交易子系统（5 大模块）",
  "1) 用户认证 —— BCrypt密码加密 + JWT Token 无状态鉴权",
  "2) 商品发布 —— AI 一键文案润色 + AI 内容安全审核，生成结构化描述",
  "3) 智能搜索 —— 关键词搜索(ES BoolQuery) + AI 智能导购(RAG + DeepSeek)",
  "4) 订单交易 —— Redisson分布式锁防超卖 + RocketMQ延时消息超时取消 + 支付宝沙箱",
  "5) 即时通讯 —— WebSocket 点对点实时聊天，消息持久化 MySQL",
  "",
  "## 后台管理子系统（3 大模块）",
  "1) 数据大盘(ECharts) + 2) 内容审核(商品/留言/举报) + 3) 向量知识库同步(双轨策略)",
], "15个Controller + 13个ServiceImpl + 1个WebSocket端点");

// SLIDE 4: Architecture
slide("系统技术架构（五层）", [
  "## 表现层",
  "Nginx反向代理，/api -> 后端8080，静态资源自托管",
  "",
  "## 网关控制层",
  "LoginInterceptor(JWT鉴权) -> AdminInterceptor(角色校验) -> GlobalLogAspect(AOP审计)",
  "",
  "## 业务逻辑层",
  "15 Controller + 13 ServiceImpl + ChatWebSocketEndpoint",
  "核心：ItemServiceImpl(8个依赖注入)、TradeOrderServiceImpl(分布式锁+事务)",
  "",
  "## 数据持久层",
  "MySQL(ACID写入) | Redis(缓存/锁/限流) | ES(搜索副本) | PgVector(向量存储)",
  "",
  "## 基础设施层",
  "RocketMQ(异步/延时) | 支付宝沙箱 | DeepSeek Chat | Ollama bge-m3",
], "层间依赖严格控制，上层不碰下层数据实现");

// SLIDE 5: RAG Innovation
slide("核心创新1：RAG 智能导购 ——「先检索，后生成」", [
  "## 流程",
  "1) 用户自然语言提问（如「两百以内有没有适合工科生的计算器」）",
  "2) PgVector 余弦相似度检索（HNSW索引, topK=3, threshold=0.0, 1024维）",
  "3) 将 Top 3 真实商品（标题、价格、描述）注入 System Prompt",
  "4) DeepSeek Chat 基于真实库存生成推荐 -> 返回 { text, items }",
  "",
  "## 为什么这样设计？",
  "直接用 LLM -> 幻觉：推荐平台上不存在的商品",
  "RAG 约束 -> 模型只能推荐检索到的真实商品 -> 前端渲染可点击商品卡片",
  "topK=3 实验依据：1->太窄匹配不上，5->Prompt太长推荐变泛化",
  "多轮对话：Math.max(0, history.size()-6) 截取最近6条=3轮",
], "核心设计决策：让模型回答被钉在真实库存上，从机制层抑制幻觉");

// SLIDE 6: RAG Code
codeSlide("RAG 智能导购 —— ChatController.java", [
  "@PostMapping('/chat')",
  "public Result<Map> chat(@RequestBody Map req) {",
  "  // Step 1: RAG vector retrieval topK=3",
  "  List<Map> items = ragService.retrieveRelevantItemDetails(msg, 3);",
  "",
  "  // Step 2: Inject retrieval results into System Prompt",
  "  systemPrompt.append('Do not fabricate products.');",
  "  items.forEach(item -> systemPrompt.append(",
  "    'Title:' + item.title + ' Price:' + item.price));",
  "",
  "  // Step 3: Trim to last 6 history entries = 3 turns",
  "  int start = Math.max(0, history.size() - 6);",
  "",
  "  // Step 4: Sync call LLM, Step 5: Wrap response",
  "  String resp = chatClient.call(prompt).getResult().getOutput().getContent();",
  "  return Result.success(Map.of('text', resp, 'items', items));",
  "}",
], "关键：items字段 = 真实商品数据 -> 前端渲染可点击卡片 -> 杜绝幻觉推荐");

// SLIDE 7: Distributed Lock
codeSlide("核心创新2：分布式锁防超卖 + 延时消息超时取消", [
  "public Result<?> createOrder(TradeOrder order) {",
  "  // 1. Item-level distributed lock",
  "  RLock lock = redissonClient.getLock('item:lock:' + itemId);",
  "  lock.tryLock(3, 10, TimeUnit.SECONDS);  // wait 3s, hold 10s",
  "",
  "  // 2. Programmatic transaction (NOT @Transactional!)",
  "  return transactionTemplate.execute(status -> {",
  "    // Validate status=1, save order status=0, update item status=2",
  "    item.setStatus(2);",
  "",
  "    // 3. Send 30min delay message for online payment only",
  "    if (order.getPaymentMethod() == 1) {",
  "      mqMsg.setDelayTimeLevel(16);  // level 16 = 30min",
  "      rocketMQTemplate.getProducer().send(mqMsg);",
  "    }",
  "    return Result.success(order.getOrderId());",
  "  });",
  "  // 4. finally { lock.unlock(); }  - precise release timing",
  "}",
], "为什么TransactionTemplate？锁释放必须在事务提交前，@Transactional无法控制");

// SLIDE 8: Dual Track Sync
slide("核心创新3：PgVector 与 MySQL 双轨数据同步", [
  "## 问题",
  "商品库时刻变动（上新/下架/改价/成交）-> PgVector和MySQL怎么保持一致性？",
  "",
  "## 第一轨：RocketMQ 增量同步",
  "publish() -> CompletableFuture 直接异步写PgVector (路径一)",
  "         -> syncToEs() -> ItemSyncMessage -> RocketMQ item-update-topic",
  "         -> ItemSyncListener -> doSyncToEs() -> ES + PgVector (路径二)",
  "消费端重新查MySQL (getById) 而非用消息快照 —— 避免异步延迟脏写",
  "",
  "## 第二轨：全量补偿",
  "syncAllToEs() 手动触发，遍历所有status=1商品批量重建索引",
  "",
  "两条路径在PgVector写入上存在冗余 —— 但各自独立，MQ延迟/单点故障时保证数据不丢",
], "增量 + 全量兜底：分布式环境中无绝对一致，但把不一致窗口压至可接受范围");

// SLIDE 9: Database
slide("核心数据库设计", [
  "## 关系型设计（MySQL 8.0, InnoDB）",
  "sys_user：role=0用户/1管理员, phone字段AES-128/ECB/PKCS5Padding加密存储",
  "item：五态状态机 (0待审核->1在售->2交易中->3已售出->4已下架)",
  "trade_order：order_no=UUID去横线, amount=下单时刻快照价格",
  "关键索引：buyer_id, seller_id, user_id, category_id 均建B+树",
  "",
  "## 关键设计决策",
  "amount 是下单时刻从 item.price 同步的快照 —— 已写入不再随商品改价变动",
  "payment_status 独立于 order_status —— 线下交易可以不付款走完流程",
  "Phone 通过 MyBatis-Plus TypeHandler 透明加解密 —— Service层代码无感知",
  "",
  "## 向量存储（PostgreSQL + PgVector）",
  "HNSW 索引 + COSINE_DISTANCE + 1024维 (bge-m3 Embedding输出)",
], "四核心表：sys_user -> item -> trade_order (item_category)");

// SLIDE 10: Test Results
slide("系统测试结果", [
  "## 性能测试：500并发线程 x 5分钟",
  "首页商品列表：平均45ms, TPS 2100+",
  "商品搜索 (ES)：平均120ms, TPS ~850",
  "AI 智能导购：平均3.5s (LLM推理2-3s + 网络往返 + Prompt处理)",
  "CPU峰值 < 75%, 无OOM",
  "",
  "## 功能测试：全链路无断点",
  "发布->搜索->下单->支付->取消，7条路径状态迁移正确",
  "RAG 管线端到端验证：自然语言 -> 检索 -> LLM推理 -> 卡片渲染，无幻觉",
  "订单超时取消：30min后幂等校验通过，订单->status=3，商品->回滚status=1",
  "",
  "## 安全测试",
  "AES加密：DB中phone为密文, API返回自动解密为明文 [OK]",
  "限流：前3次200, 第4次起返回「下单过于频繁」 [OK]",
], "已知不足：AI接口延迟3.5s偏高(待改SSE流式) | 登录接口未限流 | ES低配瓶颈");

// SLIDE 11: Conclusion
slide("总结与展望", [
  "## 主要工作",
  "1) 完整交易链路：发布 -> 搜索/AI导购 -> 下单(锁+事务) -> 支付 -> 聊天",
  "2) RAG 智能导购：三层约束(Prompt工程 + 事实约束 + 数据同步)抑制幻觉",
  "3) 工程可靠性：分布式锁防超卖 + 延时消息超时取消 + 双轨数据同步",
  "",
  "## 创新点回顾",
  "先检索后生成：不是把数据丢给模型，而是让模型被真实库存约束",
  "双轨同步：增量MQ + 全量兜底，工程级别的数据一致性方案",
  "Prompt工程：十几轮迭代 -> 三项硬约束 -> 输出从「灾难」到「可靠」",
  "",
  "## 展望",
  "1) SSE流式输出(ChatController已import Flux, 改.call()为.stream())",
  "2) 多模态检索(CLIP视觉模型 -> 「以图搜图」 -> 需GPU实例)",
  "3) 补全安全短板(登录限流 + ES资源优化)",
], "达成设计目标：交易链路完整 + 中间件协同稳定 + AI能力工程化可用");

// SLIDE 12: Thank You
(function() {
  var s = pres.addSlide();
  s.background = { color: C.darkNavy };
  s.addShape(pres.shapes.RECTANGLE, { x: 5.4, y: 2.8, w: 2.5, h: 0.05, fill: { color: C.orange } });
  s.addText("感谢各位老师批评指正", { x: 0.5, y: 1.8, w: 12.3, h: 1.5, fontSize: 42, fontFace: FONT, color: C.white, bold: true, align: "center", margin: 0 });
  s.addText("SwapU — 校园智能二手交易平台", { x: 0.5, y: 3.8, w: 12.3, h: 0.8, fontSize: 18, fontFace: FONT, color: C.midGray, align: "center", margin: 0 });
  s.addText("武汉理工大学 · 计算机与人工智能学院 · 2026届本科毕业设计答辩", { x: 0.5, y: 6.8, w: 12.3, h: 0.5, fontSize: 11, fontFace: FONT, color: C.midGray, align: "center", margin: 0 });
})();

// Save
pres.writeFile({ fileName: "d:/SwapU/docs/thesis/SwapU答辩PPT.pptx" }).then(function() {
  console.log("PPT saved: d:/SwapU/docs/thesis/SwapU答辩PPT.pptx");
  console.log("Total slides: " + pres.slides.length);
});
