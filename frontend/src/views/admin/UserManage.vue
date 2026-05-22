<template>
  <div class="user-manage">
    <!-- 搜索栏 -->
    <div class="filter-container">
      <el-input v-model="listQuery.username" placeholder="用户名" style="width: 200px;" class="filter-item" @keyup.enter="handleFilter" />
      <el-button class="filter-item" type="primary" icon="Search" @click="handleFilter">
        搜索
      </el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="ID" prop="userId" align="center" width="80">
      </el-table-column>
      <el-table-column label="用户名" prop="username" align="center">
      </el-table-column>
      <el-table-column label="昵称" prop="nickname" align="center">
      </el-table-column>
      <el-table-column label="角色" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : 'success'">
            {{ row.role === 1 ? '管理员' : '学生' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" align="center" width="180">
        <template #default="{ row }">
          <span>{{ formatTime(row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" size="small" type="danger" @click="handleModifyStatus(row, 0)">
            禁用
          </el-button>
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleModifyStatus(row, 1)">
            启用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-show="total > 0"
      :current-page="listQuery.page"
      :page-size="listQuery.size"
      :total="total"
      layout="total, prev, pager, next, jumper"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { Search } from '@element-plus/icons-vue'

const list = ref([])
const total = ref(0)
const listLoading = ref(true)
const listQuery = reactive({
  page: 1,
  size: 10,
  username: undefined
})

const getList = async () => {
  listLoading.value = true
  try {
    const res = await request.get('/admin/user/list', { params: listQuery })
    if (res.code === 200) {
      list.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    console.error(e)
  } finally {
    listLoading.value = false
  }
}

const handleFilter = () => {
  listQuery.page = 1
  getList()
}

const handleCurrentChange = (val) => {
  listQuery.page = val
  getList()
}

const handleModifyStatus = async (row, status) => {
  try {
    await request.post(`/admin/user/status/${row.userId}/${status}`)
    ElMessage.success('操作成功')
    row.status = status
  } catch (e) {
    console.error(e)
  }
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.filter-container {
  padding-bottom: 20px;
}
.filter-item {
  margin-right: 10px;
}
</style>
