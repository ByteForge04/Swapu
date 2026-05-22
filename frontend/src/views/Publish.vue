<template>
  <div class="publish-page">
    <div class="publish-container">
      <div class="page-header">
        <h2 class="title">{{ isEdit ? '编辑物品' : '发布闲置' }}</h2>
        <p class="subtitle">{{ isEdit ? '修改信息后需重新审核' : '让闲置游起来' }}</p>
      </div>

      <el-card class="publish-card">
        <el-form 
          ref="publishFormRef"
          :model="form" 
          :rules="rules" 
          label-position="top"
          size="large"
          v-loading="loading"
        >
          <!-- 标题 -->
          <el-form-item label="标题" prop="title">
            <el-input 
              v-model="form.title" 
              placeholder="品牌型号，都是关键信息" 
              maxlength="30"
              show-word-limit
            />
          </el-form-item>

          <!-- 描述 -->
          <el-form-item prop="description">
            <template #label>
              <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                <span>描述</span>
                <el-button 
                  type="primary" 
                  link 
                  @click="handleAIPolish" 
                  :loading="aiLoading"
                >
                  <el-icon style="margin-right: 4px"><MagicStick /></el-icon> AI 一键润色
                </el-button>
              </div>
            </template>
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="6" 
              placeholder="描述一下宝贝的品牌型号、入手渠道、转手原因..." 
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- 图片上传 -->
          <el-form-item label="添加图片" prop="images">
            <el-upload
              :file-list="fileList"
              action="/api/common/upload"
              list-type="picture-card"
              :on-success="handleUploadSuccess"
              :on-remove="handleRemove"
              :limit="9"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持 JPG/PNG 格式，最多上传 9 张</div>
          </el-form-item>

          <el-divider />

          <!-- 价格与分类 -->
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="价格 (元)" prop="price">
                <el-input-number 
                  v-model="form.price" 
                  :precision="2" 
                  :step="1" 
                  :min="0" 
                  class="price-input"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="原价 (元)" prop="originalPrice">
                <el-input-number 
                  v-model="form.originalPrice" 
                  :precision="2" 
                  :step="1" 
                  :min="0"
                  class="price-input" 
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
                  <el-option
                    v-for="cat in categoryList"
                    :key="cat.categoryId"
                    :label="cat.categoryName"
                    :value="cat.categoryId"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="新旧程度" prop="conditionRate">
                <el-select v-model="form.conditionRate" placeholder="选择新旧程度" style="width: 100%">
                  <el-option label="全新" :value="10" />
                  <el-option label="99新" :value="9" />
                  <el-option label="95新" :value="8" />
                  <el-option label="9成新" :value="7" />
                  <el-option label="8成新" :value="6" />
                  <el-option label="7成新及以下" :value="5" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="交易方式" prop="transactionMethod">
            <el-radio-group v-model="form.transactionMethod">
              <el-radio :label="1">自提</el-radio>
              <el-radio :label="2">送货上门</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="所在校区" prop="campusArea">
             <el-input v-model="form.campusArea" placeholder="例如：北校区、南校区、图书馆附近" />
          </el-form-item>

          <div class="form-actions">
            <el-button @click="$router.push('/')">取消</el-button>
            <el-button type="primary" class="publish-btn" @click="submitPublish" :loading="loading">
              {{ isEdit ? '确认修改' : '立即发布' }}
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const publishFormRef = ref(null)
const loading = ref(false)
const aiLoading = ref(false) // AI 润色加载状态
const fileList = ref([])
const categoryList = ref([]) // New: dynamic categories

const isEdit = computed(() => !!route.query.id)

const form = reactive({
  itemId: undefined,
  title: '',
  description: '',
  price: 0,
  originalPrice: 0,
  categoryId: null,
  conditionRate: 9,
  transactionMethod: 1,
  campusArea: '',
  images: [] // 存储图片URL字符串
})

// 用于判断表单是否已修改
const initialFormState = ref('')
const isFormChanged = computed(() => {
  return JSON.stringify(form) !== initialFormState.value
})
// 标记是否已经提交，提交后不需要拦截离开
const isSubmitted = ref(false)

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  images: [{ required: true, message: '请至少上传一张图片', trigger: 'change', type: 'array' }]
}

// 图片上传成功回调
const handleUploadSuccess = (response, uploadFile, uploadFiles) => {
  if (response.code === 200) {
    // 假设后端返回的是文件的相对路径或绝对URL
    form.images.push(response.data)
  } else {
    ElMessage.error(response.msg || '上传失败')
    // 从列表中移除失败的文件
    const index = fileList.value.indexOf(uploadFile)
    if (index !== -1) {
      fileList.value.splice(index, 1)
    }
  }
}

