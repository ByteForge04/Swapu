import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 读取用户信息 (包含 token)
  const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

  const login = async (loginForm) => {
    const res = await request.post('/user/login', loginForm)
    if (res.code === 200) {
      // res.data 结构: { token: '...', user: { ... } }
      const loginVO = res.data
      
      // 组合存储: 将 token 和 user 信息合并存入
      const userData = {
        token: loginVO.token,
        ...loginVO.user
      }
      
      user.value = userData
      localStorage.setItem('user', JSON.stringify(userData))
      return true
    }
    return false
  }

  const register = async (registerForm) => {
    const res = await request.post('/user/register', registerForm)
    return res.code === 200
  }

  const logout = () => {
    user.value = {}
    localStorage.clear() // 完全清理所有本地缓存
    sessionStorage.clear() // 清理 session 缓存
    unreadCount.value = 0
    unreadChatCount.value = 0
  }
  
  const unreadCount = ref(0)
  const unreadChatCount = ref(0)
  
  const updateUnreadCount = async () => {
    if (!user.value.token) return
    try {
      const res = await request.get('/notification/unread-count')
      if (res.code === 200) {
        unreadCount.value = res.data
      }
    } catch (e) {
      console.error(e)
    }
  }

  const updateUnreadChatCount = async () => {
    if (!user.value.token) return
    try {
      const res = await request.get('/chat/unread-count')
      if (res.code === 200) {
        unreadChatCount.value = res.data
      }
    } catch (e) {
      console.error(e)
    }
  }

  return { user, login, register, logout, unreadCount, unreadChatCount, updateUnreadCount, updateUnreadChatCount }
})
