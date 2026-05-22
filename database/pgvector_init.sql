-- 如果 Spring AI 的 initializeSchema: true 自动建表失败，请手动在 PostgreSQL 中执行以下 SQL

-- 注意：如果你还没有 postgres 数据库，通常 PostgreSQL 默认就会有一个 postgres 数据库。
-- 但如果你的项目需要在特定的数据库中运行（根据 application.yml，你使用的是 jdbc:postgresql://192.168.74.128:5432/postgres）
-- 那么你需要先连接到 postgres 数据库，或者执行以下建库语句（如果需要的话，注意 PostgreSQL 中不能在事务块内 CREATE DATABASE）：
-- CREATE DATABASE postgres;

-- 1. 连接到你的目标数据库 (如果是 Navicat，请确保当前选中的是 postgres 数据库)

-- 2. 确保安装了 vector 扩展 (这一步必须在目标数据库下执行)
CREATE EXTENSION IF NOT EXISTS vector;

-- 3. 创建向量表 (Spring AI PgVector 默认的表名和结构)
CREATE TABLE IF NOT EXISTS vector_store (
	id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
	content text,
	metadata jsonb,
	embedding vector(1024) -- 这里对应 bge-m3 模型的维度 1024
);

-- 4. 创建 HNSW 索引以加速查询 (可选但强烈推荐)
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store USING hnsw (embedding vector_cosine_ops);
