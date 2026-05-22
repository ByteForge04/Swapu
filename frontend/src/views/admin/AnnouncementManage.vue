<template>
  <div class="announcement-manage">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">发布公告</el-button>
    </div>

    <el-table :data="announcements" border style="width: 100%" v-loading="loading">
      <el-table-column prop="announcementId" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
         <template #default="scope">
             {{ formatTime(scope.row.createdAt) }}
         </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="50%">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="form.content" rows="5" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const announcements = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('发布公告')
const form = ref({
  announcementId: null,
  title: '',
  content: '',
  status: 1
})

const formatTime = (time) => {
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const res = await request.get('/announcement/admin/list')
    if (res.code === 200) {
      announcements.value = res.data.records
    }
  } catch (error) {
    // ElMessage.error('加载失败') // Request interceptor handles error
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '发布公告'
  form.value = { announcementId: null, title: '', content: '', status: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑公告'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该公告吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/announcement/delete/${row.announcementId}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchAnnouncements()
      }
    } catch (error) {
      // handled by interceptor
    }
  })
}

const submitForm = async () => {
  try {
    let res
    if (form.value.announcementId) {
      res = await request.put('/announcement/update', form.value)
    } else {
      res = await request.post('/announcement/add', form.value)
    }
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchAnnouncements()
    }
  } catch (error) {
    // handled by interceptor
  }
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
