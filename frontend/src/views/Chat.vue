<template>
  <div class="chat-page">
    <div class="page-header">
      <el-button icon="ArrowLeft" circle @click="$router.back()"></el-button>
      <span class="header-title">消息中心</span>
    </div>
    
    <el-container class="chat-container">
      <el-aside width="280px" class="chat-sidebar">
        <div class="sidebar-header">
          <span>最近联系人</span>
        </div>
        <div class="contact-list" v-loading="loadingContacts">
          <el-empty v-if="contacts.length === 0" description="暂无联系人" />
          <div 
            v-for="contact in contacts" 
            :key="contact.contactId"
            class="contact-item"
            :class="{ active: currentContact?.contactId === contact.contactId }"
            @click="selectContact(contact)"
          >
            <el-badge :value="contact.unreadCount" :hidden="!contact.unreadCount" class="contact-badge">
              <el-avatar :size="40" :src="contact.avatar" icon="UserFilled" />
            </el-badge>
            <div class="contact-info">
              <div class="contact-name">{{ contact.nickname }}</div>
              <div class="contact-time" v-if="contact.lastTime">{{ formatTime(contact.lastTime) }}</div>
            </div>
          </div>
        </div>
      </el-aside>

      <el-main class="chat-main">
        <div v-if="currentContact" class="chat-window">
          <div class="chat-header">
            <span>与 {{ currentContact.nickname }} 聊天中</span>
          </div>
          
          <div class="message-list" ref="messageListRef">
            <div v-for="msg in messages" :key="msg.msgId" class="message-row" :class="{ 'is-me': msg.senderId === userStore.user.userId }">
              <el-avatar v-if="msg.senderId !== userStore.user.userId" :size="36" :src="currentContact.avatar" icon="UserFilled" class="msg-avatar" />
              <div class="message-bubble">
                <div v-if="msg.msgType === 1" class="text-msg">{{ msg.content }}</div>
                <div v-else-if="msg.msgType === 2" class="image-msg">
                  <el-image :src="msg.content" :preview-src-list="[msg.content]" fit="cover" />
                </div>
                <div v-else-if="msg.msgType === 3" class="item-card-msg" @click="$router.push(`/item/${msg.relatedId}`)">
                  <!-- 商品卡片简易展示 -->
                  <div class="item-link">查看商品详情</div>
                </div>
              </div>
              <el-avatar v-if="msg.senderId === userStore.user.userId" :size="36" :src="userStore.user.avatar" icon="UserFilled" class="msg-avatar is-me-avatar" />
            </div>
          </div>

          <div class="chat-input-area">
            <div class="toolbar">
              <el-upload
                action="/api/common/upload"
                :show-file-list="false"
                :on-success="handleImageSuccess"
                :headers="{ Authorization: userStore.user.token }"
                accept="image/*"
              >
                <el-button link icon="Picture">发送图片</el-button>
              </el-upload>
            </div>
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="3"
              placeholder="按 Enter 发送，Shift + Enter 换行"
              @keydown.enter.prevent="handleEnter"
            />
            <div class="input-actions">
              <el-button type="primary" @click="sendTextMessage">发送</el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-chat">
          <el-empty description="选择一个联系人开始聊天吧" />
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { formatTime } from '@/utils/format'
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Picture, UserFilled, ArrowLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const contacts = ref([])
const loadingContacts = ref(false)
const currentContact = ref(null)
const messages = ref([])
const inputText = ref('')
const messageListRef = ref(null)

let ws = null
let reconnectTimer = null
let heartbeatTimer = null

// 移至 format.js

