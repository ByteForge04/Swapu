<template>
  <div class="profile-container">
    <div class="profile-layout">
      <div class="profile-sidebar xy-card">
        <div class="user-summary">
          <el-upload
            class="avatar-uploader"
            action="/api/common/upload"
            :show-file-list="false"
            :headers="uploadHeaders"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="userInfo.avatar" :src="userInfo.avatar" class="large-avatar-img" />
            <el-icon v-else class="large-avatar-icon"><UserFilled /></el-icon>
            <div class="avatar-mask">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </el-upload>
          
          <h3>{{ userStore.user.nickname || userStore.user.username }}</h3>
          <p class="credit-score">
            信用分: <span class="score-val">{{ userInfo.creditScore || 100 }}</span>
          </p>
        </div>
        <el-menu
          :default-active="activeTab"
          class="profile-menu"
          @select="(index) => activeTab = index"
        >
          <el-menu-item index="order">
            <el-icon><List /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          <el-menu-item index="info">
            <el-icon><User /></el-icon>
            <span>基本资料</span>
          </el-menu-item>
          <el-menu-item index="publish">
            <el-icon><Goods /></el-icon>
            <span>我发布的</span>
          </el-menu-item>
          <el-menu-item index="want">
            <el-icon><Star /></el-icon>
            <span>我收藏的</span>
          </el-menu-item>
          <el-menu-item index="password">
            <el-icon><Lock /></el-icon>
            <span>安全设置</span>
          </el-menu-item>
          <el-menu-item index="home" @click="handleHomeClick">
            <el-icon><HomeFilled /></el-icon>
            <span>返回首页</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="profile-content xy-card">
        <div class="content-header">
          <h2 v-if="activeTab === 'info'">编辑资料</h2>
          <h2 v-else-if="activeTab === 'password'">修改密码</h2>
          <h2 v-else-if="activeTab === 'publish'">我发布的</h2>
          <h2 v-else-if="activeTab === 'want'">我收藏的</h2>
          <h2 v-else-if="activeTab === 'order'">我的订单</h2>
        </div>
        
        <!-- 我的订单 -->
        <div class="content-body" v-if="activeTab === 'order'">
          <OrderList :is-embedded="true" />
        </div>
        <div class="content-body" v-else-if="activeTab === 'publish' || activeTab === 'want'">
          <div v-loading="listLoading">
            <el-empty v-if="!listLoading && itemList.length === 0" description="暂无数据" />
            <div class="item-list" v-else>
              <div 
                v-for="item in itemList" 
                :key="item.itemId" 
                class="list-item"
                @click="$router.push(`/item/${item.itemId}`)"
              >
                <el-image 
                  :src="getCoverImage(item.images)" 
                  fit="cover" 
                  class="item-cover"
                />
                <div class="item-details">
                  <div class="item-title">{{ item.title }}</div>
                  <div class="item-price">¥ {{ item.price }}</div>
                  <div class="item-status">
                    <el-tag size="small" :type="getStatusType(item.status)">
                      {{ getStatusText(item.status) }}
                    </el-tag>
                    <span class="view-count">{{ item.viewCount || 0 }}浏览</span>
                  </div>
                </div>
                
                <div class="item-actions" v-if="activeTab === 'publish'">
                  <el-button 
                    v-if="item.status === 0 || item.status === 1 || item.status === 4" 
                    round
                    size="small" 
                    class="action-btn edit-btn"
                    @click.stop="handleEdit(item)"
                  >
                    编辑
                  </el-button>
                  <el-button 
                    v-if="item.status === 1" 
                    round
                    size="small" 
                    class="action-btn off-shelf-btn"
                    @click.stop="handleUpdateStatus(item, 4)"
                  >
                    下架
                  </el-button>
                  <el-button 
                    v-if="item.status === 4" 
                    round
                    size="small" 
                    class="action-btn on-shelf-btn"
                    @click.stop="handleUpdateStatus(item, 1)"
                  >
                    上架
                  </el-button>
                  <el-button 
                    round
                    size="small" 
                    class="action-btn delete-btn"
                    @click.stop="handleDelete(item)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="content-body" v-else-if="activeTab === 'info'">
          <el-form :model="userInfo" label-width="80px" size="large" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="userInfo.nickname" placeholder="设置一个好听的昵称" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userInfo.phone" placeholder="方便买家联系" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" placeholder="绑定邮箱" />
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="微信号">
                  <el-input v-model="userInfo.wechatId" placeholder="WeChat ID" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="QQ号">
                  <el-input v-model="userInfo.qqId" placeholder="QQ Number" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateInfo" :loading="loading" class="save-btn">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="content-body" v-else-if="activeTab === 'password'">
          <el-alert title="为了您的账号安全，建议定期修改密码" type="warning" show-icon :closable="false" class="mb-20" />
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" size="large" class="profile-form">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="loading" class="save-btn">确认修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getCoverImage } from '@/utils/format'
import { ref, reactive, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, User, Lock, HomeFilled, Camera, Goods, Star, List } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import OrderList from './OrderList.vue'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('info')
const loading = ref(false)
const passwordFormRef = ref(null)
const itemList = ref([])
const listLoading = ref(false)

