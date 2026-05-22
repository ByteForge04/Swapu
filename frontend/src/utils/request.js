import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api', // Proxy in vite.config.js will handle this
  timeout: 60000 // 增加超时时间到 60 秒，为了等待 AI 响应
})

// Request interceptor
request.interceptors.request.use(
  config => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.token) {
      config.headers['Authorization'] = user.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 401) {
      ElMessage.error(res.msg || '登录已过期，请重新登录')
      localStorage.clear()
      sessionStorage.clear()
      window.location.href = '/login'
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    if (res.code !== 200) {
      ElMessage.error(res.msg || 'Error')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || 'Request Error')
    return Promise.reject(error)
  }
)

export default request
