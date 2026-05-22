package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.entity.User;
import com.swapu.entity.es.ItemDoc;
import com.swapu.entity.mq.ItemSyncMessage;
import com.swapu.mapper.ItemMapper;
import com.swapu.mapper.ItemWantMapper;
import com.swapu.mapper.UserMapper;
import com.swapu.repository.ItemRepository;
import com.swapu.service.ItemService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import com.swapu.service.RagService;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
// import org.springframework.data.elasticsearch.core.suggest.Completion;
import com.swapu.utils.DeepSeekUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Service
@CacheConfig(cacheNames = "item")
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ItemWantMapper itemWantMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired(required = false)
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    
    @Autowired
    private com.swapu.service.SearchHistoryService searchHistoryService;

    @Autowired
    private DeepSeekUtils deepSeekUtils;

    @Autowired
    private RagService ragService;

    @Override
    @CacheEvict(allEntries = true)
    public Result<?> publish(Item item) {
        if (item.getUserId() == null) {
            return Result.error("发布者不能为空");
        }
        
        // AI 审核物品是否合规
        int initialStatus = 0; // 默认待审核
        String aiReason = null;
        try {
            log.info("Start AI check for item publish. Item Title: {}", item.getTitle());
            String aiResultStr = deepSeekUtils.checkItemPublish(item.getTitle(), item.getDescription());
            if (aiResultStr.startsWith("{")) {
                JSONObject aiResult = JSON.parseObject(aiResultStr);
                if (aiResult != null) {
                    if (!aiResult.getBooleanValue("allow")) {
                        aiReason = aiResult.getString("reason");
                        log.warn("Item publish rejected by AI. Title: {}, Reason: {}", item.getTitle(), aiReason);
                        initialStatus = 4; // 违规下架
                    } else {
                        log.info("Item publish approved by AI. Title: {}", item.getTitle());
                        initialStatus = 1; // 审核通过，直接上架
                    }
                }
            } else {
                log.warn("AI returned non-JSON format for item check: {}", aiResultStr);
            }
        } catch (Exception e) {
            log.error("AI check for item publish failed: {}", e.getMessage(), e);
            // 如果AI审核出错，暂不阻断发布流程，交由人工审核 (初始状态 0)
        }
        
        // 设置默认值
        item.setStatus(initialStatus); 
        item.setViewCount(0);
        item.setWantCount(0);
        
        save(item);

        // 如果审核不通过，直接返回错误，但记录已保存到数据库（状态为4）
        if (initialStatus == 4) {
            return Result.error("物品涉嫌违规被 AI 拦截: " + aiReason);
        }

        // 异步将商品信息存入向量数据库以供 RAG 检索
        if (initialStatus == 1) {
            CompletableFuture.runAsync(() -> {
                try {
                    ragService.addDocument(item);
                } catch (Exception e) {
                    log.error("Async RAG document addition failed for itemId: {}", item.getId(), e);
                }
            });

            // 同步到 ES
            syncToEs(item.getId());
        }
        
        return Result.success();
    }

    @Override
    public void syncToEs(Long itemId) {
        // 发送同步消息 (Type 1)
        try {
            ItemSyncMessage msg = new ItemSyncMessage();
            msg.setItemId(itemId);
            msg.setType(1);
            
            if (rocketMQTemplate != null) {
                // 使用底层 producer 发送，避免 RocketMQTemplate 内部自动打印刺眼的 ERROR 日志
                org.apache.rocketmq.common.message.Message mqMsg = new org.apache.rocketmq.common.message.Message(
                        "item-update-topic", 
                        com.alibaba.fastjson.JSON.toJSONBytes(msg)
                );
                rocketMQTemplate.getProducer().send(mqMsg, 3000); // 3秒超时
            }
        } catch (Exception e) {
            log.warn("MQ Send Warning (syncToEs): Topic 'item-update-topic' may not exist. Fallback to direct sync.");
            doSyncToEs(itemId);
        }
    }

    @Override
    public void deleteFromEs(Long itemId) {
        // 发送删除消息 (Type 2)
        try {
            ItemSyncMessage msg = new ItemSyncMessage();
            msg.setItemId(itemId);
            msg.setType(2);
            
            if (rocketMQTemplate != null) {
                org.apache.rocketmq.common.message.Message mqMsg = new org.apache.rocketmq.common.message.Message(
                        "item-update-topic", 
                        com.alibaba.fastjson.JSON.toJSONBytes(msg)
                );
                rocketMQTemplate.getProducer().send(mqMsg, 3000);
            }
        } catch (Exception e) {
            log.warn("MQ Send Warning (deleteFromEs): Topic 'item-update-topic' may not exist. Fallback to direct sync.");
            doDeleteFromEs(itemId);
        }
    }

    @Override
    public List<String> suggest(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        
        // 降级方案：使用 match_phrase_prefix 查询 title 字段
        Query searchQuery = NativeQuery.builder()
                .withQuery(q -> q.matchPhrasePrefix(m -> m.field("title").query(keyword)))
                .withPageable(PageRequest.of(0, 10))
                .build();
                
        SearchHits<ItemDoc> searchHits = elasticsearchOperations.search(searchQuery, ItemDoc.class);
        
        List<String> result = new ArrayList<>();
        for (SearchHit<ItemDoc> hit : searchHits) {
            result.add(hit.getContent().getTitle());
        }
        return result;
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ItemDoc> search(String keyword, Integer categoryId, Integer page, Integer size) {
        // Record search history
        if (StringUtils.hasText(keyword)) {
            try {
                searchHistoryService.recordSearch(keyword);
            } catch (Exception e) {
                // Ignore error
            }
        }
        
        Query searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    // 必须是在售状态
                    b.must(m -> m.term(t -> t.field("status").value(1)));
                    
                    if (categoryId != null) {
                        b.must(m -> m.term(t -> t.field("categoryId").value(categoryId)));
                    }
                    
                    if (StringUtils.hasText(keyword)) {
                        b.must(m -> m.bool(kb -> {
                            // 1. 分词匹配 (标准搜索)
                            kb.should(s -> s.match(ma -> ma.field("title").query(keyword)));
                            kb.should(s -> s.match(ma -> ma.field("description").query(keyword)));
                            
                            // 2. 通配符查询 (支持 * 和 ?)
                            if (keyword.contains("*") || keyword.contains("?")) {
                                kb.should(s -> s.wildcard(w -> w.field("title").value(keyword)));
                                kb.should(s -> s.wildcard(w -> w.field("description").value(keyword)));
                            }
                            return kb;
                        }));
                    }
                    return b;
                }))
                .withPageable(PageRequest.of(page - 1, size))
                .withHighlightQuery(
                    new org.springframework.data.elasticsearch.core.query.HighlightQuery(
                        new org.springframework.data.elasticsearch.core.query.highlight.Highlight(
                            new org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters.HighlightParametersBuilder()
                                .withPreTags("<span style='color:red'>")
                                .withPostTags("</span>")
                                .build(),
                            java.util.Arrays.asList(
                                new org.springframework.data.elasticsearch.core.query.highlight.HighlightField("title"),
                                new org.springframework.data.elasticsearch.core.query.highlight.HighlightField("description")
                            )
                        ),
                        ItemDoc.class
                    )
                )
                .build();
                
        SearchHits<ItemDoc> searchHits = elasticsearchOperations.search(searchQuery, ItemDoc.class);
        
        // ... rest of processing
        List<ItemDoc> list = new ArrayList<>();
        for (SearchHit<ItemDoc> hit : searchHits) {
            ItemDoc content = hit.getContent();
            List<String> titleHigh = hit.getHighlightField("title");
            if (titleHigh != null && !titleHigh.isEmpty()) {
                content.setTitle(titleHigh.get(0));
            }
            List<String> descHigh = hit.getHighlightField("description");
            if (descHigh != null && !descHigh.isEmpty()) {
                content.setDescription(descHigh.get(0));
            }
            list.add(content);
        }
        
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<ItemDoc>(page, size, searchHits.getTotalHits())
                .setRecords(list);
    }

    // 真正的 ES 操作逻辑
    public void doSyncToEs(Long itemId) {
        Item item = getById(itemId);
        if (item != null && item.getStatus() == 1) {
            ItemDoc itemDoc = new ItemDoc();
            BeanUtils.copyProperties(item, itemDoc);
            
            // 设置 Suggestion 字段
            // 使用标题作为补全输入
            // itemDoc.setSuggestion(new Completion(new String[]{item.getTitle()}));
            
            itemRepository.save(itemDoc);
            
            // 同时同步到向量数据库
            CompletableFuture.runAsync(() -> {
                try {
                    ragService.addDocument(item);
                } catch (Exception e) {
                    log.error("Sync RAG document addition failed for itemId: {}", item.getId(), e);
                }
            });
        }
    }
    
    // ... doDeleteFromEs ...

    @Override
    public void syncAllToEs() {
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        List<Item> items = list(queryWrapper);
        
        // 异步多线程执行全量同步，防止请求超时
        CompletableFuture.runAsync(() -> {
            // 清除 ES 中旧数据
            itemRepository.deleteAll();
            
            List<ItemDoc> docs = new ArrayList<>();
            for (Item item : items) {
                ItemDoc doc = new ItemDoc();
                BeanUtils.copyProperties(item, doc);
                
                // 设置 Suggestion
                // doc.setSuggestion(new Completion(new String[]{item.getTitle()}));
                
                docs.add(doc);
                
                // 全量同步到向量数据库
                try {
                    ragService.addDocument(item);
                } catch (Exception e) {
                    log.error("Full sync RAG document addition failed for itemId: {}", item.getId(), e);
                }
            }
            
            if (!docs.isEmpty()) {
                itemRepository.saveAll(docs);
            }
            log.info("Full ES and Vector DB sync completed. Total items: {}", docs.size());
        });
    }

    public void doDeleteFromEs(Long itemId) {
        itemRepository.deleteById(itemId);
    }

    private void syncToEs(Item item) {
        // Deprecated method, kept for compatibility if needed, or remove
    }

    @Override
    public Result<List<Item>> listItems(Item item) {
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        // 只查询已上架的物品
        queryWrapper.eq("status", 1);
        
        // 如果传入了 userId，说明是查询特定用户的商品
        if (item.getUserId() != null) {
            queryWrapper.eq("user_id", item.getUserId());
        }

        if (StringUtils.hasText(item.getTitle())) {
            queryWrapper.like("title", item.getTitle());
        }
        if (item.getCategoryId() != null) {
            queryWrapper.eq("category_id", item.getCategoryId());
        }
        
        queryWrapper.orderByDesc("created_at");
        
        List<Item> items = list(queryWrapper);
        // 填充发布者信息
        for (Item it : items) {
            User user = userMapper.selectById(it.getUserId());
            if (user != null) {
                user.setPassword(null);
                it.setPublisher(user);
            }
        }
        return Result.success(items);
    }

    @Override
    public Result<List<Item>> listItemsWithFilters(String keyword, Integer categoryId, Integer conditionRate, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String sortField, String sortOrder, Long userId) {
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        // 只查询已上架的物品
        queryWrapper.eq("status", 1);

        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("description", keyword));
        }
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }
        if (conditionRate != null) {
            queryWrapper.ge("condition_rate", conditionRate);
        }
        if (minPrice != null) {
            queryWrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le("price", maxPrice);
        }

        // 排序处理
        if (StringUtils.hasText(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            if ("price".equals(sortField)) {
                if (isAsc) {
                    queryWrapper.orderByAsc("price");
                } else {
                    queryWrapper.orderByDesc("price");
                }
            } else if ("want".equals(sortField)) {
                queryWrapper.orderByDesc("want_count");
            } else {
                queryWrapper.orderByDesc("created_at");
            }
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        List<Item> items = list(queryWrapper);
        // 填充发布者信息
        for (Item item : items) {
            User user = userMapper.selectById(item.getUserId());
            if (user != null) {
                user.setPassword(null);
                item.setPublisher(user);
            }
        }

        return Result.success(items);
    }

    @Override
    // @Cacheable(key = "#id") // 移除缓存以确保每次都能正确更新浏览量
    public Result<Item> getItemDetail(Long id) {
        Item item = getById(id);
        if (item == null) {
            return Result.error("物品不存在");
        }
        
        // 增加浏览量 (注意：如果用了缓存，浏览量可能不会实时更新到数据库，或者需要单独处理浏览量更新不走缓存)
        // 简单起见，这里先不处理浏览量缓存问题，或者将浏览量更新操作剥离出去
        item.setViewCount(item.getViewCount() + 1);
        updateById(item);
        
        // 填充发布者信息
        User user = userMapper.selectById(item.getUserId());
        if (user != null) {
            user.setPassword(null);
            item.setPublisher(user);
        }
        
        return Result.success(item);
    }

    @Override
    @CacheEvict(allEntries = true)
    public Result<?> updateStatus(Long userId, Long itemId, Integer status) {
        Item item = getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }
        if (!item.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        
        item.setStatus(status);
        updateById(item);
        
        // 状态变更，从 ES 删除
        deleteFromEs(itemId);
        
        return Result.success();
    }

    @Override
    @CacheEvict(allEntries = true)
    public Result<?> deleteItem(Long userId, Long itemId) {
        Item item = getById(itemId);
        if (item == null) {
            return Result.error("物品不存在");
        }
        if (!item.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        removeById(itemId);
        deleteFromEs(itemId);
        return Result.success();
    }
    
    @Override
    @CacheEvict(allEntries = true)
    public Result<?> updateItem(Item item) {
        if (item.getItemId() == null) {
            return Result.error("物品ID不能为空");
        }
        Item oldItem = getById(item.getItemId());
        if (oldItem == null) {
            return Result.error("物品不存在");
        }
        if (!oldItem.getUserId().equals(item.getUserId())) {
            return Result.error("无权操作");
        }
        
        // 修改后需要重新审核
        item.setStatus(0);
        
        updateById(item);
        deleteFromEs(oldItem.getItemId());
        return Result.success();
    }

    @Override
    public Result<List<Item>> listMyItems(Long userId) {
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("created_at");
        return Result.success(list(queryWrapper));
    }

    @Override
    public Result<List<Item>> listMyWants(Long userId) {
        return Result.success(itemWantMapper.selectMyWantItems(userId));
    }
}
