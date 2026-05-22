<template>
  <div class="admin-layout">
    <el-container class="layout-container">
      <el-aside width="200px" class="admin-aside">
        <div class="logo">SwapU 管理后台</div>
        <el-menu
          router
          :default-active="$route.path"
          class="admin-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/admin/dashboard" @click="$router.push('/admin/dashboard')">
            <el-icon><Odometer /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/user" @click="$router.push('/admin/user')">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/item" @click="$router.push('/admin/item')">
            <el-icon><Goods /></el-icon>
            <span>物品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/report" @click="$router.push('/admin/report')">
            <el-icon><Warning /></el-icon>
            <span>投诉处理</span>
          </el-menu-item>
          <el-menu-item index="/admin/category" @click="$router.push('/admin/category')">
            <el-icon><Setting /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/announcement" @click="$router.push('/admin/announcement')">
            <el-icon><Bell /></el-icon>
            <span>系统公告</span>
          </el-menu-item>
          <el-menu-item index="/" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出后台</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            管理员：{{ userStore.user.nickname || userStore.user.username }}
          </div>
        </el-header>
        
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Odometer, User, Goods, SwitchButton, Warning } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentRouteName = computed(() => {
  const map = {
    'adminDashboard': '仪表盘',
    'adminUser': '用户管理',
    'adminItem': '物品管理',
    'adminReport': '投诉处理',
    'adminCategory': '分类管理',
    'adminAnnouncement': '系统公告'
  }
  return map[route.name] || ''
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.layout-container {
  height: 100%;
}

.admin-aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid #1f2d3d;
}

.admin-menu {
  border-right: none;
}

.admin-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.admin-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
