<template>
  <div class="notification-page">
    <el-container>
      <el-header class="xy-header">
        <div class="header-content">
          <div class="logo-area" @click="$router.push('/')">
            <span class="logo-text">SwapU</span>
            <span class="logo-sub">消息中心</span>
          </div>
          <div class="header-right">
            <el-button type="default" link class="header-btn" @click="$router.push('/')">返回首页</el-button>
          </div>
        </div>
      </el-header>

      <el-main class="xy-main">
        <div class="content-wrapper">
          <div class="header-nav">
            <el-button class="back-btn" icon="ArrowLeft" circle @click="$router.back()" title="返回上一页"></el-button>
            <el-breadcrumb separator="/" class="breadcrumb">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>消息通知</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="page-header">
            <div class="title-area">
              <h2>消息通知</h2>
              <el-tag type="danger" round v-if="userStore.unreadCount > 0">{{ userStore.unreadCount }} 未读</el-tag>
            </div>
            <el-button type="primary" link @click="handleReadAll" :disabled="userStore.unreadCount === 0">全部已读</el-button>
          </div>

          <div class="notification-list" v-loading="loading">
            <div 
              v-for="item in notifications" 
              :key="item.notificationId" 
              class="notification-card"
              :class="{ 'is-read': item.isRead === 1 }"
              @click="handleRead(item)"
            >
              <div class="icon-area">
                <el-icon v-if="item.type === 1" class="sys-icon"><Bell /></el-icon>
                <el-icon v-else-if="item.type === 2" class="trade-icon"><Goods /></el-icon>
                <el-icon v-else class="chat-icon"><ChatDotRound /></el-icon>
              </div>
              
              <div class="content-area">
                <div class="card-top">
                  <span class="title">{{ item.title }}</span>
                  <span class="time">{{ formatTime(item.createdAt) }}</span>
                </div>
                <div class="message">{{ item.content }}</div>
              </div>
              
              <div class="status-dot" v-if="item.isRead === 0"></div>
            </div>

            <el-empty v-if="!loading && notifications.length === 0" description="暂无消息" />
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
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { Bell, Goods, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const notifications = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await request.get('/notification/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      notifications.value = res.data.records
      total.value = Number(res.data.total)
    }
  } catch (error) {
    console.error('Failed to fetch notifications', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchNotifications()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleRead = async (item) => {
  if (item.isRead === 1) return
  
  try {
    const res = await request.put(`/notification/read/${item.notificationId}`)
    if (res.code === 200) {
      item.isRead = 1
      userStore.updateUnreadCount() // 更新全局未读数
    }
  } catch (e) {
    console.error(e)
  }
}

const handleReadAll = async () => {
  try {
    const res = await request.put('/notification/read-all')
    if (res.code === 200) {
      ElMessage.success('已全部标记为已读')
      notifications.value.forEach(item => item.isRead = 1)
      userStore.updateUnreadCount()
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchNotifications()
  userStore.updateUnreadCount()
})
</script>

<style scoped>
.notification-page {
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
  max-width: 1000px;
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

.header-btn {
  color: #333 !important;
  font-weight: 500;
}

.header-btn:hover {
  color: #000 !important;
  text-decoration: underline;
}

.content-wrapper {
  max-width: 800px;
  margin: 30px auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.notification-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  border-left: 4px solid transparent;
}

.notification-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
}

/* 未读状态样式 */
.notification-card:not(.is-read) {
  background: #fff;
  border-left-color: #ffda44;
}

.notification-card.is-read {
  opacity: 0.8;
  background: #fcfcfc;
}

.icon-area {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.sys-icon { color: #409eff; font-size: 20px; }
.trade-icon { color: #ff9800; font-size: 20px; }
.chat-icon { color: #67c23a; font-size: 20px; }

.content-area {
  flex: 1;
}

.card-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.message {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.status-dot {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  margin-bottom: 50px;
}
</style>
