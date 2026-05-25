<template>
  <div class="item-detail-page">
    <div class="container">
      <!-- 面包屑导航与返回按钮 -->
      <div class="header-nav">
        <el-button class="back-btn" icon="ArrowLeft" circle @click="$router.back()" title="返回上一页"></el-button>
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>物品详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="main-content" v-loading="loading">
        <div v-if="item" class="item-container">
          <!-- 左侧：图片轮播 -->
          <div class="left-section">
            <div class="image-gallery">
              <el-carousel trigger="click" height="400px" :autoplay="false">
                <el-carousel-item v-for="(img, index) in images" :key="index">
                  <el-image 
                    :src="img" 
                    fit="contain" 
                    class="carousel-image"
                    :preview-src-list="images"
                    :initial-index="index"
                  />
                </el-carousel-item>
              </el-carousel>
            </div>
          </div>

          <!-- 右侧：物品信息 -->
          <div class="right-section">
            <h1 class="item-title">{{ item.title }}</h1>
            
            <div class="price-section">
              <span class="currency">¥</span>
              <span class="price">{{ item.price }}</span>
              <span class="original-price" v-if="item.originalPrice">原价 ¥{{ item.originalPrice }}</span>
            </div>

            <div class="tags-section">
              <el-tag type="warning" effect="dark" class="tag">{{ conditionText }}</el-tag>
              <el-tag type="info" class="tag" v-if="item.campusArea">
                <el-icon><Location /></el-icon> {{ item.campusArea }}
              </el-tag>
              <el-tag class="tag">{{ transactionMethodText }}</el-tag>
            </div>

            <div class="meta-info">
              <span>浏览 {{ item.viewCount || 0 }}</span>
              <el-divider direction="vertical" />
              <span>想要 {{ item.wantCount || 0 }}</span>
            </div>

            <!-- 发布者卡片 -->
            <div class="seller-card" @click="$router.push(`/user/${item.userId}`)">
              <div class="seller-info">
                <el-avatar :size="50" :src="item.publisher?.avatar" icon="UserFilled" />
                <div class="seller-text">
                  <div class="seller-name">{{ item.publisher?.nickname || '匿名用户' }}</div>
                  <div class="seller-desc">来SwapU {{ daysJoined }} 天了</div>
                </div>
              </div>
              <el-button type="primary" plain round size="small">查看主页</el-button>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button 
                type="danger" 
                size="large" 
                class="buy-btn" 
                icon="Goods"
                :disabled="item.status !== 1"
                @click="handleBuy"
              >
                {{ item.status === 1 ? '我想要' : '已售出/交易中' }}
              </el-button>
              <el-button
                v-if="userStore.user.userId !== item.userId"
                size="large"
                type="success"
                icon="ChatDotRound"
                @click="$router.push(`/chat?targetId=${item.userId}`)"
              >
                私聊卖家
              </el-button>
              <el-button 
                size="large" 
                :icon="Star" 
                :type="isWanted ? 'warning' : 'default'"
                @click="handleWant"
              >
                {{ isWanted ? '已收藏' : '收藏' }}
              </el-button>
              <el-button 
                size="large" 
                type="info" 
                plain 
                icon="Warning"
                @click="reportDialogVisible = true"
              >
                举报
              </el-button>
            </div>
          </div>
        </div>

        <!-- 底部：详细描述 -->
        <div v-if="item" class="description-section">
          <div class="section-header">宝贝描述</div>
          <div class="desc-content">{{ item.description }}</div>
          
          <div class="images-list">
             <el-image 
                v-for="(img, index) in images" 
                :key="index"
                :src="img" 
                fit="contain" 
                class="detail-image"
                loading="lazy"
              />
          </div>
        </div>

        <!-- 留言板部分 -->
        <div v-if="item" class="message-board-section">
          <div class="section-header">留言板 ({{ messages.length }})</div>
          
          <!-- 留言输入框 -->
          <div class="message-input-area" v-if="userStore.user.token">
            <el-input
              v-model="newMessage"
              type="textarea"
              :rows="3"
              placeholder="看对眼了？跟卖家聊聊..."
              maxlength="200"
              show-word-limit
            />
            <div class="message-action">
              <el-button type="primary" @click="submitMessage(null, null)">发布留言</el-button>
            </div>
          </div>
          <div v-else class="login-tip">
            <el-button type="primary" link @click="$router.push('/login')">登录后留言</el-button>
          </div>

          <!-- 留言列表 -->
          <div class="message-list" v-loading="messageLoading">
            <el-empty v-if="messages.length === 0" description="暂无留言，快来抢沙发！" />
            
            <div v-for="msg in messages" :key="msg.messageId" class="message-item">
              <el-avatar :size="40" :src="msg.user?.avatar" icon="UserFilled" />
              <div class="message-content-area">
                <div class="message-header">
                  <span class="message-user">{{ msg.user?.nickname || '匿名用户' }}</span>
                  <span class="seller-badge" v-if="msg.userId === item.userId">卖家</span>
                  <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
                </div>
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-footer">
                  <span class="reply-btn" @click="showReplyInput(msg.messageId, msg.userId, msg.user?.nickname)">回复</span>
                  <span class="delete-btn" v-if="msg.userId === userStore.user.userId" @click="deleteMessage(msg.messageId)">删除</span>
                </div>

                <!-- 回复输入框 -->
                <div class="reply-input-box" v-if="activeReplyId === msg.messageId">
                  <el-input
                    v-model="replyMessage"
                    size="small"
                    :placeholder="`回复 @${replyTargetName}:`"
                    @keyup.enter="submitMessage(msg.messageId, replyTargetId)"
                  >
                    <template #append>
                      <el-button @click="submitMessage(msg.messageId, replyTargetId)">发送</el-button>
                    </template>
                  </el-input>
                  <el-button link size="small" @click="activeReplyId = null">取消</el-button>
                </div>

                <!-- 子留言列表 (回复) -->
                <div class="replies-list" v-if="msg.replies && msg.replies.length > 0">
                  <div v-for="reply in msg.replies" :key="reply.messageId" class="reply-item">
                    <div class="reply-header">
                      <span class="reply-user">{{ reply.user?.nickname || '匿名用户' }}</span>
                      <span class="seller-badge" v-if="reply.userId === item.userId">卖家</span>
                      <span class="reply-text-gap">回复</span>
                      <span class="reply-target">@{{ reply.replyToUser?.nickname || '匿名用户' }}</span>
                      <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                    </div>
                    <div class="reply-text">{{ reply.content }}</div>
                    <div class="reply-footer">
                      <span class="reply-btn" @click="showReplyInput(msg.messageId, reply.userId, reply.user?.nickname)">回复</span>
                      <span class="delete-btn" v-if="reply.userId === userStore.user.userId" @click="deleteMessage(reply.messageId)">删除</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态/错误提示 -->
        <el-empty v-else-if="!loading" description="物品不存在或已下架" />
      </div>
    </div>

    <!-- 举报弹窗 -->
    <el-dialog v-model="reportDialogVisible" title="举报违规" width="500px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reasonType" placeholder="请选择原因">
            <el-option label="虚假信息" value="虚假信息" />
            <el-option label="违禁品" value="违禁品" />
            <el-option label="欺诈行为" value="欺诈行为" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input 
            v-model="reportForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述违规情况..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport" :loading="reportLoading">提交举报</el-button>
      </template>
    </el-dialog>

    <!-- 下单弹窗 -->
    <el-dialog v-model="buyDialogVisible" title="确认购买" width="500px" center>
      <el-form :model="buyForm" label-width="100px" class="buy-form">
        <div class="buy-item-info" v-if="item">
          <img :src="images[0]" class="buy-item-img" />
          <div class="buy-item-detail">
            <div class="buy-title">{{ item.title }}</div>
            <div class="buy-price">¥{{ item.price }}</div>
          </div>
        </div>
        <el-form-item label="支付方式">
          <el-radio-group v-model="buyForm.paymentMethod">
            <el-radio :label="1">线上支付</el-radio>
            <el-radio :label="2">线下自行交易</el-radio>
          </el-radio-group>
          <div class="payment-tip" v-if="buyForm.paymentMethod === 2">
            提示：选择线下交易时，卖家确认后订单会进入交易中，买家确认完成后结束交易，平台不担保资金安全，请当面检查物品。
          </div>
        </el-form-item>
        <el-form-item label="买家留言">
          <el-input type="textarea" v-model="buyForm.buyerNote" rows="2" placeholder="有什么想对卖家说的..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="buyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBuy" :loading="buyLoading">确认下单</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { formatTime } from '@/utils/format'
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, UserFilled, Goods, Star, Warning, ArrowLeft, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const item = ref(null)
const images = ref([])
const isWanted = ref(false)

