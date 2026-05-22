package com.swapu.service.impl;

import com.swapu.entity.ItemCategory;
import com.swapu.mapper.ItemCategoryMapper;
import com.swapu.service.ItemCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ItemCategoryServiceImpl extends ServiceImpl<ItemCategoryMapper, ItemCategory> implements ItemCategoryService {
}
