<template>
  <div class="common-layout">
    <el-container>
      <el-header class="xy-header">
        <div class="header-content">
          <div class="header-left">
            <div class="logo-area" @click="$router.push('/')">
              <span class="logo-text">SwapU</span>
              <span class="logo-sub">校园闲置</span>
            </div>
            <!-- AI 助手导航项 (闲鱼风) -->
            <div class="ai-nav-item" @click="$router.push('/ai-assistant')">
              <el-icon class="ai-nav-icon"><ChatDotRound /></el-icon>
              <span class="ai-nav-text">AI 助手</span>
              <div class="ai-badge">智能</div>
            </div>
          </div>
          <div class="header-right">
            <div class="search-box">
              <el-autocomplete
                v-model="searchKeyword"
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

            <el-button 
                type="primary" 
                class="publish-entry-btn" 
                icon="Plus" 
                round
                @click="$router.push('/publish')"
              >
                我要发布
              </el-button>
              
              <div class="notification-area" v-if="userStore.user.username" @click="$router.push('/chat')" style="margin-right: 15px;">
                <el-badge :value="userStore.unreadChatCount" :hidden="userStore.unreadChatCount === 0" class="item">
                  <el-icon class="bell-icon"><ChatDotRound /></el-icon>
                </el-badge>
              </div>

              <div class="notification-area" v-if="userStore.user.username" @click="$router.push('/notification')">
                <el-badge :value="userStore.unreadCount" :hidden="userStore.unreadCount === 0" class="item">
                  <el-icon class="bell-icon"><Bell /></el-icon>
                </el-badge>
              </div>

            <div class="user-actions">
              <div class="user-info" v-if="userStore.user.username">
                <el-dropdown>
                  <span class="el-dropdown-link">
                    <el-avatar :size="32" icon="UserFilled" class="user-avatar" />
                    <span class="username">{{ userStore.user.nickname || userStore.user.username }}</span>
                    <el-icon class="el-icon--right"><arrow-down /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                      <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
              <div class="guest-actions" v-else>
                <router-link to="/login" class="nav-link">登录</router-link>
                <span class="divider">/</span>
                <router-link to="/register" class="nav-link">注册</router-link>
              </div>
            </div>
          </div>
        </div>
      </el-header>
      <el-main class="xy-main">
        <div class="banner-section">
          <h1>发现校园宝藏</h1>
          <p>让闲置游起来，让生活更精彩</p>
          
          <!-- 公告栏 -->
          <div class="announcement-bar" v-if="announcements.length > 0">
            <el-carousel height="40px" direction="vertical" indicator-position="none" :interval="3000">
              <el-carousel-item v-for="item in announcements" :key="item.announcementId">
                <div class="announcement-item" @click="showAnnouncement(item)">
                  <el-icon><Bell /></el-icon>
                  <span class="announcement-title">{{ item.title }}</span>
                  <span class="announcement-date">{{ formatDate(item.createdAt) }}</span>
                </div>
              </el-carousel-item>
            </el-carousel>
            <div class="more-link" @click="$router.push('/announcement/list')">查看全部 ></div>
          </div>
        </div>
        
        <div class="content-section">
          <h2>
            <span class="section-title">新鲜发布</span>
            <span class="section-subtitle">RECOMMEND</span>
          </h2>
          
          <!-- 筛选栏 -->
          <div class="filter-section">
            <div class="filter-group">
              <span class="filter-label">分类：</span>
              <el-radio-group v-model="filterForm.categoryId" @change="handleFilter">
                <el-radio-button :label="null">全部</el-radio-button>
                <el-radio-button 
                  v-for="cat in categoryList" 
                  :key="cat.categoryId" 
                  :label="cat.categoryId"
                >{{ cat.categoryName }}</el-radio-button>
              </el-radio-group>
            </div>
            
            <div class="filter-row">
              <div class="filter-group">
                <span class="filter-label">排序：</span>
                <el-radio-group v-model="filterForm.sort" @change="handleFilter">
                  <el-radio-button label="default">最新</el-radio-button>
                  <el-radio-button label="price_asc">价格↑</el-radio-button>
                  <el-radio-button label="price_desc">价格↓</el-radio-button>
                  <el-radio-button label="want">最热</el-radio-button>
                </el-radio-group>
              </div>

              <div class="filter-group">
                <span class="filter-label">成色：</span>
                <el-select v-model="filterForm.conditionRate" placeholder="不限" clearable @change="handleFilter" style="width: 140px">
                  <el-option label="全新" :value="10" />
                  <el-option label="95新以上" :value="9" />
                  <el-option label="9成新以上" :value="9" />
                  <el-option label="8成新以上" :value="8" />
                </el-select>
              </div>

              <div class="filter-group price-group">
                <span class="filter-label">价格：</span>
                <el-input-number v-model="filterForm.minPrice" :min="0" :controls="false" placeholder="最低" style="width: 100px" @change="handleFilter" />
                <span class="price-separator">-</span>
                <el-input-number v-model="filterForm.maxPrice" :min="0" :controls="false" placeholder="最高" style="width: 100px" @change="handleFilter" />
              </div>
              
              <el-button type="primary" link @click="resetFilter" style="margin-left: auto;">重置筛选</el-button>
            </div>
          </div>

          <div>
            <!-- 空状态 -->
            <div class="empty-state" v-if="!loading && itemList.length === 0">
              <el-empty description="暂无商品，快去发布第一个吧！">
                <el-button type="primary" @click="$router.push('/publish')">立即发布</el-button>
              </el-empty>
            </div>

            <!-- 骨架屏 -->
            <div class="item-grid" v-else-if="loading">
              <div v-for="i in 10" :key="i" class="item-card skeleton-card">
                <el-skeleton animated>
                  <template #template>
                    <el-skeleton-item variant="image" style="width: 100%; height: 220px;" />
                    <div style="padding: 12px">
                      <el-skeleton-item variant="p" style="width: 80%; height: 20px; margin-bottom: 12px;" />
                      <div style="display: flex; justify-content: space-between; margin-bottom: 12px;">
                        <el-skeleton-item variant="text" style="width: 30%" />
                        <el-skeleton-item variant="text" style="width: 20%" />
                      </div>
                      <div style="display: flex; align-items: center;">
                        <el-skeleton-item variant="circle" style="width: 20px; height: 20px" />
                        <el-skeleton-item variant="text" style="width: 40%; margin-left: 8px;" />
                      </div>
                    </div>
                  </template>
                </el-skeleton>
              </div>
            </div>

            <!-- 物品列表 -->
            <div class="item-grid" v-else>
              <div 
                v-for="item in itemList" 
                :key="item.itemId" 
                class="item-card"
                @click="$router.push(`/item/${item.itemId}`)"
              >
                <div class="item-cover-wrapper">
                  <el-image 
                    :src="getCoverImage(item.images)" 
                    fit="cover" 
                    class="item-cover"
                    loading="lazy"
                  >
                    <template #error>
                      <div class="image-slot">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="item-condition" v-if="item.conditionRate">
                    {{ item.conditionRate === 10 ? '全新' : item.conditionRate + '成新' }}
                  </div>
                </div>
                
                <div class="item-info">
                  <div class="item-title" :title="item.title">{{ item.title }}</div>
                  <div class="item-meta">
                    <span class="price-symbol">¥</span>
                    <span class="item-price">{{ formatPrice(item.price) }}</span>
                    <span class="want-count" v-if="item.wantCount > 0">{{ item.wantCount }}人想要</span>
                  </div>
                  
                  <div class="publisher-info">
                    <div class="publisher-left">
                      <el-avatar :size="20" :src="item.publisher?.avatar" icon="UserFilled" />
                      <span class="publisher-name">{{ item.publisher?.nickname || '匿名用户' }}</span>
                    </div>
                    <span class="campus-area" v-if="item.campusArea">{{ item.campusArea }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
defineOptions({
  name: 'Home'
})
import { getCoverImage } from '@/utils/format'
import { ref, reactive, onMounted, onActivated } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, UserFilled, ArrowDown, Plus, Picture, Bell, ChatDotRound } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'