// 留言相关
const messages = ref([])
const messageLoading = ref(false)
const newMessage = ref('')
const replyMessage = ref('')
const activeReplyId = ref(null) // 当前正在回复的父留言ID
const replyTargetId = ref(null) // 被回复用户的ID
const replyTargetName = ref('') // 被回复用户的昵称

// 举报相关
const reportDialogVisible = ref(false)
const reportLoading = ref(false)
const reportForm = reactive({
  reasonType: '',
  reason: ''
})

// 计算属性
const conditionText = computed(() => {
  if (!item.value) return ''
  const rate = item.value.conditionRate
  return rate === 10 ? '全新' : `${rate}成新`
})

const transactionMethodText = computed(() => {
  if (!item.value) return ''
  const methods = { 1: '自提', 2: '送货上门' }
  return methods[item.value.transactionMethod] || '面交'
})

const daysJoined = computed(() => {
  if (!item.value?.publisher?.createdAt) return 0
  return dayjs().diff(dayjs(item.value.publisher.createdAt), 'day')
})

// formatTime已移至format.js

// 获取详情
const fetchDetail = async () => {
  const id = route.params.id
  if (!id) return
  
  loading.value = true
  try {
    const res = await request.get(`/item/detail/${id}`)
    if (res.code === 200) {
      item.value = res.data
      // 解析图片
      try {
        images.value = JSON.parse(res.data.images || '[]')
      } catch (e) {
        images.value = []
      }
      // 检查是否已收藏
      checkWantStatus(id)
      // 获取留言列表
      fetchMessages(id)
    }
  } catch (error) {
    console.error('获取详情失败', error)
  } finally {
    loading.value = false
  }
}

