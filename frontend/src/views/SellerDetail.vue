<template>
  <div class="seller-detail-page">
    <div class="container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>卖家详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="main-content" v-loading="loading">
        <!-- 卖家信息卡片 -->
        <el-card class="seller-card">
          <div class="seller-header">
            <el-avatar :size="80" :src="userInfo?.avatar" icon="UserFilled" />
            <div class="seller-info">
              <h2 class="nickname">{{ userInfo?.nickname || userInfo?.username }}</h2>
              <div class="meta">
                <span class="score">信用分: {{ userInfo?.creditScore }}</span>
                <span class="join-time">加入时间: {{ formatTime(userInfo?.createdAt) }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <div class="content-tabs">
          <el-tabs v-model="activeTab">
            <!-- 在售物品 -->
            <el-tab-pane label="在售物品" name="items">
              <div v-if="items.length > 0" class="item-list">
                <div 
                  v-for="item in items" 
                  :key="item.itemId" 
                  class="item-card"
                  @click="$router.push(`/item/${item.itemId}`)"
                >
                  <el-image :src="getCoverImage(item.images)" class="item-cover" fit="cover" />
                  <div class="item-info">
                    <div class="item-title" :title="item.title">{{ item.title }}</div>
                    <div class="item-price">¥{{ item.price }}</div>
                    <div class="item-meta">
                      <span>{{ item.viewCount || 0 }}浏览</span>
                      <span>{{ item.wantCount || 0 }}想要</span>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无在售物品" />
            </el-tab-pane>

            <!-- 用户评价 -->
            <el-tab-pane label="收到评价" name="comments">
              <div v-if="comments.length > 0" class="comment-list">
                <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
                  <div class="comment-header">
                    <div class="user-info">
                      <el-avatar :size="30" :src="comment.user?.avatar" icon="UserFilled" />
                      <span class="username">{{ comment.user?.nickname || comment.user?.username || '匿名用户' }}</span>
                      <el-rate v-model="comment.rating" disabled size="small" />
                    </div>
                    <span class="time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-content">{{ comment.content }}</div>
                  
                  <!-- 关联物品信息 -->
                  <div v-if="comment.item" class="comment-item-link" @click="$router.push(`/item/${comment.item.itemId}`)">
                    <el-image :src="getCoverImage(comment.item.images)" class="mini-cover" fit="cover" />
                    <div class="link-info">
                      <div class="link-title">{{ comment.item.title }}</div>
                      <div class="link-price">¥{{ comment.item.price }}</div>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无评价" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserFilled, ArrowLeft } from '@element-plus/icons-vue'
import request from '@/utils/request'
import dayjs from 'dayjs'

const route = useRoute()
const userId = route.params.id

const loading = ref(true)
const userInfo = ref(null)
const items = ref([])
const comments = ref([])
const activeTab = ref('items')

const fetchData = async () => {
  loading.value = true
  try {
    // 1. 获取用户信息
    const userRes = await request.get(`/user/public/${userId}`)
    if (userRes.code === 200) {
      userInfo.value = userRes.data
    }

    // 2. 获取在售物品
    const itemsRes = await request.get(`/item/user/${userId}/selling`)
    if (itemsRes.code === 200) {
      // 过滤出状态为 1（在售）的商品
      items.value = itemsRes.data.filter(item => item.status === 1)
    }

    // 3. 获取评价
    const commentsRes = await request.get(`/comment/public/seller/${userId}`)
    if (commentsRes.code === 200) {
      comments.value = commentsRes.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const parseImages = (json) => {
  try {
    return JSON.parse(json) || []
  } catch (e) {
    return []
  }
}

const getCoverImage = (imagesJson) => {
  const images = parseImages(imagesJson)
  return images.length > 0 ? images[0] : ''
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.seller-detail-page {
  background-color: #f6f7f9;
  min-height: 100vh;
  padding-bottom: 40px;
}

.container {
  max-width: 1000px;
  margin: 0 auto;
}

.header-nav {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-btn {
  margin-right: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.breadcrumb {
  margin-bottom: 0;
}

.seller-card {
  margin-bottom: 20px;
}

.seller-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.nickname {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.meta {
  color: #666;
  font-size: 14px;
  display: flex;
  gap: 20px;
}

.score {
  color: #67c23a;
  font-weight: bold;
}

.content-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.item-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.item-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.item-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.item-cover {
  width: 100%;
  height: 180px;
}

.item-info {
  padding: 10px;
}

.item-title {
  font-size: 14px;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}

.item-meta {
  font-size: 12px;
  color: #999;
  display: flex;
  justify-content: space-between;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.comment-item {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: bold;
  font-size: 14px;
}

.time {
  color: #999;
  font-size: 12px;
}

.comment-content {
  color: #333;
  margin-bottom: 5px;
  line-height: 1.5;
}

.comment-item-link {
  display: flex;
  align-items: center;
  background-color: #f6f7f9;
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
}

.mini-cover {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 10px;
}

.link-info {
  flex: 1;
}

.link-title {
  font-size: 12px;
  color: #333;
  margin-bottom: 2px;
}

.link-price {
  font-size: 12px;
  color: #f56c6c;
  font-weight: bold;
}
</style>
