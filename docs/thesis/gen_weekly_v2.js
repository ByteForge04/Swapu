const JSZip = require('jszip');
const fs = require('fs');

const weeks = [
  {
    num: 1, date: '2026年3月2日 — 2026年3月8日',
    title: '第1周：选题与任务书确认',
    content: '完成毕业设计（论文）选题，与指导教师沟通确认题目为"基于Vue3+SpringBoot的校园闲置物品循环利用系统设计与实现"。查阅相关文献资料，初步了解系统开发所需的技术栈，明确设计任务与要求，领取毕业设计（论文）任务书。',
    plan: '下周计划深入开展需求调研与文献研究，通过问卷调查等方式收集高校学生闲置物品交易的一手需求资料。'
  },
  {
    num: 2, date: '2026年3月9日 — 2026年3月15日',
    title: '第2周：需求调研与文献研究',
    content: '深入调研高校学生闲置物品交易的现状与需求，通过问卷调查、访谈等方式收集一手资料。系统查阅国内外相关研究文献，重点研读校园二手交易平台、前后端分离架构、Vue3与SpringBoot技术应用、RAG检索增强生成技术等方面的论文与资料，累计阅读参考文献15篇以上（其中近五年外文文献3篇以上）。对现有平台（闲鱼、转转）在校园场景下的不足进行了分析，明确了系统的三个核心痛点：信息匹配效率不足、交易摩擦过大、发布门槛偏高。',
    plan: '下周计划在调研基础上撰写开题报告，明确研究目的及意义、技术方案和进度安排。'
  },
  {
    num: 3, date: '2026年3月16日 — 2026年3月22日',
    title: '第3周：开题报告撰写',
    content: '在前期调研和文献研究的基础上，撰写开题报告。明确研究的目的及意义（含国内外研究现状分析），梳理研究的基本内容、目标、拟采用的技术方案及措施，制定详细的进度安排。技术方案确定为：前端采用Vue3+Element Plus+Pinia，后端采用Spring Boot 3.2+MyBatis-Plus，数据层整合MySQL、Redis、Elasticsearch和PgVector四种存储引擎，AI能力通过Spring AI框架接入DeepSeek Chat和Ollama bge-m3模型。开题报告初稿完成后提交指导教师审阅，根据意见进行了修改完善。同时开始系统需求分析，使用用例图对用户管理、物品管理、检索匹配、交易置换、评价投诉、管理员后台六大功能模块进行建模。',
    plan: '下周计划完成系统总体设计，包括架构设计、功能模块划分和数据库设计。'
  },
  {
    num: 4, date: '2026年3月23日 — 2026年3月29日',
    title: '第4周：系统设计与数据库设计',
    content: '完成系统设计与数据库设计工作。系统采用前后端分离的B/S架构，运行时结构拆分为五层：表现层（Vue3+Vite）、网关控制层（JWT鉴权+AOP审计）、业务逻辑层（15个Controller+13个ServiceImpl）、数据持久层（MySQL+Redis+ES+PgVector）、基础设施层（RocketMQ+支付宝沙箱+Ollama+DeepSeek）。功能模块划分为四个模块：基础支撑模块、核心交易模块、AI智能赋能模块、后台管控模块。数据库设计方面，设计了用户表（sys_user）、商品表（item）、订单表（trade_order）、评论表（comment）、举报表（report）和分类表共六个业务表，绘制了E-R图，核心业务表遵循第三范式，高频查询字段适当冗余。安全设计上确定了BCrypt密码哈希和AES-128手机号加密方案。',
    plan: '下周计划搭建开发环境，开始前端基础页面开发。'
  },
  {
    num: 5, date: '2026年3月30日 — 2026年4月5日',
    title: '第5周：开发环境搭建与前端基础页面',
    content: '搭建开发环境，配置Vue3+SpringBoot前后端分离项目结构。前端使用Vite构建工具，安装Element Plus组件库和Pinia状态管理库，配置Axios请求库（baseURL设为/api，超时60000毫秒以适应AI接口）。完成前端路由配置与状态管理初始化，实现Axios拦截器统一处理token注入和401跳转。完成登录注册页面开发，登录页面包含用户名和密码输入框，注册页面需填写用户名、昵称和密码。完成首页商品列表页面，以卡片式信息流展示在售商品，顶部提供分类导航栏。完成管理员后台页面基本框架搭建。',
    plan: '下周计划继续前端核心页面开发，包括商品发布、AI导购、即时通讯和订单管理页面。'
  },
  {
    num: 6, date: '2026年4月6日 — 2026年4月12日',
    title: '第6周：前端核心页面开发',
    content: '完成前端核心页面开发。商品发布页面支持上传商品图片、选择分类、填写价格，描述输入框中集成了AI一键润色按钮，点击后调用后端/ai/polish接口生成结构化文案并自动填充。AI智能导购页面实现了对话式交互界面，用户以自然语言描述购物需求，系统基于RAG管线检索平台真实商品并生成推荐，同时展示可点击的商品卡片。即时通讯页面通过WebSocket建立买卖双方实时私信通道，支持文本和图片消息，实现了心跳保活和断线重连机制。订单管理页面展示买入和卖出订单列表，每个订单显示商品信息、交易状态和操作按钮。个人中心页面支持查看已发布商品、收藏商品列表等。',
    plan: '下周计划开始后端开发，搭建Spring Boot项目框架并实现用户认证模块。'
  },
  {
    num: 7, date: '2026年4月13日 — 2026年4月19日',
    title: '第7周：后端框架搭建与用户认证模块',
    content: '搭建后端项目框架并实现用户认证模块。基于Spring Boot 3.2框架搭建后端项目，采用MVC架构（控制层-服务层-数据访问层），集成MyBatis-Plus简化数据库操作。实现JWT无状态认证方案：JwtUtils类封装了密钥派生（HMAC-SHA256算法）和token签发逻辑，Claims包含userId、username、role三个字段，过期时间24小时。实现LoginInterceptor从Authorization请求头获取token并解析校验，未登录用户访问受保护接口返回401。实现AdminInterceptor校验role=1方可访问/admin/**路径，否则返回403。实现GlobalLogAspect通过AOP切面记录方法耗时与异常。用户注册登录接口使用BCrypt加密存储密码，AES-128 TypeHandler实现手机号加解密，业务代码无需感知加密过程。',
    plan: '下周计划开发后端商品管理与搜索模块，集成Elasticsearch全文检索。'
  },
  {
    num: 8, date: '2026年4月20日 — 2026年4月26日',
    title: '第8周：商品管理与搜索模块',
    content: '完成后端商品管理与搜索模块开发。ItemServiceImpl实现商品CRUD操作和五态状态机管理（待审核→在售→交易中→已售出→已下架），商品发布时调用DeepSeek进行AI合规审核。集成Elasticsearch全文检索，search()方法构建多层嵌套的BoolQuery：最外层bool必须status=1（在售），可选categoryId过滤；关键词搜索通过should子句匹配title和description字段（IK分词），支持通配符查询；高亮通过HighlightQuery配置preTags和postTags标签。实现match_phrase_prefix自动补全建议功能，返回前10条匹配结果。商品数据通过RocketMQ异步同步至ES索引，消息包含商品ID和操作类型。同时实现商品收藏、分类筛选等辅助功能。',
    plan: '下周计划开发订单交易与支付模块，实现分布式锁防超卖和支付宝沙箱支付。'
  },
  {
    num: 9, date: '2026年4月27日 — 2026年5月3日',
    title: '第9周：订单交易与支付模块',
    content: '完成后端订单交易与支付模块开发。TradeOrderServiceImpl实现订单创建、支付回调与状态流转逻辑，是系统中事务密度最高的模块。订单四态状态机：待确认→进行中→已完成→已取消。createOrder()方法通过Redisson分布式锁对商品加锁（key为item:lock:{itemId}，tryLock(3,10,SECONDS)非阻塞式获取），防止并发超卖。采用TransactionTemplate编程式事务而非声明式@Transactional，避免长事务问题。集成支付宝沙箱支付，后端构建AlipayTradePagePayRequest生成支付表单，异步回调中执行RSA验签、幂等性检查和订单状态更新。实现RocketMQ延时消息订单超时自动取消机制，OrderTimeoutListener检查订单仍为未支付状态则自动取消并恢复商品在售状态。实现订单确认收货、买家取消订单等接口。',
    plan: '下周计划开发AI智能赋能模块，集成Spring AI框架实现RAG智能导购和文案润色功能。'
  },
  {
    num: 10, date: '2026年5月4日 — 2026年5月10日',
    title: '第10周：AI智能赋能模块',
    content: '完成AI智能赋能模块开发，这是系统最具创新性的部分。集成Spring AI框架，通过ChatClient抽象层接入DeepSeek Chat（对话能力）和Ollama bge-m3（向量Embedding），两者通过Qualifier机制解耦。实现AI文案润色功能：System Prompt经过十余个版本迭代调优，解决了输出包含开场白、字数超标、格式不统一三个核心问题，最终版本明确字数500以内、只返回文案本身、分段清晰，temperature设为0.1降低随机性，输出合格率从不足40%提升至95%以上。实现RAG智能导购管线三步流程：第一步，RagService调用VectorStore.similaritySearch（topK=3、similarityThreshold=0.0）在PgVector中执行余弦相似度检索；第二步，ChatController拼接System Prompt、检索结果上下文和历史对话（最近6条）；第三步，chatClient.call()同步阻塞等待模型推理生成推荐。实现双轨同步策略保证向量数据与MySQL行记录一致性：RocketMQ增量同步保实时，管理员全量同步保正确。',
    plan: '下周计划开发后台管理与即时通讯模块，完成系统全部功能开发。'
  },
  {
    num: 11, date: '2026年5月11日 — 2026年5月17日',
    title: '第11周：后台管理与即时通讯模块',
    content: '完成后台管理与即时通讯模块开发，系统全部功能开发完成。即时通讯模块基于WebSocket实现，ChatWebSocketEndpoint使用@ServerEndpoint注解，通过ConcurrentHashMap管理在线连接，JWT认证通过URL查询参数传递。消息采用先持久化再转发的模式，确保消息不丢失。实现聊天历史查询、已读状态更新、联系人列表（UNION SQL合并买卖双方）和未读消息计数等接口。后台管理模块实现Dashboard页面，通过ECharts渲染近7日订单量折线图、新增用户数、商品品类分布和大模型调用频次等统计图表。实现商品审核与强制下架、举报处理（联动更新物品状态并发送系统通知）、公告管理等功能。系统通知通过@Async异步发送，避免阻塞主流程。前后端全部接口联调完成，系统功能链路贯通。',
    plan: '下周计划开展系统测试与优化，验证各核心功能的正确性和稳定性。'
  },
  {
    num: 12, date: '2026年5月18日 — 2026年5月24日',
    title: '第12周：系统测试与优化',
    content: '开展系统测试与优化工作。测试环境为4核8GB内存的Ubuntu虚拟机，JDK 17、MySQL 8.0、Redis 7.0、ES 8.12、PostgreSQL 16（含PgVector）、RocketMQ 5.1。采用黑盒测试方法，以普通用户和管理员两种角色遍历核心功能路径。完成6项核心测试用例：登录与注册测试（验证普通用户和管理员登录流程及异常场景提示）、商品智能发布测试（验证AI润色输出质量和发布流程完整性）、AI导购与全文检索测试（验证ES关键词搜索高亮和RAG导购推荐准确性）、线上交易订单测试（验证下单-支付-确认收货完整链路状态迁移）、订单超时取消测试（验证RocketMQ延时消息30分钟自动取消机制）、AES加密与越权防护测试（验证手机号密文存储和接口权限控制）。全部测试用例通过验证，系统各功能模块运行正常，AI导购推荐准确率满足预期，开始准备毕业设计说明书撰写工作。',
    plan: '后续将开始撰写毕业设计说明书，按照学校规范完成论文初稿。'
  }
];

