<template>
  <div class="report-manage">
    <!-- 搜索栏 -->
    <div class="filter-container">
      <el-select v-model="listQuery.status" placeholder="状态" clearable style="width: 120px" class="filter-item">
        <el-option label="待处理" :value="0" />
        <el-option label="已处理" :value="1" />
        <el-option label="已驳回" :value="2" />
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
      <el-table-column label="ID" prop="reportId" align="center" width="80">
      </el-table-column>
      <el-table-column label="举报类型" align="center" width="100">
        <template #default="{ row }">
          <el-tag>{{ getReportType(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="举报对象ID" prop="targetId" align="center" width="100">
      </el-table-column>
      <el-table-column label="举报原因" prop="reason" align="center" show-overflow-tooltip>
      </el-table-column>
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理结果" prop="result" align="center" show-overflow-tooltip>
      </el-table-column>
      <el-table-column label="提交时间" align="center" width="180">
        <template #default="{ row }">
          <span>{{ formatTime(row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleViewDetail(row)">
            详情
          </el-button>
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleProcess(row)">
            处理
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
    <el-dialog v-model="detailVisible" title="举报详情" width="800px">
      <div v-if="currentReport" class="report-detail-dialog">
        <!-- 举报信息 -->
        <div class="section-title">举报信息</div>
        <div class="detail-row">
          <label>举报类型：</label>
          <span>{{ getReportType(currentReport.type) }}</span>
        </div>
        <div class="detail-row">
          <label>举报原因：</label>
          <span>{{ currentReport.reason }}</span>
        </div>
        <div class="detail-row">
          <label>提交时间：</label>
          <span>{{ formatTime(currentReport.createdAt) }}</span>
        </div>

        <!-- 违规物品详情 -->
        <div v-if="reportItem" class="mt-20">
          <div class="section-title">违规物品详情</div>
          <div class="detail-row">
            <label>物品标题：</label>
            <span>{{ reportItem.title }}</span>
          </div>
          <div class="detail-row">
            <label>价格：</label>
            <span class="price">¥{{ reportItem.price }}</span>
          </div>
          <div class="detail-row">
            <label>描述：</label>
            <p class="description">{{ reportItem.description }}</p>
          </div>
          <div class="detail-row" v-if="reportItem.images">
            <label>图片：</label>
            <div class="image-list">
              <el-image 
                v-for="(img, index) in parseImages(reportItem.images)" 
                :key="index"
                :src="img"
                :preview-src-list="parseImages(reportItem.images)"
                fit="cover"
                class="detail-image"
              />
            </div>
          </div>
        </div>

        <!-- 卖家信息 -->
        <div v-if="reportItem && sellerInfo" class="mt-20">
          <div class="section-title">卖家信息</div>
          <div class="detail-row">
            <label>用户名：</label>
            <span>{{ sellerInfo.username }}</span>
          </div>
          <div class="detail-row">
            <label>昵称：</label>
            <span>{{ sellerInfo.nickname }}</span>
          </div>
          <div class="detail-row">
            <label>信用分：</label>
            <span class="score">{{ sellerInfo.creditScore }}</span>
          </div>
          
          <!-- 卖家在售物品 -->
          <div class="mt-10">
            <div class="sub-title">卖家其他在售物品</div>
            <div v-if="sellerItems.length > 0" class="mini-item-list">
              <div v-for="item in sellerItems" :key="item.itemId" class="mini-item">
                <el-image :src="getCoverImage(item.images)" class="mini-cover" fit="cover" />
                <div class="mini-title" :title="item.title">{{ item.title }}</div>
                <div class="mini-price">¥{{ item.price }}</div>
              </div>
            </div>
            <div v-else class="text-gray">暂无其他在售物品</div>
          </div>

          <!-- 卖家收到的评价 -->
          <div class="mt-10">
            <div class="sub-title">卖家收到的评价</div>
            <div v-if="sellerComments.length > 0" class="comment-list">
              <div v-for="comment in sellerComments" :key="comment.commentId" class="comment-item">
                <div class="comment-header">
                  <el-rate v-model="comment.rating" disabled size="small" />
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
              </div>
            </div>
            <div v-else class="text-gray">暂无评价</div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 处理弹窗 -->
    <el-dialog v-model="dialogVisible" title="处理投诉" width="500px">
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="processForm.status">
            <el-radio :label="1">有效投诉（处理）</el-radio>
            <el-radio :label="2">无效投诉（驳回）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input 
            v-model="processForm.result" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入处理结果反馈给用户..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProcess" :loading="processLoading">确认处理</el-button>
      </template>
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
  status: undefined
})

// 详情相关
const detailVisible = ref(false)
const currentReport = ref(null)
const reportItem = ref(null)
const sellerInfo = ref(null)
const sellerItems = ref([])
const sellerComments = ref([])

// 处理相关
const dialogVisible = ref(false)
const processLoading = ref(false)
const processForm = reactive({
  reportId: null,
  status: 1,
  result: ''
})

const getList = async () => {
  listLoading.value = true
  try {
    const res = await request.get('/report/admin/list', { params: listQuery })
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

const handleViewDetail = async (row) => {
  try {
    const res = await request.get(`/report/admin/detail/${row.reportId}`)
    if (res.code === 200) {
      currentReport.value = res.data.report
      reportItem.value = res.data.item || null
      detailVisible.value = true
      
      // 如果有物品信息，获取卖家信息
      if (reportItem.value) {
        fetchSellerInfo(reportItem.value.userId)
      }
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchSellerInfo = async (userId) => {
  try {
    // 获取卖家基本信息
    const userRes = await request.get(`/user/admin/${userId}`)
    if (userRes.code === 200) {
      sellerInfo.value = userRes.data
    }
    
    // 获取卖家在售物品
    const itemsRes = await request.get(`/item/user/${userId}/selling`)
    if (itemsRes.code === 200) {
      sellerItems.value = itemsRes.data
    }
    
    // 获取卖家收到的评价
    const commentsRes = await request.get(`/comment/user/${userId}`)
    if (commentsRes.code === 200) {
      sellerComments.value = commentsRes.data
    }
  } catch (e) {
    console.error(e)
  }
}

const parseImages = (json) => {
  try {
    return JSON.parse(json) || []
  } catch (e) {
    return []
  }
}

const getCoverImage = (imagesJson) => {
  const images = parseImages(imagesJson)
  return images.length > 0 ? images[0] : ''
}

const handleProcess = (row) => {
  processForm.reportId = row.reportId
  processForm.status = 1
  processForm.result = ''
  dialogVisible.value = true
}

const submitProcess = async () => {
  if (!processForm.result.trim()) {
    ElMessage.warning('请输入处理结果')
    return
  }
  
  processLoading.value = true
  try {
    const res = await request.post('/report/admin/handle', processForm)
    if (res.code === 200) {
      ElMessage.success('处理成功')
      dialogVisible.value = false
      getList() // 刷新列表
    } else {
      ElMessage.error(res.msg || '处理失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    processLoading.value = false
  }
}

const getReportType = (type) => {
  const map = { 1: '违规物品', 2: '交易纠纷' }
  return map[type] || '未知'
}

const getStatusText = (status) => {
  const map = { 0: '待处理', 1: '已处理', 2: '已驳回' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
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

.report-detail-dialog {
  padding: 10px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}

.sub-title {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #606266;
}

.detail-row {
  margin-bottom: 12px;
  display: flex;
}

.detail-row label {
  width: 90px;
  font-weight: bold;
  color: #606266;
  flex-shrink: 0;
}

.mt-20 {
  margin-top: 20px;
}

.mt-10 {
  margin-top: 10px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.score {
  color: #67c23a;
  font-weight: bold;
}

.description {
  margin: 0;
  color: #303133;
  line-height: 1.6;
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

.mini-item-list {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.mini-item {
  width: 100px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 5px;
}

.mini-cover {
  width: 100%;
  height: 80px;
  border-radius: 2px;
}

.mini-title {
  font-size: 12px;
  margin-top: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-price {
  color: #f56c6c;
  font-size: 12px;
  font-weight: bold;
}

.comment-list {
  max-height: 200px;
  overflow-y: auto;
}

.comment-item {
  border-bottom: 1px solid #ebeef5;
  padding: 10px 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-content {
  font-size: 13px;
  color: #303133;
}

.text-gray {
  color: #909399;
  font-size: 13px;
}
</style>
