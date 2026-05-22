package com.swapu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.common.Result;
import com.swapu.entity.Item;
import com.swapu.entity.es.ItemDoc;

import java.util.List;

public interface ItemService extends IService<Item> {
    Result<?> publish(Item item);
    Result<List<Item>> listItems(Item item);
    Result<List<Item>> listItemsWithFilters(String keyword, Integer categoryId, Integer conditionRate, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, String sortField, String sortOrder, Long userId);
    Result<Item> getItemDetail(Long id);
    Result<List<Item>> listMyItems(Long userId);
    Result<List<Item>> listMyWants(Long userId);
    Result<?> updateStatus(Long userId, Long itemId, Integer status);
    Result<?> deleteItem(Long userId, Long itemId);
    
    /**
     * 更新物品信息 (修改后需重新审核)
     */
    Result<?> updateItem(Item item);
    
    // ES Search
    Page<ItemDoc> search(String keyword, Integer categoryId, Integer page, Integer size);
    
    // ES Suggest
    List<String> suggest(String keyword);
    
    // Admin ES Sync
    void syncToEs(Long itemId);
    void deleteFromEs(Long itemId);
    void syncAllToEs();
    
    // Internal use for MQ
    void doSyncToEs(Long itemId);
    void doDeleteFromEs(Long itemId);
}
