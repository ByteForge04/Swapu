package com.swapu.config;

import com.swapu.service.ItemService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.MessageBuilder;

@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupRunner.class);

    @Autowired
    private ItemService itemService;

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (rocketMQTemplate != null) {
            try {
                // 尝试通过内置模板 topic (TBW102) 手动创建需要的 topic
                org.apache.rocketmq.client.producer.DefaultMQProducer producer = rocketMQTemplate.getProducer();
                producer.createTopic("TBW102", "item-update-topic", 4, new java.util.HashMap<>());
                logger.info("RocketMQ Topic 'item-update-topic' initialized using TBW102 successfully.");
            } catch (Exception e) {
                logger.warn("Failed to create RocketMQ Topic 'item-update-topic' via TBW102, will fallback to direct sync on send failure: {}", e.getMessage());
            }
            
            try {
                org.apache.rocketmq.client.producer.DefaultMQProducer producer = rocketMQTemplate.getProducer();
                producer.createTopic("TBW102", "order-timeout-topic", 4, new java.util.HashMap<>());
                logger.info("RocketMQ Topic 'order-timeout-topic' initialized using TBW102 successfully.");
            } catch (Exception e) {
                logger.warn("Failed to create RocketMQ Topic 'order-timeout-topic' via TBW102, will fallback to direct sync on send failure: {}", e.getMessage());
            }
        }

        logger.info("Starting ES data synchronization...");
        try {
            // itemService.syncAllToEs();
            logger.info("ES data synchronization skipped to prevent duplicate inserts on startup.");
        } catch (Exception e) {
            logger.error("Failed to sync data to ES: ", e);
        }
    }
}