// 图片移除回调
const handleRemove = (uploadFile, uploadFiles) => {
  // 处理移除逻辑
  const url = uploadFile.response ? uploadFile.response.data : uploadFile.url
  const index = form.images.indexOf(url)
  if (index !== -1) {
    form.images.splice(index, 1)
  }
}

// AI 一键润色
const handleAIPolish = async () => {
  if (!form.title.trim() && !form.description.trim()) {
    ElMessage.warning('请先输入商品标题或简单的描述，AI 才能帮你润色哦！')
    return
  }
  
  aiLoading.value = true
  try {
    const res = await request.post('/ai/polish', {
      title: form.title,
      description: form.description
    })
    if (res.code === 200 && res.data) {
      form.description = res.data
      ElMessage.success('文案润色成功！')
    } else {
      ElMessage.error(res.msg || '文案润色失败')
    }
  } catch (error) {
    console.error('AI 润色失败', error)
    ElMessage.error('AI 助手开小差了，请稍后再试')
  } finally {
    aiLoading.value = false
  }
}

// Fetch categories from backend
const fetchCategories = async () => {
  try {
    const res = await request.get('/category/list')
    if (res.code === 200) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const submitPublish = async () => {
  if (!publishFormRef.value) return
  
  await publishFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      if (form.images.length === 0) {
        ElMessage.warning('请至少上传一张图片')
        return
      }

      loading.value = true
      try {
        const payload = { ...form }
        payload.images = JSON.stringify(form.images)
        
        let res
        if (isEdit.value) {
          res = await request.post('/item/update', payload)
        } else {
          res = await request.post('/item/publish', payload)
        }
        
        if (res.code === 200) {
          isSubmitted.value = true // 标记为已提交，不触发离开确认
          ElMessage.success(isEdit.value ? '修改成功，请等待审核' : '发布成功，请等待审核')
          router.push('/profile?tab=publish')
        } else {
          ElMessage.error(res.msg || (isEdit.value ? '修改失败' : '发布失败'))
        }
      } catch (e) {
        console.error(e)
        ElMessage.error(isEdit.value ? '修改出错，请稍后重试' : '发布出错，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 初始化编辑数据
const initEditData = async () => {
  if (!isEdit.value) return
  
  const itemId = route.query.id
  loading.value = true
  try {
    const res = await request.get(`/item/detail/${itemId}`)
    if (res.code === 200) {
      const item = res.data
      form.itemId = item.itemId
      form.title = item.title
      form.description = item.description
      form.price = item.price
      form.originalPrice = item.originalPrice
      form.categoryId = item.categoryId
      form.conditionRate = item.conditionRate
      form.transactionMethod = item.transactionMethod
      form.campusArea = item.campusArea
      
      // 处理图片回显
      try {
        const images = JSON.parse(item.images) || []
        form.images = images
        fileList.value = images.map(url => ({ name: 'image', url: url }))
      } catch (e) {
        form.images = []
      }
      // 数据加载完成后，记录初始状态
      initialFormState.value = JSON.stringify(form)
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('获取物品详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchCategories()
  if (isEdit.value) {
    await initEditData()
  } else {
    // 如果是新建，直接记录初始状态
    initialFormState.value = JSON.stringify(form)
  }
  
  // 监听浏览器刷新/关闭事件
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// 处理浏览器刷新/关闭
const handleBeforeUnload = (e) => {
  if (isFormChanged.value && !isSubmitted.value) {
    e.preventDefault()
    e.returnValue = '' // Chrome requires this
  }
}

// 处理 Vue Router 路由切换
onBeforeRouteLeave(async (to, from) => {
  if (isFormChanged.value && !isSubmitted.value) {
    try {
      await ElMessageBox.confirm(
        '当前页面有未保存的内容，确认要离开吗？',
        '提示',
        {
          confirmButtonText: '确认离开',
          cancelButtonText: '取消',
          type: 'warning',
        }
      )
      return true
    } catch {
      return false // 取消导航
    }
  }
  return true
})
</script>

<style scoped>
.publish-page {
  background-color: #f6f7f9;
  min-height: 100vh;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.publish-container {
  width: 100%;
  max-width: 800px;
}

.page-header {
  margin-bottom: 20px;
  text-align: center;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.subtitle {
  color: #999;
  font-size: 14px;
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

.publish-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.price-input {
  width: 100%;
}

.form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.publish-btn {
  background-color: #ffda44;
  border-color: #ffda44;
  color: #333;
  font-weight: bold;
  padding-left: 30px;
  padding-right: 30px;
}

.publish-btn:hover {
  background-color: #ffcd00;
  border-color: #ffcd00;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
