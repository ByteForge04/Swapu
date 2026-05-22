package com.swapu.service;

import com.swapu.entity.Item;

import java.util.List;
import java.util.Map;

public interface RagService {
    
    /**
     * 将商品信息存入向量数据库
     * @param item 商品信息
     */
    void addDocument(Item item);

    /**
     * 根据用户输入的查询词检索最相关的商品
     * @param query 用户查询词
     * @param topK 检索数量
     * @return 检索到的商品描述列表
     */
    List<String> retrieveRelevantItems(String query, int topK);

    /**
     * 根据用户输入的查询词检索最相关的商品详情（包含完整的元数据）
     * @param query 用户查询词
     * @param topK 检索数量
     * @return 检索到的商品完整数据列表
     */
    List<Map<String, Object>> retrieveRelevantItemDetails(String query, int topK);
}