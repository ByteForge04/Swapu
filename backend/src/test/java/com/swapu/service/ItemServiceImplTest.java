package com.swapu.service;

import com.swapu.entity.es.ItemDoc;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @MockBean
    private ElasticsearchOperations elasticsearchOperations;

    @Test
    public void testSearchWithMockedES() {
        // 准备 Mock 数据
        ItemDoc mockItemDoc = new ItemDoc();
        mockItemDoc.setId(1L);
        mockItemDoc.setTitle("Mock Title");
        mockItemDoc.setDescription("Mock Description");

        SearchHit<ItemDoc> mockHit = new SearchHit<>(
                "index",
                "1",
                null,
                1.0f,
                null,
                null,
                null,
                null,
                null,
                null,
                mockItemDoc
        );

        SearchHits<ItemDoc> mockHits = new SearchHitsImpl<>(
                1L,
                org.springframework.data.elasticsearch.core.TotalHitsRelation.EQUAL_TO,
                1.0f,
                "scrollId",
                Collections.singletonList(mockHit),
                null,
                null
        );

        Mockito.when(elasticsearchOperations.search(ArgumentMatchers.any(Query.class), ArgumentMatchers.eq(ItemDoc.class)))
                .thenReturn(mockHits);

        // 调用业务方法
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ItemDoc> result = 
                itemService.search("keyword", null, 1, 10);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("Mock Title", result.getRecords().get(0).getTitle());
    }
}