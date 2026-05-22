package com.swapu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.embedding.EmbeddingClient;

import javax.sql.DataSource;

@Configuration
public class PgVectorConfig {

    @Value("${spring.pgvector.datasource.url}")
    private String url;

    @Value("${spring.pgvector.datasource.username}")
    private String username;

    @Value("${spring.pgvector.datasource.password}")
    private String password;

    @Value("${spring.pgvector.datasource.driver-class-name}")
    private String driverClassName;

    // 解决多数据源导致 MyBatis Plus 默认找到 pgDataSource 的问题
    // 最好的办法是不把 PostgreSQL 的 DataSource 注册成 Spring 的 @Bean
    // 这样 Spring Boot 的默认数据源自动配置就不会被打断，MyBatis 依然会完美使用 MySQL

    @Bean(name = "pgJdbcTemplate")
    public JdbcTemplate pgJdbcTemplate() {
        // 在这里直接实例化 PostgreSQL 数据源，只给 pgJdbcTemplate 使用
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return new JdbcTemplate(dataSource);
    }

    // 手动注册 PgVectorStore，因为 Starter 默认会用主数据源(MySQL)，这会导致建表失败
    @Bean
    public PgVectorStore pgVectorStore(JdbcTemplate pgJdbcTemplate, @org.springframework.beans.factory.annotation.Qualifier("ollamaEmbeddingClient") EmbeddingClient embeddingClient) {
        return new PgVectorStore(pgJdbcTemplate, embeddingClient, 
            1024, // 维度
            PgVectorStore.PgDistanceType.COSINE_DISTANCE, 
            true, // initializeSchema
            PgVectorStore.PgIndexType.HNSW
        );
    }
}