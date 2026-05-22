<template>
  <div class="announcement-list-page">
    <el-container>
      <el-header class="xy-header">
        <div class="header-content">
          <div class="logo-area" @click="$router.push('/')">
            <span class="logo-text">SwapU</span>
            <span class="logo-sub">系统公告</span>
          </div>
          <div class="header-right">
            <el-button type="default" link class="header-btn" @click="$router.go(-1)">返回上一页</el-button>
            <el-divider direction="vertical" />
            <el-button type="default" link class="header-btn" @click="$router.push('/')">返回首页</el-button>
          </div>
        </div>
      </el-header>

      <el-main class="xy-main">
        <div class="content-wrapper">
          <div class="page-title">
            <h2>全部公告</h2>
            <span class="total-count">共 {{ total }} 条</span>
          </div>

          <div class="announcement-list" v-loading="loading">
            <div 
              v-for="item in announcements" 
              :key="item.announcementId" 
              class="announcement-card"
              @click="toggleExpand(item)"
            >
              <div class="card-header">
                <div class="title-row">
                  <el-tag size="small" effect="dark" type="warning" class="tag">公告</el-tag>
                  <span class="title">{{ item.title }}</span>
                </div>
                <span class="date">{{ formatDate(item.createdAt) }}</span>
              </div>
              
              <div class="card-content" v-show="item.expanded">
                <div class="content-text">{{ item.content }}</div>
              </div>
              
              <div class="card-footer">
                <el-icon :class="{ 'is-expanded': item.expanded }"><ArrowDown /></el-icon>
                <span>{{ item.expanded ? '收起详情' : '展开详情' }}</span>
              </div>
            </div>

            <el-empty v-if="!loading && announcements.length === 0" description="暂无公告" />
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
import dayjs from 'dayjs'
import { ArrowDown } from '@element-plus/icons-vue'

const loading = ref(false)
const announcements = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const formatDate = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const res = await request.get('/announcement/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      announcements.value = res.data.records.map(item => ({
        ...item,
        expanded: false
      }))
      total.value = Number(res.data.total)
    }
  } catch (error) {
    console.error('Failed to fetch announcements', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchAnnouncements()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const toggleExpand = (item) => {
  item.expanded = !item.expanded
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.announcement-list-page {
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

.page-title {
  display: flex;
  align-items: baseline;
  margin-bottom: 20px;
}

.page-title h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.total-count {
  margin-left: 10px;
  font-size: 14px;
  color: #999;
}

.announcement-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}

.announcement-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.date {
  font-size: 13px;
  color: #999;
}

.card-content {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #eee;
}

.content-text {
  font-size: 15px;
  color: #555;
  line-height: 1.6;
  white-space: pre-wrap;
}

.card-footer {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #999;
  font-size: 12px;
  gap: 4px;
}

.card-footer .el-icon {
  transition: transform 0.3s;
}

.card-footer .el-icon.is-expanded {
  transform: rotate(180deg);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  margin-bottom: 50px;
}
</style>
