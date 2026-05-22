package com.swapu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

@Data
@TableName("item")
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    private Long userId;

    @NotNull(message = "分类不能为空")
    private Integer categoryId;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotNull(message = "价格不能为空")
    @Min(value = 0, message = "价格不能为负数")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String images; // JSON 字符串

    private Integer conditionRate;

    private Integer transactionMethod;

    private Integer status; // 0-待审核, 1-在售, 2-交易中, 3-已售出, 4-已下架

    private Integer viewCount;

    private Integer wantCount;

    private String campusArea;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    
    // 非数据库字段，用于查询时返回关联信息
    @TableField(exist = false)
    private User publisher;
    
    @TableField(exist = false)
    private String categoryName;

    // 搜索关键词
    @TableField(exist = false)
    private String keyword;

    // 价格区间
    @TableField(exist = false)
    private BigDecimal minPrice;

    @TableField(exist = false)
    private BigDecimal maxPrice;

    @TableField(exist = false)
    private String sortField;

    @TableField(exist = false)
    private String sortOrder;

    // 手动添加 Getter/Setter 方法以解决 Lombok 编译问题
    public Long getId() { return itemId; }
    public void setId(Long itemId) { this.itemId = itemId; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public Integer getConditionRate() { return conditionRate; }
    public void setConditionRate(Integer conditionRate) { this.conditionRate = conditionRate; }

    public Integer getTransactionMethod() { return transactionMethod; }
    public void setTransactionMethod(Integer transactionMethod) { this.transactionMethod = transactionMethod; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getWantCount() { return wantCount; }
    public void setWantCount(Integer wantCount) { this.wantCount = wantCount; }

    public String getCampusArea() { return campusArea; }
    public void setCampusArea(String campusArea) { this.campusArea = campusArea; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getPublisher() { return publisher; }
    public void setPublisher(User publisher) { this.publisher = publisher; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public String getSortField() { return sortField; }
    public void setSortField(String sortField) { this.sortField = sortField; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
}
