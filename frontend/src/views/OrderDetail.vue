<template>
  <div class="order-detail-page">
    <div class="container">
      <div class="header-nav">
        <el-button class="back-btn" icon="ArrowLeft" circle @click="$router.back()" title="返回上一页"></el-button>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/order/list' }">我的订单</el-breadcrumb-item>
          <el-breadcrumb-item>订单详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="main-content" v-loading="loading">
        <div v-if="order" class="order-card">
          <!-- 状态栏 -->
          <div class="status-header">
            <div class="status-text">
              <el-icon class="status-icon"><InfoFilled /></el-icon>
              {{ statusText }}
            </div>
            <div class="order-no">订单号：{{ order.orderNo }}</div>
          </div>

          <!-- 进度条 -->
          <el-steps :active="activeStep" finish-status="success" align-center class="order-steps">
            <el-step title="买家下单" :description="formatTime(order.createdAt)" />
            <el-step title="卖家确认" :description="order.status >= 1 ? '进行交易' : ''" />
            <el-step title="交易完成" :description="formatTime(order.completedAt)" />
          </el-steps>

          <!-- 物品信息 -->
          <div class="section-title">物品信息</div>
          <div class="item-info" @click="$router.push(`/item/${order.itemId}`)">
            <el-image :src="itemCover" class="item-img" fit="cover" />
            <div class="item-detail">
              <div class="item-title">{{ order.item?.title }}</div>
              <div class="item-price">¥ {{ order.amount }}</div>
            </div>
          </div>

          <!-- 交易信息 -->
          <div class="section-title">交易信息</div>
          <div class="info-list">
            <div class="info-item">
              <span class="label">买家：</span>
              <span class="value">{{ order.buyer?.nickname || order.buyer?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">卖家：</span>
              <span class="value">{{ order.seller?.nickname || order.seller?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">交易方式：</span>
              <span class="value">{{ order.transactionMethod === 2 ? '送货上门' : '自提' }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付方式：</span>
              <span class="value">{{ order.paymentMethod === 2 ? '线下自行交易' : '线上支付' }}</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-bar">
            <!-- 卖家操作 -->
            <template v-if="isSeller">
              <el-button 
                type="primary" 
                v-if="order.status === 0 && order.paymentMethod === 2" 
                @click="confirmOrder"
              >
                确认接单
              </el-button>
            </template>

            <!-- 买家操作 -->
            <template v-if="isBuyer">
              <el-button 
                type="primary" 
                v-if="order.status === 0 && order.paymentMethod !== 2 && (!order.paymentStatus || order.paymentStatus === 0)" 
                @click="handlePay"
              >
                去支付
              </el-button>
              <el-button 
                type="success" 
                v-if="order.status === 1 && order.paymentMethod !== 2" 
                @click="completeOrder"
              >
                确认收货
              </el-button>
            </template>

            <!-- 公共操作 -->
            <el-button 
              type="danger" 
              plain 
              v-if="order.status < 2" 
              @click="cancelOrder"
            >
              取消订单
            </el-button>
          </div>
        </div>
        
        <el-empty v-else-if="!loading" description="订单不存在" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatTime } from '@/utils/format'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { InfoFilled, ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const order = ref(null)

const isBuyer = computed(() => order.value?.buyerId === userStore.user.userId)
const isSeller = computed(() => order.value?.sellerId === userStore.user.userId)

const activeStep = computed(() => {
  if (!order.value) return 0
  if (order.value.status === 0) return 1
  if (order.value.status === 1) return 2
  if (order.value.status === 2) return 3
  return 1 // 取消状态也显示第一步完成
})

const statusText = computed(() => {
  if (!order.value) return ''
  if (order.value.status === 0) {
    return order.value.paymentMethod === 2 ? '待卖家确认' : '待买家支付'
  }
  const map = { 1: '交易进行中', 2: '交易已完成', 3: '订单已取消' }
  return map[order.value.status]
})

const itemCover = computed(() => {
  try {
    const images = JSON.parse(order.value?.item?.images || '[]')
    return images[0] || ''
  } catch {
    return ''
  }
})

// 移至 format.js

const fetchOrder = async () => {
  loading.value = true
  try {
    const res = await request.get(`/order/detail/${route.params.id}`)
    if (res.code === 200) {
      order.value = res.data
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  try {
    const res = await axios.get(`/api/pay/alipay?orderId=${order.value.orderId}`, {
      headers: { 'Authorization': userStore.user.token }
    })
    // 后端返回的是 HTML 表单
    const div = document.createElement('div')
    div.innerHTML = res.data
    document.body.appendChild(div)
    // 找到并提交表单
    const forms = document.getElementsByTagName('form')
    if (forms.length > 0) {
      forms[forms.length - 1].submit()
    }
  } catch (error) {
    console.error('支付跳转失败', error)
    ElMessage.error('支付跳转失败，请重试')
  }
}

const confirmOrder = async () => {
  const msg = '线下交易：确认接受该订单并开始交易吗？'
    
  ElMessageBox.confirm(msg, '提示', { type: 'info' })
    .then(async () => {
      const res = await request.post(`/order/confirm/${order.value.orderId}`)
      if (res.code === 200) {
        ElMessage.success('已确认接单')
        fetchOrder()
      }
    })
    .catch(() => {})
}

const completeOrder = () => {
  ElMessageBox.confirm('确认已收到物品且无误？此操作不可撤销', '确认收货', { type: 'success' })
    .then(async () => {
      const res = await request.post(`/order/complete/${order.value.orderId}`)
      if (res.code === 200) {
        ElMessage.success('交易完成！')
        fetchOrder()
      }
    })
    .catch(() => {})
}

const cancelOrder = () => {
  ElMessageBox.confirm('确定要取消该订单吗？', '警告', { type: 'warning' })
    .then(async () => {
      const res = await request.post(`/order/cancel/${order.value.orderId}`)
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        fetchOrder()
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchOrder()
})
</script>

<style scoped>
.order-detail-page {
  background-color: #f6f7f9;
  min-height: 100vh;
  padding: 20px 0;
}

.container {
  max-width: 800px;
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

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.status-text {
  font-size: 20px;
  font-weight: bold;
  color: #ffda44;
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-no {
  color: #999;
  font-size: 14px;
}

.order-steps {
  margin-bottom: 40px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #ffda44;
}

.item-info {
  display: flex;
  gap: 15px;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 30px;
  cursor: pointer;
}

.item-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.item-detail {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-title {
  font-size: 16px;
  color: #333;
}

.item-price {
  color: #ff5000;
  font-size: 18px;
  font-weight: bold;
}

.info-list {
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
  font-size: 14px;
}

.label {
  color: #666;
  width: 80px;
}

.value {
  color: #333;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
</style>