function escapeXml(str) {
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// Template paragraph builders - matching the exact XML structure from the template

// Empty paragraph with 仿宋_GB2312 sz=30 (cover page style)
function emptyPara() {
  return '<w:p><w:pPr><w:spacing w:line="520" w:lineRule="exact"/><w:rPr><w:rFonts w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:pPr></w:p>';
}

// Cover title - style 6, centered, bold, 仿宋 sz=44
function coverTitle(text) {
  return '<w:p><w:pPr><w:pStyle w:val="6"/><w:snapToGrid w:val="0"/><w:spacing w:line="520" w:lineRule="exact"/><w:jc w:val="center"/><w:rPr><w:rFonts w:ascii="\u4EFF\u5B8B" w:hAnsi="\u4EFF\u5B8B" w:eastAsia="\u4EFF\u5B8B"/><w:b/><w:bCs/><w:sz w:val="44"/><w:szCs w:val="44"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u4EFF\u5B8B" w:hAnsi="\u4EFF\u5B8B" w:eastAsia="\u4EFF\u5B8B"/><w:b/><w:bCs/><w:sz w:val="44"/><w:szCs w:val="44"/></w:rPr><w:t>' + escapeXml(text) + '</w:t></w:r></w:p>';
}

// Info line - 仿宋_GB2312, sz=30, bold
function infoLine(text) {
  return '<w:p><w:pPr><w:spacing w:line="520" w:lineRule="exact"/><w:ind w:left="1" w:firstLine="996" w:firstLineChars="184"/><w:rPr><w:rFonts w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:b/><w:bCs/><w:kern w:val="0"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:b/><w:bCs/><w:kern w:val="0"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr><w:t>' + escapeXml(text) + '</w:t></w:r></w:p>';
}

// Section title - 仿宋_GB2312, sz=30, bold (like "3．进度安排")
function sectionTitle(text) {
  return '<w:p><w:pPr><w:spacing w:line="520" w:lineRule="exact"/><w:rPr><w:rFonts w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:b/><w:bCs/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:b/><w:bCs/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr><w:t>' + escapeXml(text) + '</w:t></w:r></w:p>';
}

// Body text - style 9, 宋体, sz=24
function bodyText(text) {
  return '<w:p><w:pPr><w:pStyle w:val="9"/><w:keepNext w:val="0"/><w:keepLines w:val="0"/><w:pageBreakBefore w:val="0"/><w:widowControl/><w:suppressLineNumbers w:val="0"/><w:shd w:val="clear" w:fill="FFFFFF"/><w:kinsoku/><w:wordWrap/><w:overflowPunct/><w:topLinePunct w:val="0"/><w:autoSpaceDE/><w:autoSpaceDN/><w:bidi w:val="0"/><w:adjustRightInd/><w:snapToGrid/><w:spacing w:before="192" w:beforeAutospacing="0" w:after="192" w:afterAutospacing="0" w:line="400" w:lineRule="exact"/><w:ind w:left="0" w:right="0" w:firstLine="0"/><w:textAlignment w:val="auto"/><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u5B8B\u4F53" w:hAnsi="\u5B8B\u4F53" w:eastAsia="\u5B8B\u4F53" w:cs="\u5B8B\u4F53"/><w:i w:val="0"/><w:iCs w:val="0"/><w:caps w:val="0"/><w:color w:val="0F1115"/><w:spacing w:val="0"/><w:sz w:val="24"/><w:szCs w:val="24"/><w:shd w:val="clear" w:fill="FFFFFF"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u5B8B\u4F53" w:hAnsi="\u5B8B\u4F53" w:eastAsia="\u5B8B\u4F53" w:cs="\u5B8B\u4F53"/><w:i w:val="0"/><w:iCs w:val="0"/><w:caps w:val="0"/><w:color w:val="0F1115"/><w:spacing w:val="0"/><w:sz w:val="24"/><w:szCs w:val="24"/><w:shd w:val="clear" w:fill="FFFFFF"/></w:rPr><w:t>' + escapeXml(text) + '</w:t></w:r></w:p>';
}

// Week entry - like P97 in template, style 9 with title bold + line break + content
function weekEntry(title, content) {
  return '<w:p><w:pPr><w:pStyle w:val="9"/><w:keepNext w:val="0"/><w:keepLines w:val="0"/><w:pageBreakBefore w:val="0"/><w:widowControl/><w:suppressLineNumbers w:val="0"/><w:shd w:val="clear" w:fill="FFFFFF"/><w:kinsoku/><w:wordWrap/><w:overflowPunct/><w:topLinePunct w:val="0"/><w:autoSpaceDE/><w:autoSpaceDN/><w:bidi w:val="0"/><w:adjustRightInd/><w:snapToGrid/><w:spacing w:before="192" w:beforeAutospacing="0" w:after="192" w:afterAutospacing="0" w:line="400" w:lineRule="exact"/><w:ind w:left="0" w:right="0" w:firstLine="0"/><w:textAlignment w:val="auto"/><w:rPr><w:rFonts w:hint="default" w:ascii="\u5B8B\u4F53" w:hAnsi="\u5B8B\u4F53" w:eastAsia="\u5B8B\u4F53" w:cs="\u5B8B\u4F53"/><w:i w:val="0"/><w:iCs w:val="0"/><w:caps w:val="0"/><w:color w:val="0F1115"/><w:spacing w:val="0"/><w:sz w:val="24"/><w:szCs w:val="24"/><w:shd w:val="clear" w:fill="FFFFFF"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:hint="default" w:ascii="\u5B8B\u4F53" w:hAnsi="\u5B8B\u4F53" w:eastAsia="\u5B8B\u4F53" w:cs="\u5B8B\u4F53"/><w:i w:val="0"/><w:iCs w:val="0"/><w:caps w:val="0"/><w:color w:val="0F1115"/><w:spacing w:val="0"/><w:sz w:val="24"/><w:szCs w:val="24"/><w:shd w:val="clear" w:fill="FFFFFF"/><w:b/><w:bCs/></w:rPr><w:t>' + escapeXml(title) + '</w:t></w:r><w:r><w:rPr><w:rFonts w:hint="default" w:ascii="\u5B8B\u4F53" w:hAnsi="\u5B8B\u4F53" w:eastAsia="\u5B8B\u4F53" w:cs="\u5B8B\u4F53"/><w:i w:val="0"/><w:iCs w:val="0"/><w:caps w:val="0"/><w:color w:val="0F1115"/><w:spacing w:val="0"/><w:sz w:val="24"/><w:szCs w:val="24"/><w:shd w:val="clear" w:fill="FFFFFF"/></w:rPr><w:br w:type="textWrapping"/></w:r><w:r><w:rPr><w:rFonts w:hint="default" w:ascii="\u5B8B\u4F53" w:hAnsi="\u5B8B\u4F53" w:eastAsia="\u5B8B\u4F53" w:cs="\u5B8B\u4F53"/><w:i w:val="0"/><w:iCs w:val="0"/><w:caps w:val="0"/><w:color w:val="0F1115"/><w:spacing w:val="0"/><w:sz w:val="24"/><w:szCs w:val="24"/><w:shd w:val="clear" w:fill="FFFFFF"/></w:rPr><w:t>' + escapeXml(content) + '</w:t></w:r></w:p>';
}

// Empty paragraph with style 6 (page break spacing)
function emptyParaStyle6() {
  return '<w:p><w:pPr><w:pStyle w:val="6"/><w:spacing w:line="520" w:lineRule="exact"/><w:rPr><w:rFonts w:ascii="\u4EFF\u5B8B" w:hAnsi="\u4EFF\u5B8B" w:eastAsia="\u4EFF\u5B8B"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr></w:pPr></w:p>';
}

// Signature line
function signatureLine() {
  return '<w:p><w:pPr><w:spacing w:line="520" w:lineRule="exact"/><w:rPr><w:rFonts w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr><w:t xml:space="preserve">                   \u6307\u5BFC\u6559\u5E08\uFF08\u7B7E\u540D\uFF09\uFF1A</w:t></w:r><w:r><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:sz w:val="30"/><w:szCs w:val="30"/><w:u w:val="single"/></w:rPr><w:t xml:space="preserve">                      </w:t></w:r></w:p>';
}

// Date line
function dateLine() {
  return '<w:p><w:pPr><w:adjustRightInd w:val="0"/><w:snapToGrid w:val="0"/><w:spacing w:line="520" w:lineRule="exact"/><w:jc w:val="right"/><w:rPr><w:rFonts w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr><w:t xml:space="preserve">                                                      \u5E74    \u6708      \u65E5</w:t></w:r></w:p>';
}

async function main() {
  // Read the template docx
  const templateBuf = fs.readFileSync('d:\\SwapU\\要求和模板\\开题报告模板.docx');
  const templateZip = await JSZip.loadAsync(templateBuf);

  // Read the original document.xml to get the sectPr
  const origDocXml = await templateZip.file('word/document.xml').async('string');
  
  // Extract sectPr from the original document
  const sectPrMatch = origDocXml.match(/<w:sectPr[\s\S]*?<\/w:sectPr>/);
  const sectPr = sectPrMatch ? sectPrMatch[0] : '<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1800" w:bottom="1440" w:left="1800" w:header="851" w:footer="992" w:gutter="0"/></w:sectPr>';

  // Build the new body content
  let bodyContent = '';

  // Cover page (3 empty paras like template)
  bodyContent += emptyPara();
  bodyContent += emptyPara();
  bodyContent += emptyPara();

  // Title
  bodyContent += coverTitle('\u6BD5\u4E1A\u8BBE\u8BA1\uFF08\u8BBA\u6587\uFF09\u5468\u8BB0');

  // Empty lines
  bodyContent += emptyPara();
  bodyContent += emptyPara();
  bodyContent += emptyPara();
  bodyContent += emptyPara();
  bodyContent += emptyPara();

  // Student info
  bodyContent += infoLine('\u5B66\u751F\u59D3\u540D\uFF1A      \u5218\u9065\u6770');
  bodyContent += infoLine('\u5BFC\u5E08\u59D3\u540D\u3001\u804C\u79F0\uFF1A      \u8BB2\u5E08 \u5F20\u971E');
  bodyContent += infoLine('\u6240\u5C5E\u5B66\u9662\uFF1A   \u8BA1\u7B97\u673A\u4E0E\u4EBA\u5DE5\u667A\u80FD\u5B66\u9662');
  bodyContent += infoLine('\u4E13\u4E1A\u73ED\u7EA7\uFF1A    \u8BA1\u7B97\u673Azy2201');
  bodyContent += infoLine('\u8BBE\u8BA1\uFF08\u8BBA\u6587\uFF09\u9898\u76EE\uFF1A');
  bodyContent += infoLine('\u57FA\u4E8E Vue3+SpringBoot \u7684\u6821\u56ED\u95F2\u7F6E\u7269\u54C1\u5FAA\u73AF\u5229\u7528\u7CFB\u7EDF\u8BBE\u8BA1\u4E0E\u5B9E\u73B0');

  // Empty lines
  bodyContent += emptyPara();
  bodyContent += emptyPara();
  bodyContent += emptyPara();
  bodyContent += emptyPara();

  // Date
  bodyContent += '<w:p><w:pPr><w:spacing w:line="520" w:lineRule="exact"/><w:jc w:val="center"/><w:rPr><w:rFonts w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:pPr><w:r><w:rPr><w:rFonts w:hint="eastAsia" w:ascii="\u4EFF\u5B8B_GB2312" w:eastAsia="\u4EFF\u5B8B_GB2312"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr><w:t>2026\u5E74  3  \u6708 6\u65E5</w:t></w:r></w:p>';

  bodyContent += emptyPara();

  // Section: 周记内容
  bodyContent += sectionTitle('\u5468\u8BB0\u5185\u5BB9');

  // Each week
  for (const w of weeks) {
    bodyContent += weekEntry(w.title, w.content);
    bodyContent += bodyText('\u4E0B\u5468\u8BA1\u5212\uFF1A' + w.plan);
  }

  // Page break before teacher comments
  bodyContent += emptyParaStyle6();
  bodyContent += emptyParaStyle6();
  bodyContent += emptyParaStyle6();
  bodyContent += emptyParaStyle6();

  // Teacher comment section
  bodyContent += sectionTitle('\u6307\u5BFC\u6559\u5E08\u610F\u89C1');
  bodyContent += emptyPara();
  bodyContent += emptyPara();
  bodyContent += emptyPara();
  bodyContent += signatureLine();
  bodyContent += dateLine();
  bodyContent += emptyPara();

  // Build the full document XML using the same namespace declarations as the template
  const nsDecl = 'xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" xmlns:w10="urn:schemas-microsoft-com:office:word" xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml" xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml" xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup" xmlns:wpi="http://schemas.microsoft.com/office/word/2010/wordprocessingInk" xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml" xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" xmlns:wpsCustomData="http://www.wps.cn/officeDocument/2013/wpsCustomData" mc:Ignorable="w14 w15 wp14"';

  const newDocXml = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' +
    '<w:document ' + nsDecl + '><w:body>' + bodyContent + sectPr + '</w:body></w:document>';

  // Create new docx using the template's supporting files
  const newZip = new JSZip();

  // Copy all template files except document.xml
  const templateFiles = Object.keys(templateZip.files);
  for (const file of templateFiles) {
    if (file === 'word/document.xml') continue;
    const fileData = templateZip.files[file];
    if (!fileData.dir) {
      newZip.file(file, await fileData.async('nodebuffer'));
    } else {
      newZip.folder(file);
    }
  }

  // Add our new document.xml
  newZip.file('word/document.xml', newDocXml);

  const buf = await newZip.generateAsync({ type: 'nodebuffer' });
  fs.writeFileSync('d:\\SwapU\\docs\\thesis\\\u6BD5\u4E1A\u8BBE\u8BA1\u5468\u8BB0.docx', buf);
  console.log('Done! File saved to d:\\SwapU\\docs\\thesis\\毕业设计周记.docx');
}

main().catch(console.error);
