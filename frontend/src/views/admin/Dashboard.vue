<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户总数</span>
            </div>
          </template>
          <div class="card-value">{{ data.userCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日日活 (DAU)</span>
            </div>
          </template>
          <div class="card-value">{{ data.dau || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>物品总数</span>
              <el-button type="primary" size="small" @click="handleSyncEs" :loading="syncing">全量同步 ES</el-button>
            </div>
          </template>
          <div class="card-value">{{ data.itemCount || 0 }}</div>
          <div class="sub-text">今日新增: {{ data.newItemsToday || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>总成交额</span>
            </div>
          </template>
          <div class="card-value">¥{{ data.totalAmount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>近7天交易统计</span>
            </div>
          </template>
          <div ref="chartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" style="height: 100%">
          <template #header>
            <div class="card-header">
              <span>热门搜索词 TOP10</span>
            </div>
          </template>
          <el-table :data="data.hotSearch || []" stripe style="width: 100%">
            <el-table-column type="index" width="50" />
            <el-table-column prop="keyword" label="关键词" />
            <el-table-column prop="count" label="搜索次数" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const data = ref({})
const chartRef = ref(null)
const syncing = ref(false)
let myChart = null
let resizeHandler = null

const handleSyncEs = async () => {
  syncing.value = true
  try {
    const res = await request.post('/admin/es/sync')
    if (res.code === 200) {
      ElMessage.success('ES数据全量同步成功！')
    } else {
      ElMessage.error(res.msg || '同步失败')
    }
  } catch (error) {
    ElMessage.error('同步异常')
  } finally {
    syncing.value = false
  }
}

onMounted(async () => {
  try {
    const res = await request.get('/admin/dashboard')
    if (res.code === 200) {
      data.value = res.data
      initChart(res.data.chartData)
    }
  } catch (e) {
    console.error(e)
  }
})

onBeforeUnmount(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
})

const initChart = (chartData) => {
  if (!chartData || !chartRef.value) return
  
  // 按照日期排序
  chartData.sort((a, b) => new Date(a.date) - new Date(b.date))
  
  const dates = chartData.map(item => item.date)
  const counts = chartData.map(item => item.count)
  
  myChart = echarts.init(chartRef.value)
  const option = {
    title: {
      text: '每日新增订单数'
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        data: counts,
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.5)' },
            { offset: 1, color: 'rgba(64,158,255,0.1)' }
          ])
        }
      }
    ]
  }
  myChart.setOption(option)
  
  // 监听窗口大小变化
  resizeHandler = () => {
    if (myChart) {
      myChart.resize()
    }
  }
  window.addEventListener('resize', resizeHandler)
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mt-20 {
  margin-top: 20px;
}
.card-value {
  font-size: 36px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}
.sub-text {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>
