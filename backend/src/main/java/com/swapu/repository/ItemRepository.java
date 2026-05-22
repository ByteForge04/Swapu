package com.swapu.repository;

import com.swapu.entity.es.ItemDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends ElasticsearchRepository<ItemDoc, Long> {
}