// 获取联系人列表
const fetchContacts = async () => {
  loadingContacts.value = true
  try {
    const res = await request.get('/chat/contacts')
    if (res.code === 200) {
      contacts.value = res.data
      
      // 更新全局未读总数
      userStore.updateUnreadChatCount()
      
      // 如果路由带了 targetId，则默认选中
      const targetId = route.query.targetId
      if (targetId) {
        const targetUser = contacts.value.find(c => c.contactId == targetId)
        if (targetUser) {
          selectContact(targetUser)
        } else {
          // 如果不在最近列表中，尝试获取基本信息并塞入
          fetchTargetUserInfo(targetId)
        }
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loadingContacts.value = false
  }
}

// 补充未聊过天的用户信息
const fetchTargetUserInfo = async (userId) => {
  try {
    const res = await request.get(`/user/info/${userId}`)
    if (res.code === 200) {
      const newUser = {
        contactId: res.data.userId,
        nickname: res.data.nickname || res.data.username,
        avatar: res.data.avatar,
        unreadCount: 0
      }
      contacts.value.unshift(newUser)
      selectContact(newUser)
    }
  } catch (e) {
    console.error(e)
  }
}

// 选择联系人
const selectContact = async (contact) => {
  currentContact.value = contact
  contact.unreadCount = 0
  await fetchHistory(contact.contactId)
  await request.post(`/chat/read/${contact.contactId}`)
  userStore.updateUnreadChatCount() // 标记已读后更新全局未读数
  scrollToBottom()
}

// 获取历史记录
const fetchHistory = async (contactId) => {
  try {
    const res = await request.get(`/chat/history/${contactId}`)
    if (res.code === 200) {
      messages.value = res.data
      scrollToBottom()
    }
  } catch (e) {
    console.error(e)
  }
}

// 建立 WebSocket 连接
const connectWebSocket = () => {
  if (!userStore.user.token) return
  if (ws && ws.readyState === WebSocket.OPEN) return
  
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  
  // 智能判断后端 WebSocket 地址：
  // 1. 如果有全局 VITE_API_URL 配置，则从其中提取 host
  // 2. 如果是在本地开发环境，则默认连接本机的 8080 端口
  // 3. 如果是线上环境，则连接当前域名
  let host = window.location.host
  const apiUrl = import.meta.env.VITE_API_URL
  if (apiUrl) {
    // 提取 http:// 或 https:// 之后的 host 部分
    host = apiUrl.replace(/^https?:\/\//, '').split('/')[0]
  } else if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    host = window.location.hostname + ':8080'
  }
  
  const wsUrl = `${protocol}//${host}/ws/chat/${userStore.user.userId}?token=${userStore.user.token}`
  console.log('Connecting to WebSocket:', wsUrl)
  
  try {
    ws = new WebSocket(wsUrl)
  } catch (e) {
    console.error('WebSocket creation failed', e)
    return
  }
  
  ws.onopen = () => {
    console.log('WebSocket Connected')
    startHeartbeat()
  }
  
  ws.onmessage = (e) => {
    if (e.data === 'pong') return
    try {
      const msg = JSON.parse(e.data)
      
      if (currentContact.value && currentContact.value.contactId === msg.senderId) {
        messages.value.push(msg)
        scrollToBottom()
        request.post(`/chat/read/${msg.senderId}`)
        userStore.updateUnreadChatCount() // 虽然当前窗口是激活的，但保险起见刷新一下全局未读
      } else {
        const contact = contacts.value.find(c => c.contactId === msg.senderId)
        if (contact) {
          contact.unreadCount = (contact.unreadCount || 0) + 1
          userStore.updateUnreadChatCount() // 收到非当前窗口的新消息，更新红点
        } else {
          fetchContacts() 
        }
      }
    } catch (err) {
      console.error('Error parsing message', err)
    }
  }
  
  ws.onerror = (e) => {
    console.error('WebSocket Error', e)
  }
  
  ws.onclose = () => {
    console.log('WebSocket Closed')
    stopHeartbeat()
    if (reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(() => {
      console.log('Reconnecting WebSocket...')
      connectWebSocket()
    }, 3000)
  }
}

const startHeartbeat = () => {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send('ping')
    }
  }, 20000)
}

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

// 发送文本消息
const sendTextMessage = () => {
  if (!inputText.value.trim() || !currentContact.value) return
  
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.error('聊天连接已断开，正在重新连接...')
    connectWebSocket()
    return
  }
  
  const msg = {
    receiverId: currentContact.value.contactId,
    content: inputText.value.trim(),
    msgType: 1
  }
  
  try {
    ws.send(JSON.stringify(msg))
    
    // 本地也追加一条
    messages.value.push({
      msgId: Date.now(), // 临时ID
      senderId: userStore.user.userId,
      receiverId: currentContact.value.contactId,
      content: inputText.value.trim(),
      msgType: 1,
      createdAt: new Date()
    })
    
    inputText.value = ''
    scrollToBottom()
  } catch (e) {
    console.error('发送异常:', e)
    ElMessage.error('消息发送失败')
  }
}

