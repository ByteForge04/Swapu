# SwapU 校园二手交易平台

![Vue3](https://img.shields.io/badge/Vue.js-3.0-green.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)
![Spring AI](https://img.shields.io/badge/Spring%20AI-0.8.0-blue.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

SwapU 是一个基于大模型（RAG架构）与分布式中间件体系的现代化校园二手物品交易平台。它不仅提供了传统的商品发布、搜索、购买、聊天等核心功能，还创新性地引入了 **AI 智能导购助手** 与 **AI 一键文案生成**，为用户提供智能化的交易体验。

---

## 🌟 核心特性

- 🤖 **AI 智能导购 (RAG)**：基于 Spring AI 与 PgVector 向量数据库，实现基于自然语言的商品语义检索与推荐。
- ✍️ **AI 一键文案**：调用大模型一键润色商品发布描述，提高商品吸引力。
- 🔍 **多维混合搜索**：基于 Elasticsearch 实现商品标题与描述的全文检索、通配符查询与前缀补全。
- 🔒 **并发防超卖**：基于 Redisson 分布式锁，严格保证高并发场景下的商品库存安全。
- ⏳ **延迟取消订单**：基于 RocketMQ 延迟消息队列，实现订单 30 分钟未支付自动取消。
- 💬 **实时即时通讯**：基于 WebSocket 与 JWT 鉴权，实现买卖双方的实时在线聊天。
- 💰 **支付宝沙箱支付**：集成支付宝当面付，实现真实的支付与异步回调流程。

---

## 🛠 技术栈

### 前端 (Frontend)
- **核心框架**: Vue 3 (Composition API) + Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **UI 组件库**: Element Plus
- **网络请求**: Axios
- **CSS 预处理器**: SCSS

### 后端 (Backend)
- **核心框架**: Spring Boot 3.2 + JDK 17
- **持久层**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **缓存与分布式锁**: Redis + Redisson
- **消息队列**: RocketMQ 5.x
- **搜索引擎**: Elasticsearch 8.x
- **AI 框架**: Spring AI
- **向量数据库**: PostgreSQL + PgVector插件
- **大模型接口**: OpenAI 兼容接口 / Ollama 本地部署
- **对象存储**: 本地文件系统 (可无缝切换 MinIO/阿里云 OSS)

---

## 🚀 快速启动

### 1. 环境准备
请确保您的开发环境中已安装并启动以下服务：
- JDK 17+
- Node.js 18+
- MySQL 8.0
- Redis
- RocketMQ (NameServer & Broker)
- Elasticsearch 
- PostgreSQL (需安装 PgVector 插件)

### 2. 数据库初始化
1. 创建 MySQL 数据库 `swapu`。
2. 执行 `database/init.sql` 中的建表脚本与初始数据。
3. 创建 PostgreSQL 数据库 `swapu_vector`，并执行 `CREATE EXTENSION vector;`。

### 3. 后端配置与启动
1. 进入 `backend` 目录。
2. 复制或修改 `src/main/resources/application.yml`，更新数据库、Redis、RocketMQ、ES 等连接信息。
3. 配置 Spring AI 的 `api-key` 或本地 Ollama 地址。
4. 运行 `SwapuApplication.java` 启动后端服务 (默认端口 8080)。

### 4. 前端配置与启动
1. 进入 `frontend` 目录。
2. 安装依赖：`npm install`
3. 启动开发服务器：`npm run dev`
4. 访问控制台输出的本地地址 (默认 http://localhost:5173)。

---

## 📸 效果截图

> *（注：此处可在实际演示后补充项目运行截图）*

- **首页瀑布流与智能搜索**
- **商品详情与下单流程**
- **AI 智能导购对话**
- **WebSocket 实时聊天**
- **管理员数据看板**

---

## 📄 文档导航

- [需求分析文档](docs/requirements/REQUIREMENTS.md)
- [部署文档](docs/deployment/DEPLOY.md)
- [核心架构设计](docs/architecture/SWAPU_ADVANCED_TECH_ARCHITECTURE.md)
- [答辩技术点](docs/architecture/DEFENSE_TECH_POINTS.md)
- [API 接口文档](docs/api/API_DOC.md)

---

## 📄 许可证

本项目基于 MIT 协议开源，仅供学习交流与毕业设计参考使用。