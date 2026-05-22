package com.swapu.listener;

import com.swapu.entity.mq.ItemSyncMessage;
import com.swapu.service.ItemService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "item-update-topic", consumerGroup = "item-sync-consumer-group")
public class ItemSyncListener implements RocketMQListener<ItemSyncMessage> {

    @Autowired
    private ItemService itemService;

    @Override
    public void onMessage(ItemSyncMessage message) {
        if (message == null || message.getItemId() == null) {
            return;
        }

        try {
            if (message.getType() == 1) {
                // 上架/更新
                itemService.doSyncToEs(message.getItemId());
            } else if (message.getType() == 2) {
                // 下架/删除
                itemService.doDeleteFromEs(message.getItemId());
            }
        } catch (Exception e) {
            // 简单重试或记录日志
            e.printStackTrace();
        }
    }
}
