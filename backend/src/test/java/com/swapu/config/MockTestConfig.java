package com.swapu.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Configuration
@Profile("test")
public class MockTestConfig {

    @Bean
    @Primary
    public RedissonClient redissonClient() {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        
        try {
            // 模拟锁总是成功获取
            Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                   .thenReturn(true);
            Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        return redissonClient;
    }

    @Bean
    @Primary
    public RocketMQTemplate rocketMQTemplate() {
        // 模拟 RocketMQ
        return Mockito.mock(RocketMQTemplate.class);
    }
}