// 检查收藏状态
const checkWantStatus = async (itemId) => {
  if (!userStore.user.token) return
  try {
    const res = await request.get(`/item/want/check/${itemId}`)
    if (res.code === 200) {
      isWanted.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

// 购买相关
const buyDialogVisible = ref(false)
const buyLoading = ref(false)
const buyForm = reactive({
  paymentMethod: 1,
  buyerNote: ''
})

// 处理购买
const handleBuy = () => {
  if (!userStore.user.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (item.value.userId === userStore.user.userId) {
    ElMessage.warning('不能购买自己发布的物品')
    return
  }

  buyForm.paymentMethod = 1
  buyForm.buyerNote = ''
  buyDialogVisible.value = true
}

const submitBuy = async () => {
  buyLoading.value = true
  try {
    const res = await request.post('/order/create', {
      itemId: item.value.itemId,
      paymentMethod: buyForm.paymentMethod,
      buyerNote: buyForm.buyerNote
    })
    if (res.code === 200) {
      ElMessage.success('下单成功！')
      buyDialogVisible.value = false
      router.push('/order/list')
    }
  } catch (e) {
    console.error(e)
  } finally {
    buyLoading.value = false
  }
}

// 处理收藏/取消收藏
const handleWant = async () => {
  if (!userStore.user.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    const res = await request.post(`/item/want/${item.value.itemId}`)
    if (res.code === 200) {
      isWanted.value = !isWanted.value
      // 更新显示的想要数
      if (isWanted.value) {
        item.value.wantCount = (item.value.wantCount || 0) + 1
        ElMessage.success('收藏成功')
      } else {
        item.value.wantCount = Math.max(0, (item.value.wantCount || 0) - 1)
        ElMessage.success('已取消收藏')
      }
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作出错')
  }
}

const submitReport = async () => {
  if (!reportForm.reasonType) {
    ElMessage.warning('请选择举报原因类型')
    return
  }
  if (!reportForm.reason.trim()) {
    ElMessage.warning('请填写详细描述')
    return
  }

  reportLoading.value = true
  try {
    const res = await request.post('/report/create', {
      targetId: item.value.itemId,
      type: 1, // 1-违规物品
      reason: `[${reportForm.reasonType}] ${reportForm.reason}`
    })
    
    if (res.code === 200) {
      ElMessage.success('举报已提交，我们会尽快处理')
      reportDialogVisible.value = false
      reportForm.reasonType = ''
      reportForm.reason = ''
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    reportLoading.value = false
  }
}

// 获取留言列表
const fetchMessages = async (itemId) => {
  messageLoading.value = true
  try {
    const res = await request.get(`/item-message/list/${itemId}`)
    if (res.code === 200) {
      messages.value = res.data
    }
  } catch (e) {
    console.error('获取留言失败', e)
  } finally {
    messageLoading.value = false
  }
}

// 提交留言或回复
const submitMessage = async (parentId, replyUserId) => {
  const content = parentId ? replyMessage.value : newMessage.value
  if (!content || !content.trim()) {
    ElMessage.warning('留言内容不能为空')
    return
  }
  
  try {
    const res = await request.post('/item-message/add', {
      itemId: item.value.itemId,
      content: content.trim(),
      parentId: parentId,
      replyToUserId: replyUserId
    })
    if (res.code === 200) {
      ElMessage.success('留言成功')
      if (parentId) {
        replyMessage.value = ''
        activeReplyId.value = null
      } else {
        newMessage.value = ''
      }
      fetchMessages(item.value.itemId)
    } else {
      ElMessage.error(res.msg || '留言失败')
    }
  } catch (e) {
    console.error(e)
  }
}

const showReplyInput = (parentId, targetUserId, targetUserName) => {
  if (!userStore.user.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  activeReplyId.value = parentId
  replyTargetId.value = targetUserId
  replyTargetName.value = targetUserName
  replyMessage.value = ''
}

const deleteMessage = async (messageId) => {
  try {
    await ElMessageBox.confirm('确定删除这条留言吗？', '提示', { type: 'warning' })
    const res = await request.delete(`/item-message/delete/${messageId}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchMessages(item.value.itemId)
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.item-detail-page {
  background-color: #f6f7f9;
  min-height: 100vh;
  padding: 20px 0;
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

.item-container {
  background: #fff;
  border-radius: 12px;
  display: flex;
  padding: 20px;
  gap: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.left-section {
  width: 400px;
  flex-shrink: 0;
}

.carousel-image {
  width: 100%;
  height: 100%;
  background: #f8f8f8;
}

.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.item-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 15px 0;
  line-height: 1.4;
}

.price-section {
  margin-bottom: 15px;
  display: flex;
  align-items: baseline;
}

.currency {
  color: #ff5000;
  font-size: 18px;
  font-weight: bold;
  margin-right: 2px;
}

.price {
  color: #ff5000;
  font-size: 32px;
  font-weight: bold;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  margin-left: 10px;
  font-size: 14px;
}

.tags-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tag {
  border: none;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #999;
  font-size: 13px;
  margin-bottom: 25px;
}

.seller-card {
  background-color: #f6f7f9;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.seller-card:hover {
  background-color: #eef0f4;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.seller-name {
  font-weight: bold;
  font-size: 15px;
  color: #333;
}

.seller-desc {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.action-buttons {
  margin-top: auto;
  display: flex;
  gap: 15px;
}

.buy-btn {
  flex: 1;
  background-color: #ffda44;
  border-color: #ffda44;
  color: #333;
  font-weight: bold;
}

.buy-btn:hover {
  background-color: #ffcd00;
  border-color: #ffcd00;
}

.description-section {
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  margin-top: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.section-header {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
  position: relative;
  padding-left: 12px;
}

.section-header::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background: #ffda44;
  border-radius: 2px;
}

.desc-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  margin-bottom: 30px;
}

.images-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-image {
  width: 100%;
  border-radius: 8px;
}

/* 留言板样式 */
.message-board-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-top: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.message-input-area {
  margin-bottom: 20px;
}

.message-action {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.login-tip {
  text-align: center;
  padding: 20px 0;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.message-list {
  margin-top: 20px;
}

.message-item {
  display: flex;
  gap: 15px;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.message-item:last-child {
  border-bottom: none;
}

.message-content-area {
  flex: 1;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.message-user {
  font-weight: 600;
  color: #333;
  margin-right: 10px;
}

.seller-badge {
  background: #ffda44;
  color: #333;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 10px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 8px;
  white-space: pre-wrap;
}

.message-footer {
  display: flex;
  gap: 15px;
  font-size: 13px;
}

.reply-btn, .delete-btn {
  color: #999;
  cursor: pointer;
}

.reply-btn:hover, .delete-btn:hover {
  color: #ffda44;
}

.delete-btn:hover {
  color: #f56c6c;
}

.reply-input-box {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  align-items: center;
  background: #f9f9f9;
  padding: 10px;
  border-radius: 6px;
}

.replies-list {
  margin-top: 15px;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.reply-item {
  margin-bottom: 15px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 13px;
}

.reply-user {
  font-weight: 600;
  color: #333;
  margin-right: 5px;
}

.reply-text-gap {
  color: #666;
  margin: 0 5px;
}

.reply-target {
  color: #409eff;
  margin-right: 10px;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.buy-item-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.buy-item-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.buy-title {
  font-weight: bold;
  margin-bottom: 5px;
  font-size: 15px;
}

.buy-price {
  color: #ff5000;
  font-weight: bold;
  font-size: 16px;
}

.payment-tip {
  font-size: 12px;
  color: #e6a23c;
  margin-top: 8px;
  line-height: 1.4;
}

.reply-text {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 5px;
  white-space: pre-wrap;
}

.reply-footer {
  display: flex;
  gap: 15px;
  font-size: 12px;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .item-container {
    flex-direction: column;
    padding: 10px;
  }
  .left-section, .right-section {
    width: 100%;
    padding: 0;
  }
}
</style>
