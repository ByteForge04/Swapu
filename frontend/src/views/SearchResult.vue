<template>
  <div class="search-page">
    <el-container>
      <el-header class="xy-header">
        <div class="header-content">
          <div class="logo-area" @click="$router.push('/')">
            <span class="logo-text">SwapU</span>
            <span class="logo-sub">搜索</span>
          </div>
          <div class="search-box">
            <el-autocomplete
              v-model="keyword"
              :fetch-suggestions="querySearchAsync"
              placeholder="搜一搜校园好物..."
              :trigger-on-focus="false"
              class="xy-search-input"
              @select="handleSelect"
              @keyup.enter="handleSearch"
              clearable
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon class="el-input__icon"><Search /></el-icon>
              </template>
            </el-autocomplete>
            <el-button type="primary" class="search-btn" @click="handleSearch">搜索</el-button>
          </div>
          <div class="header-right">
            <el-button type="default" link class="header-btn" @click="$router.push('/')">返回首页</el-button>
          </div>
        </div>
      </el-header>

      <el-main class="xy-main">
        <div class="content-wrapper">
          <div class="filter-bar">
            <div class="filter-group">
              <span class="label">分类：</span>
              <span 
                class="filter-item" 
                :class="{ active: categoryId === null }" 
                @click="handleCategoryChange(null)"
              >全部</span>
              <span 
                v-for="cat in categoryList"
                :key="cat.categoryId"
                class="filter-item" 
                :class="{ active: categoryId === cat.categoryId }" 
                @click="handleCategoryChange(cat.categoryId)"
              >{{ cat.categoryName }}</span>
            </div>
          </div>

          <div class="result-info">
            找到相关物品 <span class="count">{{ total }}</span> 件
          </div>

          <div class="item-list" v-loading="loading">
            <div 
              v-for="item in items" 
              :key="item.itemId" 
              class="item-card"
              @click="$router.push(`/item/${item.itemId}`)"
            >
              <div class="img-wrapper">
                <el-image :src="getMainImage(item.images)" class="item-img" fit="cover" lazy>
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="status-tag" v-if="item.status === 2">交易中</div>
                <div class="status-tag done" v-if="item.status === 3">已售出</div>
              </div>
              <div class="info-area">
                <div class="title" v-html="item.title"></div>
                <div class="price">￥{{ item.price }}</div>
                <div class="desc" v-html="item.description"></div>
                <div class="meta-row">
                  <span class="area"><el-icon><Location /></el-icon> {{ item.campusArea || '校内' }}</span>
                  <span class="want">{{ item.wantCount || 0 }}人想要</span>
                </div>
              </div>
            </div>
            
            <el-empty v-if="!loading && items.length === 0" description="换个关键词试试吧~" />
          </div>

          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="total"
              :page-size="pageSize"
              :current-page="currentPage"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { Search, Location, Picture } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const keyword = ref(route.query.keyword || '')

const querySearchAsync = (queryString, cb) => {
  if (!queryString) {
    cb([])
    return
  }
  request.get('/item/suggest', { params: { keyword: queryString } })
    .then(res => {
      if (res.code === 200 && res.data) {
        // el-autocomplete needs { value: 'xxx' } format
        const suggestions = res.data.map(item => ({ value: item }))
        cb(suggestions)
      } else {
        cb([])
      }
    })
    .catch(err => {
      console.error(err)
      cb([])
    })
}

const handleSelect = (item) => {
  keyword.value = item.value
  handleSearch()
}

const categoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : null)
const items = ref([])
const categoryList = ref([]) // New: dynamic categories
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)

const getMainImage = (images) => {
  if (!images) return 'https://via.placeholder.com/200'
  try {
    return JSON.parse(images)[0]
  } catch (e) {
    return images.split(',')[0] // Fallback in case it's not JSON
  }
}

const fetchSearch = async () => {
  loading.value = true
  try {
    const res = await request.get('/item/search', {
      params: {
        keyword: keyword.value,
        categoryId: categoryId.value,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      items.value = res.data.records
      total.value = Number(res.data.total)
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// Fetch categories from backend
const fetchCategories = async () => {
  try {
    const res = await request.get('/category/list')
    if (res.code === 200) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  // 更新路由参数以便分享，同时触发 watch
  router.replace({ 
    query: { 
      keyword: keyword.value, 
      categoryId: categoryId.value 
    } 
  })
  fetchSearch()
}

const handleCategoryChange = (id) => {
  categoryId.value = id
  handleSearch()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => route.query, (newQuery) => {
  if (newQuery.keyword !== keyword.value) {
    keyword.value = newQuery.keyword || ''
    fetchSearch()
  }
})

onMounted(() => {
  fetchCategories()
  fetchSearch()
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background-color: #f6f7f9;
}

.xy-header {
  background-color: #ffda44;
  height: 60px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo-area {
  display: flex;
  align-items: baseline;
  cursor: pointer;
  margin-right: 40px;
}

.logo-text {
  font-size: 24px;
  font-weight: 900;
  color: #333;
}

.logo-sub {
  font-size: 14px;
  color: #333;
  margin-left: 8px;
  font-weight: 500;
  opacity: 0.8;
  border-left: 1px solid #333;
  padding-left: 8px;
}

.search-box {
  flex: 1;
  max-width: 500px;
  display: flex;
  align-items: center;
}

.xy-search-input {
  flex: 1;
}

.search-btn {
  padding: 0 20px;
  height: 32px;
  margin-left: 4px;
}

.header-btn {
  color: #333 !important;
  font-weight: 500;
}

.header-btn:hover {
  color: #000 !important;
  text-decoration: underline;
}

.content-wrapper {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.filter-bar {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  margin-right: 15px;
  color: #666;
}

.filter-item {
  padding: 6px 16px;
  margin-right: 10px;
  cursor: pointer;
  border-radius: 20px;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

.filter-item:hover {
  color: #ffda44;
}

.filter-item.active {
  background: #ffda44;
  color: #333;
  font-weight: 500;
}

.result-info {
  margin-bottom: 15px;
  font-size: 14px;
  color: #666;
}

.count {
  color: #ff5000;
  font-weight: bold;
  margin: 0 4px;
}

.item-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.item-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #eee;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.08);
}

.img-wrapper {
  height: 220px;
  width: 100%;
  position: relative;
  background: #f9f9f9;
}

.item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.done {
  background: rgba(200, 200, 200, 0.9);
  color: #666;
}

.info-area {
  padding: 12px;
}

.title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 42px;
}

/* 高亮样式 */
:deep(span[style*='color:red']) {
  color: #ff5000 !important;
  font-weight: bold;
}

.price {
  font-size: 18px;
  color: #ff5000;
  font-weight: bold;
  margin-bottom: 8px;
}

.desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.area {
  display: flex;
  align-items: center;
  gap: 2px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  margin-bottom: 60px;
}

/* 移动端响应式适配 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 10px;
  }
  .logo-area {
    margin-right: 0;
    margin-bottom: 10px;
  }
  .search-box {
    width: 100%;
    max-width: none;
    margin-bottom: 10px;
  }
  .item-list {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 10px;
  }
  .img-wrapper {
    height: 140px;
  }
  .title {
    font-size: 13px;
    height: 36px;
  }
  .price {
    font-size: 16px;
  }
}
</style>