const userInfo = reactive({
  username: '',
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  wechatId: '',
  qqId: '',
  creditScore: 100
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const uploadHeaders = {
  Authorization: userStore.user.token
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await request.get('/user/info')
    if (res.code === 200) {
      Object.assign(userInfo, res.data)
      // 更新store中的信息
      userStore.user.nickname = res.data.nickname
      userStore.user.avatar = res.data.avatar
      localStorage.setItem('user', JSON.stringify(userStore.user))
    }
  } catch (error) {
    console.error(error)
  }
}

// 更新用户信息
const handleUpdateInfo = async () => {
  loading.value = true
  try {
    const res = await request.post('/user/update', userInfo)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      fetchUserInfo()
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 修改密码
const handleChangePassword = () => {
  if (!passwordFormRef.value) return
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/user/password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          userStore.logout()
          window.location.href = '/login'
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 监听 Tab 切换
watch(activeTab, (val) => {
  if (val === 'publish') {
    fetchMyPublish()
  } else if (val === 'want') {
    fetchMyWant()
  }
})

const fetchMyPublish = async () => {
  listLoading.value = true
  try {
    const res = await request.get('/item/my/publish')
    if (res.code === 200) {
      itemList.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    listLoading.value = false
  }
}

const fetchMyWant = async () => {
  listLoading.value = true
  try {
    const res = await request.get('/item/my/want')
    if (res.code === 200) {
      itemList.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    listLoading.value = false
  }
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '在售', 2: '交易中', 3: '已售出', 4: '已下架' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }
  return map[status] || ''
}

const handleUpdateStatus = async (item, status) => {
  try {
    const actionText = status === 4 ? '下架' : '上架'
    await ElMessageBox.confirm(`确定要${actionText}该物品吗？`, '提示', {
      type: 'warning'
    })
    
    const res = await request.post(`/item/status/${item.itemId}/${status}`)
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      fetchMyPublish() // 刷新列表
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleEdit = (item) => {
  router.push(`/publish?id=${item.itemId}`)
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除该物品吗？删除后不可恢复', '警告', {
      type: 'warning',
      confirmButtonText: '确定删除',
      confirmButtonClass: 'el-button--danger'
    })
    
    const res = await request.delete(`/item/${item.itemId}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchMyPublish() // 刷新列表
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleHomeClick = () => {
  router.push('/')
}

const handleOrderClick = () => {
  // router.push('/order/list')
}

// 头像上传成功回调
const handleAvatarSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    userInfo.avatar = response.data
    handleUpdateInfo() // 自动保存
    ElMessage.success('头像更新成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const beforeAvatarUpload = (rawFile) => {
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

onMounted(() => {
  fetchUserInfo()
  // 如果初始tab是publish或want，也需要加载数据
  if (activeTab.value === 'publish') {
    fetchMyPublish()
  } else if (activeTab.value === 'want') {
    fetchMyWant()
  }
})
</script>

<style scoped>
.item-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  margin-left: 15px;
}

.action-btn {
  margin-left: 0 !important;
  width: 70px;
  font-weight: normal;
}

.on-shelf-btn {
  background-color: #ffda44;
  border-color: #ffda44;
  color: #333;
  font-weight: bold;
}
.on-shelf-btn:hover {
  background-color: #ffcd00;
  border-color: #ffcd00;
}

.off-shelf-btn {
  background-color: #f6f7f9;
  border-color: #e5e5e5;
  color: #666;
}
.off-shelf-btn:hover {
  background-color: #eee;
  border-color: #ddd;
}

.delete-btn {
  background-color: #fff;
  border-color: #ff4d4f;
  color: #ff4d4f;
}
.delete-btn:hover {
  background-color: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
}
.profile-container {
  display: flex;
  justify-content: center;
  padding: 40px 20px;
  background-color: #f6f7f9;
  min-height: 100vh;
  box-sizing: border-box;
}

.profile-layout {
  display: flex;
  width: 1000px;
  max-width: 100%;
  gap: 20px;
}

.profile-sidebar {
  width: 250px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  height: fit-content;
}

.profile-content {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 30px;
  min-height: 600px;
}

.xy-card {
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.user-summary {
  padding: 30px 20px;
  text-align: center;
  border-bottom: 1px solid #f5f5f5;
  background: linear-gradient(to bottom, #fffdf0, #fff);
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  cursor: pointer;
  margin-bottom: 10px;
}

.large-avatar-img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #ffda44;
}

.large-avatar-icon {
  font-size: 80px;
  color: #ddd;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-uploader:hover .avatar-mask {
  opacity: 1;
}

.credit-score {
  font-size: 13px;
  color: #666;
  margin-top: 5px;
  background: #f5f5f5;
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
}

.score-val {
  color: #00c853;
  font-weight: bold;
}

.profile-menu {
  border-right: none;
}

.profile-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 5px 10px;
  border-radius: 8px;
}

.profile-menu .el-menu-item.is-active {
  background-color: #fff8c7;
  color: #333;
  font-weight: bold;
}

.content-header {
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.content-header h2 {
  font-size: 20px;
  color: #333;
  position: relative;
  padding-left: 12px;
}

.content-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background: #ffda44;
  border-radius: 2px;
}

.profile-form {
  max-width: 500px;
}

.save-btn {
  background-color: #ffda44;
  border-color: #ffda44;
  color: #333;
  font-weight: bold;
  padding-left: 30px;
  padding-right: 30px;
}

.save-btn:hover {
  background-color: #ffcd00;
  border-color: #ffcd00;
}

.mb-20 {
  margin-bottom: 20px;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.list-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.list-item:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border-color: #ffda44;
}

.item-cover {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  background: #f8f8f8;
}

.item-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.item-price {
  color: #ff5000;
  font-size: 18px;
  font-weight: bold;
}

.item-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.view-count {
  font-size: 12px;
  color: #999;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }

  .profile-layout {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .profile-menu {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
  }
  
  .profile-menu .el-menu-item {
    width: 45%;
    margin: 5px 2%;
    text-align: center;
    padding: 0 !important;
  }
  
  .list-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .item-cover {
    width: 100%;
    height: 200px;
    margin-bottom: 10px;
  }
  
  .item-details {
    width: 100%;
    margin-bottom: 10px;
  }
  
  .item-actions {
    width: 100%;
    flex-direction: row;
    justify-content: flex-end;
    margin-left: 0;
    margin-top: 10px;
  }
}
</style>
