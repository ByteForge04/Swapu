<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-left">
        <div class="brand-intro">
          <h1>SwapU</h1>
          <p>校园闲置，转手遇到爱</p>
          <p class="sub-intro">安全 · 便捷 · 就在你身边</p>
        </div>
      </div>
      <el-card class="login-card xy-card">
        <template #header>
          <div class="card-header">
            <h2>欢迎登录</h2>
          </div>
        </template>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" size="large">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名/学号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="w-100 login-btn" @click="handleLogin" round>登 录</el-button>
          </el-form-item>
          <div class="form-footer">
            <span class="no-account">还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login(loginForm)
        if (success) {
          ElMessage.success('登录成功')
          // 判断角色跳转
          if (userStore.user.role === 1) {
            router.push('/admin')
          } else {
            router.push('/')
          }
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #fff9d6 0%, #ffffff 100%);
  position: relative;
  overflow: hidden;
}

/* 装饰背景圆 */
.login-container::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: #ffda44;
  opacity: 0.2;
  border-radius: 50%;
  filter: blur(60px);
}

.login-content {
  display: flex;
  align-items: center;
  gap: 80px;
  z-index: 1;
}

.login-left {
  text-align: left;
}

.brand-intro h1 {
  font-size: 64px;
  color: #333;
  margin: 0;
  font-weight: 800;
  letter-spacing: -2px;
}

.brand-intro p {
  font-size: 24px;
  color: #666;
  margin: 10px 0 0;
}

.brand-intro .sub-intro {
  font-size: 16px;
  color: #999;
  margin-top: 5px;
}

.login-card {
  width: 400px;
  border-radius: 16px;
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  color: #333;
  text-align: center;
}

.w-100 {
  width: 100%;
}

.login-btn {
  font-size: 16px;
  letter-spacing: 2px;
  height: 48px;
}

.form-footer {
  text-align: center;
  font-size: 14px;
  margin-top: 10px;
}

.no-account {
  color: #999;
}

.register-link {
  color: #ffda44;
  font-weight: bold;
  margin-left: 5px;
}

.register-link:hover {
  text-decoration: underline;
  color: #e6c43d;
}
</style>
