# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SwapU is a campus second-hand trading platform (校园二手交易平台) built with Vue 3 (frontend) and Spring Boot 3.2 (backend). It features AI-powered semantic search (RAG with PgVector), real-time WebSocket chat, Alipay sandbox payments, and distributed locking for inventory safety.

## Commands

### Frontend (`frontend/`)

```bash
cd frontend
npm install          # Install dependencies
npm run dev          # Start dev server (port 3001, with proxy to :8080)
npm run build        # Production build
npm run preview      # Preview production build
npm run tunnel       # Expose local server via localtunnel (port 3001)
```

### Backend (`backend/`)

```bash
# Maven (requires JDK 17)
mvn spring-boot:run     # Start backend (port 8080)
mvn test                # Run tests (uses H2 in-memory database)
mvn clean package       # Build JAR

# The main class is: com.swapu.SwapUApplication
```

### Database

```bash
# MySQL initialization
mysql -u root -p < database/init.sql

# PostgreSQL vector DB setup
psql -d postgres -c "CREATE EXTENSION vector;"
psql -d swapu_vector -f database/pgvector_init.sql

# Mock data
mysql -u root -p swapu < database/mock_data.sql
```

## Architecture

### Backend (`backend/src/main/java/com/swapu/`)

**Layer pattern**: `controller` -> `service`/`service/impl` -> `mapper` (MyBatis-Plus), with `entity` as domain objects.

- **`config/`** — Key configurations:
  - `WebConfig` — CORS (allow all origins), interceptors (LoginInterceptor for auth, AdminInterceptor for `/admin/**`), static file mapping (`/files/**` -> local uploads dir)
  - `PgVectorConfig` — Manually constructs `PgVectorStore` bean using a separate PostgreSQL `JdbcTemplate` (NOT registered as a `DataSource` bean, to avoid breaking MyBatis-Plus's MySQL auto-config)
  - `WebSocketConfig` — Exports `ServerEndpointExporter` for JSR 356 WebSocket
  - `RestClientConfig` — 60s connect / 120s read timeouts for AI calls
  - `RedissonConfig`, `AlipayConfig`, `StartupRunner`, `MybatisPlusConfig`

- **`common/Result<T>`** — Unified API response wrapper: `{ code, msg, data }`. Success = code 200. Frontend axios interceptor redirects to `/login` on code 401.

- **Auth flow**: JWT tokens (io.jsonwebtoken 0.12.5). On login, backend returns `{ token, user }`. Frontend stores merged object in `localStorage.user`. Axios request interceptor sends `Authorization: <token>` header. `LoginInterceptor` validates token for all paths except configured exclusions (login, register, item list/detail, WebSocket, AI endpoints, etc.).

- **AI / RAG pipeline** (`ChatController`, `RagService`/`RagServiceImpl`):
  1. User sends message to `POST /ai/chat`
  2. `RagService.retrieveRelevantItemDetails()` does PgVector similarity search (COSINE_DISTANCE, HNSW index, 1024 dimensions) using Ollama `bge-m3` embeddings
  3. Retrieved results injected into system prompt
  4. DeepSeek (`deepseek-chat`, OpenAI-compatible) generates response
  5. Returns `{ text, items }` — AI text + product cards for frontend
  - `POST /ai/polish` — One-shot LLM call to polish product descriptions

- **WebSocket chat** (`controller/ws/ChatWebSocketEndpoint`):
  - `@ServerEndpoint("/ws/chat/{userId}")` with JWT token auth via query param
  - `ConcurrentHashMap<Long, ChatWebSocketEndpoint>` for online user tracking
  - Messages persisted via `ChatMessageMapper`, then forwarded to receiver if online
  - Heartbeat: client sends "ping", server replies "pong"

- **Distributed lock** (`TradeOrderServiceImpl`): Uses Redisson `RLock` to prevent overselling — locks on item ID before deducting stock within a `TransactionTemplate` boundary.

- **Order timeout** (`OrderTimeoutListener`, RocketMQ): On order creation, sends `OrderTimeoutMessage` with delay level (30min). Listener checks if order still unpaid (`status=0, paymentStatus=0`) and auto-cancels.

- **Elasticsearch** (`entity/es/ItemDoc`, `repository/ItemRepository`): Spring Data Elasticsearch repository for full-text search. Items synced to ES via RocketMQ (`ItemSyncMessage` -> `ItemSyncListener`).

- **Exception handling**: `GlobalExceptionHandler` (@RestControllerAdvice) catches `CustomException`, validation exceptions, and generic `Exception` -> unified `Result` format.

### Frontend (`frontend/src/`)

- **`router/index.js`** — Vue Router 4 with route meta (`requiresAuth`, `requiresAdmin`). Navigation guard checks `localStorage.user.token` and role (role 1 = admin). Admin routes under `/admin` with `AdminLayout.vue` wrapper.

- **`stores/user.js`** — Pinia store for auth state. Login merges `token` + `user` fields into one object stored in `localStorage`.

- **`utils/request.js`** — Axios instance with base URL `/api` (proxied by Vite to `localhost:8080`, rewriting `/api` prefix). Timeout 60s for AI endpoints.

- **Key views**: `Home.vue`, `ItemDetail.vue`, `SearchResult.vue`, `AiAssistant.vue`, `Chat.vue`, `Publish.vue`, `OrderList.vue`, `Profile.vue`, `Login.vue`, `Register.vue`, plus admin views under `views/admin/`.

- **Vite proxy** (`vite.config.js`): `/api` -> `http://localhost:8080` (strip `/api` prefix), `/files` -> `http://localhost:8080`, `/ws` -> `ws://localhost:8080` (WebSocket).

### Infrastructure

- **MySQL** — Primary data (users, items, orders, chat messages, etc.)
- **PostgreSQL + PgVector** — AI vector embeddings (separate datasource, not a Spring bean)
- **Redis** — Caching + Redisson distributed locks
- **Elasticsearch** — Full-text product search
- **RocketMQ** — Async messaging (ES sync, order timeout)
- **Ollama** — Local embedding model (`bge-m3`)
- **DeepSeek API** — LLM for chat and text polishing (OpenAI-compatible endpoint)

## Key Files

| File | Role |
|------|------|
| `backend/src/main/resources/application.yml` | Main config with all service connections |
| `database/init.sql` | MySQL schema + seed data |
| `database/pgvector_init.sql` | PostgreSQL vector table setup |
| `docs/architecture/SWAPU_ADVANCED_TECH_ARCHITECTURE.md` | Detailed architecture docs |
| `docs/api/API_DOC.md` | API documentation |
| `docs/deployment/DEPLOY.md` | Deployment guide |

## Important Notes

- The application.yml contains hardcoded credentials — do not commit to public repos.
- Backend uses JDK 17 with Spring Boot 3.2.4 and Spring AI 0.8.1 (milestone releases).
- PostgreSQL datasource is intentionally NOT registered as a Spring bean (`DriverManagerDataSource` in `PgVectorConfig`) to prevent MyBatis-Plus from using it instead of MySQL.
- Frontend dev server runs on port 3001 (not the default 5173) to work with the localtunnel setup.
- Tests use H2 in-memory database; production uses MySQL. The `application-prod.yml` is available for production overrides.
