package com.swapu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swapu.entity.SearchHistory;

public interface SearchHistoryService extends IService<SearchHistory> {
    void recordSearch(String keyword);
}
