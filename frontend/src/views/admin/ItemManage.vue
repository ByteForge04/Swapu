<template>
  <div class="item-manage">
    <!-- 搜索栏 -->
    <div class="filter-container">
      <el-input v-model="listQuery.title" placeholder="物品标题" style="width: 200px;" class="filter-item" @keyup.enter="handleFilter" />
      <el-select v-model="listQuery.status" placeholder="状态" clearable style="width: 120px" class="filter-item">
        <el-option label="待审核" :value="0" />
        <el-option label="在售" :value="1" />
        <el-option label="交易中" :value="2" />
        <el-option label="已售出" :value="3" />
        <el-option label="已下架" :value="4" />
      </el-select>
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
      <el-table-column label="ID" prop="itemId" align="center" width="80">
      </el-table-column>
      <el-table-column label="标题" prop="title" align="center" show-overflow-tooltip>
      </el-table-column>
      <el-table-column label="价格" prop="price" align="center" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" align="center" width="180">
        <template #default="{ row }">
          <span>{{ formatTime(row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleViewDetail(row)">
            详情
          </el-button>
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleModifyStatus(row, 1)">
            通过
          </el-button>
          <el-button v-if="row.status === 0" size="small" type="danger" @click="handleModifyStatus(row, 4)">
            拒绝
          </el-button>
          <el-button v-if="row.status === 1" size="small" type="danger" @click="handleModifyStatus(row, 4)">
            下架
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="物品详情" width="600px">
      <div v-if="currentItem" class="item-detail-dialog">
        <div class="detail-row">
          <label>标题：</label>
          <span>{{ currentItem.title }}</span>
        </div>
        <div class="detail-row">
          <label>价格：</label>
          <span class="price">¥{{ currentItem.price }}</span>
          <span class="original-price" v-if="currentItem.originalPrice">（原价: ¥{{ currentItem.originalPrice }}）</span>
        </div>
        <div class="detail-row">
          <label>分类：</label>
          <span>{{ getCategoryName(currentItem.categoryId) }}</span>
        </div>
        <div class="detail-row">
          <label>成色：</label>
          <span>{{ getConditionText(currentItem.conditionRate) }}</span>
        </div>
        <div class="detail-row">
          <label>描述：</label>
          <p class="description">{{ currentItem.description }}</p>
        </div>
        <div class="detail-row" v-if="currentItem.images">
          <label>图片：</label>
          <div class="image-list">
            <el-image 
              v-for="(img, index) in parseImages(currentItem.images)" 
              :key="index"
              :src="img"
              :preview-src-list="parseImages(currentItem.images)"
              fit="cover"
              class="detail-image"
            />
          </div>
        </div>
        <div class="detail-row">
          <label>发布时间：</label>
          <span>{{ formatTime(currentItem.createdAt) }}</span>
        </div>
      </div>
    </el-dialog>
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
  title: undefined,
  status: undefined
})
const dialogVisible = ref(false)
const currentItem = ref(null)

const getList = async () => {
  listLoading.value = true
  try {
    const res = await request.get('/admin/item/list', { params: listQuery })
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

const handleViewDetail = (row) => {
  currentItem.value = row
  dialogVisible.value = true
}

const parseImages = (json) => {
  try {
    return JSON.parse(json) || []
  } catch (e) {
    return []
  }
}

const getCategoryName = (id) => {
  const map = { 1: '闲置书籍', 2: '数码产品', 3: '生活用品', 4: '美妆护肤', 5: '运动健身', 6: '其他闲置' }
  return map[id] || '未知'
}

const getConditionText = (rate) => {
  const map = { 10: '全新', 9: '99新', 8: '95新', 7: '9成新', 6: '8成新', 5: '7成新及以下' }
  return map[rate] || '未知'
}

const handleModifyStatus = async (row, status) => {
  try {
    await request.post(`/admin/item/status/${row.itemId}/${status}`)
    ElMessage.success('操作成功')
    row.status = status
  } catch (e) {
    console.error(e)
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

.item-detail-dialog {
  padding: 10px;
}

.detail-row {
  margin-bottom: 15px;
  display: flex;
}

.detail-row label {
  width: 80px;
  font-weight: bold;
  color: #606266;
  flex-shrink: 0;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.original-price {
  color: #909399;
  text-decoration: line-through;
  margin-left: 10px;
  font-size: 13px;
}

.description {
  margin: 0;
  line-height: 1.6;
  color: #303133;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