// 处理回车发送
const handleEnter = (e) => {
  if (e.shiftKey) return
  sendTextMessage()
}

// 发送图片
const handleImageSuccess = (res) => {
  if (res.code === 200 && currentContact.value) {
    if (!ws || ws.readyState !== WebSocket.OPEN) {
      ElMessage.error('聊天连接已断开，正在重新连接...')
      connectWebSocket()
      return
    }
    
    const msg = {
      receiverId: currentContact.value.contactId,
      content: res.data, // URL
      msgType: 2
    }
    
    try {
      ws.send(JSON.stringify(msg))
      
      messages.value.push({
        msgId: Date.now(),
        senderId: userStore.user.userId,
        receiverId: currentContact.value.contactId,
        content: res.data,
        msgType: 2,
        createdAt: new Date()
      })
      scrollToBottom()
    } catch (e) {
      console.error('图片发送异常:', e)
      ElMessage.error('图片发送失败')
    }
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
    // 增加延时以应对图片或DOM异步渲染导致的高度计算不准确
    setTimeout(() => {
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    }, 100)
  })
}

onMounted(() => {
  if (!userStore.user.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchContacts()
  connectWebSocket()
})

onUnmounted(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<style scoped>
.chat-page {
  height: 100vh;
  background: #f6f7f9;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  width: 1000px;
  max-width: 100%;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  margin-left: 15px;
  color: #333;
}

.chat-container {
  width: 1000px;
  max-width: 100%;
  height: 80vh;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.05);
  overflow: hidden;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .chat-page {
    padding: 10px;
  }
  
  .page-header {
    margin-bottom: 10px;
  }

  .chat-container {
    height: calc(100vh - 70px);
    border-radius: 8px;
    flex-direction: column;
  }
  
  .chat-sidebar {
    width: 100% !important;
    height: 150px;
    border-right: none;
    border-bottom: 1px solid #ebeef5;
  }
  
  .contact-list {
    display: flex;
    overflow-x: auto;
    overflow-y: hidden;
  }
  
  .contact-item {
    flex-direction: column;
    padding: 10px;
    min-width: 80px;
  }
  
  .contact-badge {
    margin-right: 0;
    margin-bottom: 5px;
  }
  
  .contact-info {
    text-align: center;
    width: 100%;
  }
  
  .contact-time {
    display: none;
  }
  
  .chat-input-area {
    height: 120px;
  }
  
  .chat-header {
    height: 40px;
    font-size: 14px;
  }
}

.chat-sidebar {
  border-right: 1px solid #ebeef5;
  background: #fafafa;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  font-weight: bold;
  color: #333;
}

.contact-list {
  flex: 1;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.contact-item:hover {
  background: #f0f0f0;
}

.contact-item.active {
  background: #eef5fe;
}

.contact-badge {
  margin-right: 15px;
}

.contact-info {
  flex: 1;
  overflow: hidden;
}

.contact-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-time {
  font-size: 12px;
  color: #999;
}

.chat-main {
  padding: 0;
  display: flex;
  flex-direction: column;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  font-weight: bold;
  font-size: 16px;
}

.message-list {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f5f7fa;
}

.message-row {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.message-row.is-me {
  justify-content: flex-end;
}

.msg-avatar {
  margin-right: 12px;
}

.msg-avatar.is-me-avatar {
  margin-right: 0;
  margin-left: 12px;
}

.message-bubble {
  max-width: 60%;
}

.text-msg {
  background: #fff;
  padding: 10px 15px;
  border-radius: 8px;
  border-top-left-radius: 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.is-me .text-msg {
  background: #ffda44;
  border-top-left-radius: 8px;
  border-top-right-radius: 0;
}

.image-msg .el-image {
  max-width: 200px;
  border-radius: 8px;
  cursor: pointer;
}

.item-link {
  background: #fff;
  padding: 10px;
  border-radius: 8px;
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
}

.chat-input-area {
  height: 160px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.toolbar {
  height: 40px;
  padding: 0 10px;
  display: flex;
  align-items: center;
}

.chat-input-area :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  resize: none;
  background: transparent;
}

.input-actions {
  padding: 10px 20px;
  display: flex;
  justify-content: flex-end;
}

.empty-chat {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>