package com.swapu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swapu.entity.SearchHistory;
import com.swapu.mapper.SearchHistoryMapper;
import com.swapu.service.SearchHistoryService;
import org.springframework.stereotype.Service;

@Service
public class SearchHistoryServiceImpl extends ServiceImpl<SearchHistoryMapper, SearchHistory> implements SearchHistoryService {

    @Override
    public void recordSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        keyword = keyword.trim();
        if (keyword.length() > 100) {
            keyword = keyword.substring(0, 100);
        }

        QueryWrapper<SearchHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("keyword", keyword);
        SearchHistory history = getOne(queryWrapper);

        if (history != null) {
            history.setCount(history.getCount() + 1);
            updateById(history);
        } else {
            history = new SearchHistory();
            history.setKeyword(keyword);
            history.setCount(1);
            save(history);
        }
    }
}
