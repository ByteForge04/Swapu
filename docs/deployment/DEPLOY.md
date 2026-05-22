# SwapU 项目部署文档

本文档介绍如何使用 Docker Compose 部署 SwapU 校园二手交易平台。

## 前置要求

- 确保服务器已安装 [Docker](https://docs.docker.com/get-docker/) 和 [Docker Compose](https://docs.docker.com/compose/install/)。
- 确保端口 80, 8080, 3306, 6379, 9200, 9876 未被占用。

## 部署步骤

1. **克隆代码**
   ```bash
   git clone <repository_url>
   cd SwapU
   ```

2. **配置参数**
   - 后端配置位于 `backend/src/main/resources/application-prod.yml`。
   - 默认使用 Docker 内部网络连接，无需修改数据库等连接地址。
   - **重要**：如果需要支付宝支付功能，请在 `docker-compose.yml` 的 `backend` 服务中添加环境变量：
     ```yaml
     environment:
       - ALIPAY_APP_ID=你的AppID
       - ALIPAY_PRIVATE_KEY=你的应用私钥
       - ALIPAY_PUBLIC_KEY=支付宝公钥
       - ALIPAY_NOTIFY_URL=http://你的服务器IP/pay/notify
       - ALIPAY_RETURN_URL=http://你的服务器IP/pay/return
     ```

3. **启动服务**
   在项目根目录下运行：
   ```bash
   docker-compose up -d --build
   ```
   首次运行需要下载镜像和构建项目，可能需要几分钟时间。

4. **验证部署**
   - 前端访问：`http://localhost` (或服务器IP)
   - 后端接口：`http://localhost:8080` (或服务器IP:8080)
   - 数据库管理：可以使用 Navicat 连接 `localhost:3306` (密码: root)

## 常用命令

- **停止服务**
  ```bash
  docker-compose down
  ```

- **查看日志**
  ```bash
  docker-compose logs -f
  ```
  查看特定服务日志（如后端）：
  ```bash
  docker-compose logs -f backend
  ```

- **重新构建**
  如果修改了代码，需要重新构建镜像：
  ```bash
  docker-compose up -d --build
  ```

## 注意事项

1. **文件上传路径**
   默认上传的文件会存储在 Docker 卷 `upload_data` 中，数据持久化，不会随容器删除丢失。

2. **Elasticsearch 内存**
   ES 默认配置了 512MB 堆内存，如果服务器内存较小，可能需要调整 `docker-compose.yml` 中的 `ES_JAVA_OPTS`。

3. **RocketMQ**
   Broker 配置了 `autoCreateTopicEnable=true`，应用启动时会自动创建所需 Topic。

## 数据迁移

首次启动时，MySQL 数据库为空。
请手动执行 SQL 脚本初始化数据库：
1. 连接 MySQL。
2. 执行 `database/sys_user.sql` 等建表脚本。
   (或者在 docker-compose.yml 中挂载初始化脚本到 /docker-entrypoint-initdb.d/)

目前 `docker-compose.yml` 已配置挂载 `./database/init.sql`，请确保该文件存在并包含完整的建表语句。如果项目中有多个 sql 文件，建议合并为一个 `init.sql` 放在 `database` 目录下。
