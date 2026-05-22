<template>
  <div class="category-manage">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">新增分类</el-button>
    </div>
    
    <el-table :data="categories" border style="width: 100%" v-loading="loading">
      <el-table-column prop="categoryId" label="ID" width="80" />
      <el-table-column prop="categoryName" label="分类名称" />
      <el-table-column prop="icon" label="图标">
        <template #default="scope">
          <!-- Try to render icon if it matches element-plus icons or just text -->
          {{ scope.row.icon }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus Icon Name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const form = ref({
  categoryId: null,
  categoryName: '',
  icon: '',
  sortOrder: 0,
  status: 1
})

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await request.get('/category/admin/list')
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (error) {
    // ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增分类'
  form.value = { categoryId: null, categoryName: '', icon: '', sortOrder: 0, status: 1 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该分类吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/category/delete/${row.categoryId}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchCategories()
      }
    } catch (error) {
      // ElMessage.error('删除失败')
    }
  })
}

const submitForm = async () => {
  try {
    let res
    if (form.value.categoryId) {
      res = await request.put('/category/update', form.value)
    } else {
      res = await request.post('/category/add', form.value)
    }
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      fetchCategories()
    }
  } catch (error) {
    // ElMessage.error('保存失败')
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 20px;
}
</style>
