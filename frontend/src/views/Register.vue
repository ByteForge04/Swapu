<template>
  <div class="register-container">
    <div class="register-content">
      <el-card class="register-card xy-card">
        <template #header>
          <div class="card-header">
            <h2>加入 SwapU</h2>
            <p>开启你的校园交易之旅</p>
          </div>
        </template>
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" size="large" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名/学号" :prefix-icon="User" />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="设置密码" :prefix-icon="Lock" show-password />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Check" show-password />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="registerForm.nickname" placeholder="给自己起个好听的名字" :prefix-icon="Postcard" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="registerForm.phone" placeholder="用于联系和找回密码" :prefix-icon="Iphone" />
          </el-form-item>
          <el-form-item class="submit-item">
            <el-button type="primary" :loading="loading" class="w-100 register-btn" @click="handleRegister" round>立即注册</el-button>
          </el-form-item>
          <div class="form-footer">
            <router-link to="/login">已有账号？去登录</router-link>
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
import { ElMessage } from 'element-plus'
import { User, Lock, Check, Postcard, Iphone } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...submitData } = registerForm
        const success = await userStore.register(submitData)
        if (success) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f6f7f9;
  padding: 40px 0;
}

.register-card {
  width: 480px;
  border-radius: 16px;
}

.card-header {
  text-align: center;
  margin-bottom: 10px;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.card-header p {
  margin: 5px 0 0;
  color: #999;
  font-size: 14px;
}

.w-100 {
  width: 100%;
}

.register-btn {
  font-size: 16px;
  height: 44px;
  margin-top: 10px;
}

.form-footer {
  text-align: center;
  font-size: 14px;
}

.form-footer a {
  color: #666;
}

.form-footer a:hover {
  color: #ffda44;
}
</style>
