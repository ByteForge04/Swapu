package com.swapu.service.impl;

import com.swapu.entity.Item;
import com.swapu.mapper.ItemMapper;
import com.swapu.service.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RagServiceImpl implements RagService {

    private static final Logger log = LoggerFactory.getLogger(RagServiceImpl.class);

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public void addDocument(Item item) {
        if (item == null) return;
        
        try {
            // 将商品标题和描述拼接作为向量内容
            String content = String.format("商品名称: %s\n商品描述: %s", item.getTitle(), item.getDescription());
            
            // 构建文档元数据，为了前端展示卡片，需要尽可能多的信息
            Document document = new Document(content, Map.of(
                    "itemId", item.getId(),
                    "title", item.getTitle(),
                    "price", item.getPrice() != null ? item.getPrice().doubleValue() : 0.0,
                    "description", item.getDescription() != null ? item.getDescription() : "",
                    "images", item.getImages() != null ? item.getImages() : "[]"
            ));
            
            vectorStore.add(List.of(document));
            log.info("Successfully added item to Vector DB. ItemId: {}", item.getId());
        } catch (Exception e) {
            log.error("Failed to add item to Vector DB. ItemId: {}", item.getId(), e);
        }
    }

    @Override
    public List<String> retrieveRelevantItems(String query, int topK) {
        try {
            log.info("RAG search query: {}, topK: {}", query, topK);
            // 修改 similarityThreshold，默认为 0.0，允许返回所有计算出相似度的结果（由 topK 限制数量）
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.query(query).withTopK(topK).withSimilarityThreshold(0.0)
            );
            
            log.info("RAG found {} documents", documents.size());
            return documents.stream()
                    .map(Document::getContent)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to retrieve relevant items for query: {}", query, e);
            return List.of();
        }
    }

    @Override
    public List<Map<String, Object>> retrieveRelevantItemDetails(String query, int topK) {
        try {
            log.info("【RAG 服务】开始向量检索。查询词: [{}], 预期返回数量: {}", query, topK);
            List<Document> documents = vectorStore.similaritySearch(
                    SearchRequest.query(query).withTopK(topK).withSimilarityThreshold(0.0)
            );
            
            log.info("【RAG 服务】检索完成，底层共匹配到 {} 条向量文档", documents.size());
            if (documents.isEmpty()) {
                return List.of();
            }

            Set<Long> availableItemIds = itemMapper.selectBatchIds(
                    documents.stream()
                            .map(doc -> doc.getMetadata().get("itemId"))
                            .filter(java.util.Objects::nonNull)
                            .map(id -> {
                                if (id instanceof Number number) {
                                    return number.longValue();
                                }
                                return Long.parseLong(String.valueOf(id));
                            })
                            .collect(Collectors.toSet())
            ).stream()
                    .filter(item -> item != null && item.getStatus() != null && item.getStatus() == 1)
                    .map(Item::getItemId)
                    .collect(Collectors.toSet());

            return documents.stream()
                    .filter(doc -> {
                        Object itemId = doc.getMetadata().get("itemId");
                        if (itemId == null) {
                            return false;
                        }
                        Long normalizedItemId = itemId instanceof Number number
                                ? number.longValue()
                                : Long.parseLong(String.valueOf(itemId));
                        return availableItemIds.contains(normalizedItemId);
                    })
                    .map(doc -> {
                        Map<String, Object> metadata = doc.getMetadata();
                        // 创建一个新的 Map 来包含 content 和 metadata，方便前端渲染卡片
                        Map<String, Object> itemDetail = new java.util.HashMap<>(metadata);
                        itemDetail.put("content", doc.getContent());
                        return itemDetail;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("【RAG 服务】检索商品详情时发生异常! 查询词: {}", query, e);
            return List.of();
        }
    }
}
