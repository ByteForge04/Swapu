<template>
  <div :class="{'order-list-page': !isEmbedded, 'order-list-embedded': isEmbedded}">
    <div :class="{'container': !isEmbedded}">
      <div class="header-nav" v-if="!isEmbedded">
        <el-button class="back-btn" icon="ArrowLeft" circle @click="$router.back()" title="返回上一页"></el-button>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>我的订单</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="main-content">
        <el-tabs v-model="activeTab" class="order-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="我买到的" name="buyer">
            <div v-loading="loading">
              <div v-if="orderList.length > 0" class="order-list">
                <div v-for="order in orderList" :key="order.orderId" class="order-item" @click="goToDetail(order.orderId)">
                  <div class="order-header">
                    <span class="time">{{ formatTime(order.createdAt) }}</span>
                    <span class="status" :class="getStatusClass(order.status)">{{ getStatusText(order) }}</span>
                  </div>
                  <div class="order-body">
                    <el-image :src="getItemCover(order.item?.images)" class="item-img" fit="cover" />
                    <div class="item-info">
                      <div class="item-title">{{ order.item?.title }}</div>
                      <div class="seller-name">卖家：{{ order.seller?.nickname || order.seller?.username }}</div>
                      <div class="meta-item">
                        <span class="label">支付方式：</span>
                        <span>{{ order.paymentMethod === 2 ? '线下自行交易' : '线上支付' }}</span>
                      </div>
                      <div class="meta-item">
                        <span class="label">交易方式：</span>
                        <span>{{ order.transactionMethod === 2 ? '送货上门' : '自提' }}</span>
                      </div>
                    </div>
                    <div class="order-price">¥ {{ order.amount }}</div>
                  </div>
                  <div class="order-footer">
                    <el-button size="small" @click.stop="goToDetail(order.orderId)">查看详情</el-button>
                    <el-button 
                      v-if="order.status === 0 && order.paymentMethod !== 2 && (!order.paymentStatus || order.paymentStatus === 0)" 
                      type="primary" 
                      size="small" 
                      @click.stop="handlePay(order)"
                    >去支付</el-button>
                    <el-button 
                      v-if="order.status === 0" 
                      type="danger" 
                      size="small" 
                      plain
                      @click.stop="handleCancel(order)"
                    >取消订单</el-button>
                    <el-button 
                      v-if="order.status === 1 && order.paymentMethod !== 2" 
                      type="success" 
                      size="small" 
                      @click.stop="handleComplete(order)"
                    >确认收货</el-button>
                    <el-button 
                      v-if="order.status === 2" 
                      type="primary" 
                      size="small" 
                      @click.stop="handleComment(order)"
                    >评价</el-button>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无买入订单" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="我卖出的" name="seller">
            <div v-loading="loading">
              <div v-if="orderList.length > 0" class="order-list">
                <div v-for="order in orderList" :key="order.orderId" class="order-item" @click="goToDetail(order.orderId)">
                  <div class="order-header">
                    <span class="time">{{ formatTime(order.createdAt) }}</span>
                    <span class="status" :class="getStatusClass(order.status)">{{ getStatusText(order) }}</span>
                  </div>
                  <div class="order-body">
                    <el-image :src="getItemCover(order.item?.images)" class="item-img" fit="cover" />
                    <div class="item-info">
                      <div class="item-title">{{ order.item?.title }}</div>
                      <div class="buyer-name">买家：{{ order.buyer?.nickname || order.buyer?.username }}</div>
                      <div class="meta-item">
                        <span class="label">支付方式：</span>
                        <span>{{ order.paymentMethod === 2 ? '线下自行交易' : '线上支付' }}</span>
                      </div>
                      <div class="meta-item">
                        <span class="label">交易方式：</span>
                        <span>{{ order.transactionMethod === 2 ? '送货上门' : '自提' }}</span>
                      </div>
                    </div>
                    <div class="order-price">¥ {{ order.amount }}</div>
                  </div>
                  <div class="order-footer">
                    <el-button size="small" @click.stop="goToDetail(order.orderId)">查看详情</el-button>
                    <el-button 
                      v-if="order.status === 0" 
                      type="danger" 
                      size="small" 
                      plain
                      @click.stop="handleCancel(order)"
                    >取消订单</el-button>
                    <el-button 
                      v-if="order.status === 0 && order.paymentMethod === 2" 
                      type="primary" 
                      size="small" 
                      @click.stop="handleConfirm(order)"
                    >确认接单</el-button>
                    <el-button 
                      v-if="order.status === 2" 
                      type="primary" 
                      size="small" 
                      @click.stop="handleComment(order)"
                    >评价</el-button>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无卖出订单" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    <!-- 评价弹窗 -->
    <el-dialog v-model="commentDialogVisible" title="发表评价" width="500px">
      <el-form :model="commentForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="commentForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input 
            v-model="commentForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入您的评价..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment" :loading="commentLoading">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getCoverImage as getItemCover, formatTime, getStatusText } from '@/utils/format'