const userStore = useUserStore()
const router = useRouter()
const itemList = ref([])
const categoryList = ref([]) // New: dynamic categories
const loading = ref(false)
const searchKeyword = ref('')

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
  searchKeyword.value = item.value
  handleSearch()
}


const announcements = ref([])
const announcementVisible = ref(false)
const currentAnnouncement = ref({})

const filterForm = reactive({
  categoryId: null,
  conditionRate: null,
  minPrice: undefined,
  maxPrice: undefined,
  sort: 'default'
})

const formatDate = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD')
}

const fetchAnnouncements = async () => {
  try {
    const res = await request.get('/announcement/list', {
      params: { page: 1, size: 5 }
    })
    if (res.code === 200) {
      announcements.value = res.data.records
    }
  } catch (error) {
    console.error('获取公告失败', error)
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

const showAnnouncement = (item) => {
  currentAnnouncement.value = item
  announcementVisible.value = true
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

// 获取物品列表
const fetchItems = async () => {
  loading.value = true
  try {
    let sortField = null
    let sortOrder = null
    if (filterForm.sort === 'price_asc') {
      sortField = 'price'
      sortOrder = 'asc'
    } else if (filterForm.sort === 'price_desc') {
      sortField = 'price'
      sortOrder = 'desc'
    } else if (filterForm.sort === 'want') {
      sortField = 'want'
      sortOrder = 'desc'
    }

    const params = {
      keyword: searchKeyword.value,
      categoryId: filterForm.categoryId,
      conditionRate: filterForm.conditionRate,
      minPrice: filterForm.minPrice,
      maxPrice: filterForm.maxPrice,
      sortField,
      sortOrder
    }
    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === null || params[key] === '') {
        delete params[key]
      }
    })

    const res = await request.get('/item/list', { params })
    if (res.code === 200) {
      itemList.value = res.data
    }
  } catch (error) {
    console.error('获取物品列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return
  router.push({ 
    path: '/search', 
    query: { keyword: searchKeyword.value } 
  })
}

const handleFilter = () => {
  fetchItems()
}

const resetFilter = () => {
  filterForm.categoryId = null
  filterForm.conditionRate = null
  filterForm.minPrice = undefined
  filterForm.maxPrice = undefined
  filterForm.sort = 'default'
  searchKeyword.value = ''
  fetchItems()
}

// 移除旧的 getCoverImage 定义，已移至 format.js

// 格式化价格
const formatPrice = (price) => {
  return Number(price).toFixed(2)
}

onMounted(() => {
  fetchItems()
  fetchAnnouncements()
  fetchCategories()
  if (userStore.user.token) {
    userStore.updateUnreadCount()
    userStore.updateUnreadChatCount()
  }
})

onActivated(() => {
  if (userStore.user.token) {
    userStore.updateUnreadCount()
    userStore.updateUnreadChatCount()
  }
})
</script>

<style scoped>
.xy-header {
  background-color: #ffda44; /* 闲鱼黄 */
  height: 60px;
  padding: 0;
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

.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.item-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid #f0f0f0;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.08);
}

.item-cover-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 Aspect Ratio */
  background-color: #f8f8f8;
}

.item-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.item-condition {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.item-info {
  padding: 12px;
}

.item-title {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 8px;
  font-weight: 500;
}

.item-meta {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.price-symbol {
  color: #ff5000;
  font-size: 12px;
  margin-right: 2px;
}

.item-price {
  color: #ff5000;
  font-size: 18px;
  font-weight: bold;
}

.want-count {
  margin-left: auto;
  font-size: 12px;
  color: #999;
}

.publisher-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #f5f5f5;
  padding-top: 8px;
}

.publisher-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.publisher-name {
  font-size: 12px;
  color: #666;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.campus-area {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 2px 4px;
  border-radius: 2px;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

.logo-area {
  display: flex;
  align-items: baseline;
  cursor: pointer;
}

.logo-text {
  font-size: 28px;
  font-weight: 900;
  color: #333;
  letter-spacing: -1px;
}

.logo-sub {
  font-size: 14px;
  color: #333;
  margin-left: 8px;
  font-weight: 500;
  opacity: 0.8;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 30px;
}

.ai-nav-item {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background-color 0.2s;
  position: relative;
}

.ai-nav-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.ai-nav-icon {
  font-size: 20px;
  margin-right: 6px;
}

.ai-badge {
  position: absolute;
  top: -4px;
  right: -10px;
  background-color: #ff5000;
  color: #fff;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 8px 8px 8px 0;
  font-weight: normal;
  transform: scale(0.9);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 40px;
}

.search-box {
  display: flex;
  align-items: center;
  width: 400px;
}

.xy-search-input {
  flex: 1; /* Ensure input takes available space */
}

/* Remove overrides to keep square input */
.xy-search-input :deep(.el-input__inner) {
  color: #333;
}

.search-btn {
  padding: 0 20px;
  height: 32px;
  margin-left: 4px; /* Space between input and button */
}

.publish-entry-btn {
  background-color: #333;
  color: #ffda44;
  border: none;
  font-weight: bold;
  margin-right: 20px;
}

.publish-entry-btn:hover {
  background-color: #000;
  color: #ffcd00;
}

.notification-area {
  margin-right: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.bell-icon {
  font-size: 24px;
  color: #333;
}

.user-actions {
  min-width: 100px;
  text-align: right;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
}

.user-avatar {
  background: #fff;
  color: #ffda44;
  margin-right: 8px;
}

.username {
  font-weight: 600;
  margin-right: 4px;
}

.guest-actions {
  font-size: 16px;
  font-weight: 500;
}

.nav-link {
  color: #333;
  transition: opacity 0.2s;
}

.nav-link:hover {
  opacity: 0.7;
}

.divider {
  margin: 0 8px;
  color: #333;
  opacity: 0.3;
}

.xy-main {
  padding: 0;
  min-height: calc(100vh - 60px);
}

.banner-section {
  background: linear-gradient(180deg, #ffda44 0%, #fff9d6 100%);
  padding: 60px 0;
  text-align: center;
}

.banner-section h1 {
  font-size: 48px;
  margin: 0;
  color: #333;
  font-weight: 800;
}

.banner-section p {
  font-size: 20px;
  margin: 10px 0 0;
  color: #666;
}

.announcement-bar {
  width: 700px;
  background: #ffffff;
  border-radius: 25px;
  box-shadow: 0 8px 20px rgba(255, 218, 68, 0.3);
  padding: 0 25px;
  margin: 30px auto 0;
  border: 1px solid rgba(255, 218, 68, 0.2);
  display: flex;
  align-items: center;
}

.announcement-bar .el-carousel {
  flex: 1;
}

.more-link {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  margin-left: 15px;
  white-space: nowrap;
}

.more-link:hover {
  color: #ffda44;
  text-decoration: underline;
}

.announcement-item {
  display: flex;
  align-items: center;
  height: 40px;
  color: #333;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
}

.announcement-item .el-icon {
  margin-right: 12px;
  font-size: 20px;
  color: #ff5000;
  animation: bell-swing 2s infinite ease-in-out;
}

@keyframes bell-swing {
  0%, 100% { transform: rotate(-10deg); }
  50% { transform: rotate(10deg); }
}

.announcement-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 20px;
  text-align: left;
}

.announcement-date {
  color: #999;
  font-size: 13px;
  font-weight: normal;
}

.announcement-meta {
  color: #999;
  font-size: 12px;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.announcement-body {
  line-height: 1.6;
  white-space: pre-wrap;
  font-size: 16px;
  color: #333;
}

.content-section {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}

.content-section h2 {
  display: flex;
  align-items: baseline;
  margin-bottom: 30px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  position: relative;
  z-index: 1;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 2px;
  left: 0;
  width: 100%;
  height: 8px;
  background: #ffda44;
  opacity: 0.4;
  z-index: -1;
  border-radius: 4px;
}

.section-subtitle {
  font-size: 14px;
  color: #999;
  margin-left: 10px;
  font-weight: 400;
  letter-spacing: 1px;
}

.empty-state {
  background: #fff;
  border-radius: 16px;
  padding: 60px 0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.filter-section {
  background: #fff;
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.filter-group {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 30px;
}

.filter-row .filter-group {
  margin-bottom: 0;
}

.filter-label {
  font-weight: 600;
  margin-right: 12px;
  color: #333;
  font-size: 14px;
  min-width: 50px;
}

.price-separator {
  margin: 0 10px;
  color: #bbb;
}

.el-radio-button__inner {
  border: none !important;
  background: #f5f5f5 !important;
  border-radius: 6px !important;
  margin-right: 10px;
  padding: 8px 16px;
  color: #666;
  box-shadow: none !important;
}

.el-radio-button:first-child .el-radio-button__inner {
  border-radius: 6px !important;
}

.el-radio-button:last-child .el-radio-button__inner {
  border-radius: 6px !important;
}

.el-radio-button.is-active .el-radio-button__inner {
  background: #ffda44 !important;
  color: #333 !important;
  font-weight: 600;
}

/* 移动端响应式适配 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    padding: 10px;
    height: auto;
  }
  .header-right {
    width: 100%;
    margin-top: 10px;
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
  }
  .search-box {
    width: 100%;
    max-width: none;
    margin-bottom: 10px;
  }
  .item-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 10px;
  }
  .filter-row {
    gap: 10px;
  }
  .banner-section {
    padding: 30px 20px;
  }
  .banner-section h1 {
    font-size: 32px;
  }
}
</style>
