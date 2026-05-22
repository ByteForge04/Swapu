package com.swapu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swapu.entity.ItemWant;
import org.apache.ibatis.annotations.Mapper;

import com.swapu.entity.Item;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ItemWantMapper extends BaseMapper<ItemWant> {
    
    @Select("SELECT i.* FROM item i JOIN item_want w ON i.item_id = w.item_id WHERE w.user_id = #{userId} ORDER BY w.created_at DESC")
    List<Item> selectMyWantItems(Long userId);
}