import { ref, reactive, onMounted, defineProps } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const props = defineProps({
  isEmbedded: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()
const activeTab = ref('buyer')
const loading = ref(false)
const orderList = ref([])

// 评价相关
const commentDialogVisible = ref(false)
const commentLoading = ref(false)
const commentForm = reactive({
  orderId: null,
  rating: 5,
  content: ''
})

const handleTabChange = () => {
  fetchOrders()
}

const fetchOrders = async () => {
  loading.value = true
  const type = activeTab.value === 'buyer' ? 1 : 2
  try {
    const res = await request.get(`/order/list/${type}`)
    if (res.code === 200) {
      orderList.value = res.data
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 移至 format.js

// getStatusText 移至 format.js

const getStatusClass = (status) => {
  if (status === 1) return 'status-active'
  if (status === 2) return 'status-success'
  if (status === 3) return 'status-cancel'
  return 'status-pending'
}

const handleComment = (order) => {
  commentForm.orderId = order.orderId
  commentForm.rating = 5
  commentForm.content = ''
  commentDialogVisible.value = true
}

const submitComment = async () => {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  commentLoading.value = true
  try {
    const res = await request.post('/comment/create', {
      orderId: commentForm.orderId,
      rating: commentForm.rating,
      content: commentForm.content
    })
    
    if (res.code === 200) {
      ElMessage.success('评价成功')
      commentDialogVisible.value = false
      fetchOrders() // 刷新列表
    } else {
      ElMessage.error(res.msg || '评价失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    commentLoading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/order/detail/${id}`)
}

const handleConfirm = (order) => {
  const msg = '线下交易：确认接受该订单并开始交易吗？'
    
  ElMessageBox.confirm(msg, '提示', { type: 'info' })
    .then(async () => {
      const res = await request.post(`/order/confirm/${order.orderId}`)
      if (res.code === 200) {
        ElMessage.success('已确认接单')
        fetchOrders()
      }
    })
    .catch(() => {})
}

const handleComplete = (order) => {
  ElMessageBox.confirm('确认已收到物品且无误？', '确认收货', { type: 'success' })
    .then(async () => {
      const res = await request.post(`/order/complete/${order.orderId}`)
      if (res.code === 200) {
        ElMessage.success('交易完成！')
        fetchOrders()
      }
    })
    .catch(() => {})
}

const handlePay = async (order) => {
  try {
    const res = await axios.get(`/api/pay/alipay?orderId=${order.orderId}`, {
      headers: { 'Authorization': userStore.user.token }
    })
    const div = document.createElement('div')
    div.innerHTML = res.data
    document.body.appendChild(div)
    const forms = document.getElementsByTagName('form')
    if (forms.length > 0) {
      forms[forms.length - 1].submit()
    }
  } catch (error) {
    console.error('支付跳转失败', error)
    ElMessage.error('支付跳转失败，请重试')
  }
}

const handleCancel = (order) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '警告', { type: 'warning' })
    .then(async () => {
      const res = await request.post(`/order/cancel/${order.orderId}`)
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        fetchOrders()
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-list-page {
  background-color: #f6f7f9;
  min-height: 100vh;
  padding: 20px 0;
}

.order-list-embedded {
  /* 嵌入式样式调整 */
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

.main-content {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  min-height: 500px;
}

.order-item {
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.order-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border-color: #ffda44;
}

.order-header {
  background: #f9f9f9;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  font-size: 13px;
  color: #666;
}

.status-active { color: #ffda44; font-weight: bold; }
.status-success { color: #67c23a; }
.status-cancel { color: #909399; }
.status-pending { color: #e6a23c; }

.order-body {
  padding: 15px;
  display: flex;
  gap: 15px;
  align-items: center;
}

.item-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.seller-name, .buyer-name {
  font-size: 13px;
  color: #999;
}

.meta-item {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.order-price {
  font-size: 18px;
  font-weight: bold;
  color: #ff5000;
}

.order-footer {
  padding: 10px 15px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
