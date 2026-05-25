<template>
  <div class="ai-assistant-page">
    <el-container class="chat-container">
      <!-- 头部 -->
      <el-header class="chat-header">
        <div class="header-left" @click="$router.push('/')">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <span class="logo-text">SwapU AI</span>
          <div class="ai-badge">智能导购</div>
        </div>
        <div class="header-right">
          <el-button type="primary" link @click="clearChat">
            <el-icon><Delete /></el-icon> 清空对话
          </el-button>
        </div>
      </el-header>

      <!-- 聊天内容区 -->
      <el-main class="chat-main" ref="chatMainRef">
        <div class="message-list">
          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            :class="['message-wrapper', msg.role === 'user' ? 'is-user' : 'is-ai']"
          >
            <div class="avatar" v-if="msg.role === 'ai'">
              <el-avatar class="ai-avatar" :size="36">AI</el-avatar>
            </div>
            <div class="message-content">
              <div class="message-bubble">
                <span class="bubble-text" v-html="formatMessage(msg.content)"></span>
              </div>
              
              <!-- 渲染推荐商品卡片 -->
              <div v-if="msg.items && msg.items.length > 0" class="recommend-items-container">
                <div
                  v-for="(item, idx) in msg.items"
                  :key="idx" 
                  class="recommend-item-card"
                  @click="$router.push(`/item/${item.itemId}`)"
                >
                  <img :src="getCoverImage(item.images)" class="item-img" v-if="getCoverImage(item.images)" />
                  <div class="item-img placeholder" v-else>
                    <el-icon><Picture /></el-icon>
                  </div>
                  <div class="item-info">
                    <div class="item-title">{{ item.title }}</div>
                    <div class="item-price">
                      <span class="price-symbol">¥</span>{{ item.price }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="isTyping" class="message-wrapper is-ai">
            <div class="avatar">
              <el-avatar class="ai-avatar" :size="36">AI</el-avatar>
            </div>
            <div class="message-content">
              <div class="message-bubble typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </el-main>

      <!-- 输入区 -->
      <el-footer class="chat-footer" height="auto">
        <div class="input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 6 }"
            placeholder="问问 AI 助手，例如：怎么快速卖出我的闲置？"
            @keydown.enter.prevent="sendMessage"
            class="chat-input"
          />
          <el-button 
            type="primary" 
            class="send-btn" 
            :icon="Promotion" 
            circle 
            :disabled="!inputMessage.trim() || isTyping"
            @click="sendMessage"
          />
        </div>
        <div class="footer-tip">
          AI 助手可能会犯错，请核实重要信息。
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { getCoverImage } from '@/utils/format'
import { ref, reactive, nextTick, onMounted, watch } from 'vue'
import { ArrowLeft, Delete, UserFilled, Promotion, Picture } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const chatMainRef = ref(null)
const inputMessage = ref('')
const isTyping = ref(false)

const formatMessage = (text) => {
  if (!text) return ''
  // 简单的 Markdown 换行和加粗处理
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
}

const defaultMessages = [
  {
    role: 'ai',
    content: '你好！我是 SwapU 智能助手。你可以问我关于发布闲置、或者让我帮你寻找平台上的商品（我会根据现有的商品为你推荐哦）。'
  }
]

const messages = ref([...defaultMessages])

// 从本地存储加载历史记录
onMounted(() => {
  const history = localStorage.getItem('swapu_ai_chat_history')
  if (history) {
    try {
      messages.value = JSON.parse(history)
    } catch (e) {
      console.error('解析聊天记录失败', e)
    }
  }
  scrollToBottom()
})

// 监听消息变化，保存到本地存储
watch(messages, (newVal) => {
  localStorage.setItem('swapu_ai_chat_history', JSON.stringify(newVal))
}, { deep: true })

// getCoverImage已移至format.js

const scrollToBottom = async () => {
  await nextTick()
  if (chatMainRef.value) {
    const el = chatMainRef.value.$el
    el.scrollTop = el.scrollHeight
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return

  const userMsg = inputMessage.value.trim()
  messages.value.push({
    role: 'user',
    content: userMsg
  })
  
  inputMessage.value = ''
  scrollToBottom()
  
  isTyping.value = true
  
  const aiMessageIndex = messages.value.length
  
  try {
    // 构造要发送的历史记录
    const historyToSend = messages.value
      .slice(1) // 排除第一条欢迎语
      .map(msg => ({
        role: msg.role,
        content: msg.content
      }))

    const response = await request.post('/ai/chat', {
      message: userMsg,
      history: historyToSend
    })

    if (response.code === 200) {
      messages.value.push({
        role: 'ai',
        content: response.data.text,
        items: response.data.items || []
      })
    } else {
      messages.value.push({
        role: 'ai',
        content: response.msg || 'AI 助手开小差了，请稍后再试'
      })
    }
  } catch (error) {
    console.error('Chat error:', error)
    messages.value.push({
      role: 'ai',
      content: '网络似乎有点问题，AI 助手暂时无法响应。'
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

const clearChat = () => {
  messages.value = [...defaultMessages]
  localStorage.removeItem('swapu_ai_chat_history')
}
</script>

<style scoped>
.ai-assistant-page {
  height: 100vh;
  width: 100vw;
  background-color: #f5f7fa; /* 更柔和的背景色 */
  display: flex;
  justify-content: center;
  align-items: center;
  box-sizing: border-box;
}

.chat-container {
  width: 100%;
  max-width: 1000px; /* 增加一点宽度，适应闲鱼大屏布局 */
  height: 100vh;
  background: #ffffff;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05); /* 更柔和的阴影 */
  display: flex;
  flex-direction: column;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .chat-header {
    padding: 0 15px !important;
  }
  
  .ai-badge {
    display: none !important;
  }
  
  .message-wrapper {
    max-width: 92% !important;
  }
  
  .recommend-items-container {
    flex-direction: column;
  }
  
  .recommend-item-card {
    width: 100% !important;
  }
  
  .input-area {
    padding: 10px !important;
  }
  
  .chat-input {
    margin-right: 10px !important;
  }
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #ffffff;
  height: 64px;
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
  transition: opacity 0.2s;
  position: relative;
}

.header-left:hover {
  opacity: 0.8;
}

.back-icon {
  font-size: 20px;
  margin-right: 12px;
  font-weight: bold;
}

.logo-text {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.ai-badge {
  background-color: #ff5000;
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px 10px 10px 0;
  font-weight: 600;
  margin-left: 6px;
  transform: translateY(-8px);
}

.header-right .el-button {
  color: #999;
  font-size: 14px;
}

.header-right .el-button:hover {
  color: #ff5000;
}

.chat-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f8f9fa; /* 稍微带点灰度的背景区分聊天区 */
  scroll-behavior: smooth;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding-bottom: 20px;
}

.message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 75%;
}

.message-wrapper.is-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-wrapper.is-ai {
  align-self: flex-start;
}

.ai-avatar {
  background: linear-gradient(135deg, #ffda44, #ff8c00); /* 闲鱼黄到橙色的渐变 */
  color: #fff;
  font-size: 20px;
  box-shadow: 0 4px 10px rgba(255, 140, 0, 0.2);
}

.custom-ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.message-content {
  display: flex;
  flex-direction: column;
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.bubble-text {
  white-space: pre-wrap;
}

.bubble-text :deep(strong) {
  color: #ff5000;
  font-weight: 600;
}

.is-user .message-bubble {
  background-color: #ffda44; /* 用户发送的用闲鱼黄 */
  color: #333;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(255, 218, 68, 0.2);
}

.is-ai .message-bubble {
  background-color: #ffffff;
  color: #333;
  border-top-left-radius: 4px;
}

/* 打字机动画 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 18px 20px !important;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background-color: #ffda44;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

/* 推荐商品卡片样式 (类似闲鱼流式卡片) */
.recommend-items-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
  max-width: 320px;
}

.recommend-item-card {
  display: flex;
  background-color: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.recommend-item-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.08);
  border-color: #ffda44;
}

.item-img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  background-color: #f5f5f5;
}

.item-img.placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #ccc;
  font-size: 24px;
}

.item-info {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  flex: 1;
  overflow: hidden;
  padding: 2px 0;
}

.item-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
}

.item-price {
  font-size: 16px;
  color: #ff5000; /* 闲鱼红 */
  font-weight: bold;
  margin-top: 6px;
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 12px;
  margin-right: 2px;
  font-weight: normal;
}

.chat-footer {
  padding: 16px 24px 24px;
  background-color: #ffffff;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  z-index: 10;
}

.input-area {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  width: 100%;
  background: #f5f7fa;
  padding: 10px 16px;
  border-radius: 28px;
  border: 1px solid transparent;
  transition: all 0.3s ease;
}

.input-area:focus-within {
  border-color: #ffda44;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(255, 218, 68, 0.1);
}

.chat-input {
  flex: 1;
}

:deep(.chat-input .el-textarea__inner) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
  padding: 6px 4px;
  font-size: 15px;
  resize: none;
  color: #333;
  line-height: 1.5;
}

:deep(.chat-input .el-textarea__inner::placeholder) {
  color: #aaa;
}

.send-btn {
  margin-bottom: 2px;
  width: 40px;
  height: 40px;
  background-color: #ffda44 !important;
  border-color: #ffda44 !important;
  color: #333 !important;
  font-size: 18px;
  transition: transform 0.2s;
}

.send-btn:not(:disabled):hover {
  transform: scale(1.05);
  background-color: #ffcd00 !important;
}

.send-btn:disabled {
  background-color: #f0f0f0 !important;
  border-color: #f0f0f0 !important;
  color: #ccc !important;
}

.footer-tip {
  font-size: 12px;
  color: #aaa;
  text-align: center;
}
</style>
