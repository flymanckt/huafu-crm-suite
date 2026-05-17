<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: card.color }">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="16">
        <el-card>
          <template #header>销售漏斗</template>
          <div class="funnel">
            <div v-for="(stage, idx) in funnelStages" :key="idx" class="funnel-row">
              <span class="funnel-label">{{ stage.name }}</span>
              <div class="funnel-bar" :style="{ width: stage.pct + '%', background: stage.color }">{{ stage.count }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>快捷入口</template>
          <el-space direction="vertical" fill style="width:100%">
            <el-button type="primary" style="width:100%" @click="$router.push('/customer/create')">新建客户</el-button>
            <el-button style="width:100%" @click="$router.push('/opportunity/lead')">录入线索</el-button>
            <el-button style="width:100%" @click="$router.push('/performance/daily-report')">查看日报</el-button>
            <el-button style="width:100%" @click="$router.push('/ai')">AI日报解析</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { User, TrendCharts, ChatDotRound, Aim } from '@element-plus/icons-vue'

const statCards = ref([
  { title: '客户总数', value: 128, icon: User, color: '#409eff' },
  { title: '进行中商机', value: 34, icon: TrendCharts, color: '#67c23a' },
  { title: '今日日报', value: 12, icon: ChatDotRound, color: '#e6a23c' },
  { title: '月度目标达成', value: '78%', icon: Aim, color: '#f56c6c' }
])

const funnelStages = ref([
  { name: '初步接触', count: 42, pct: 100, color: '#409eff' },
  { name: '需求确认', count: 28, pct: 67, color: '#67c23a' },
  { name: '方案报价', count: 18, pct: 43, color: '#e6a23c' },
  { name: '合同谈判', count: 10, pct: 24, color: '#f56c6c' },
  { name: '成交', count: 6, pct: 14, color: '#909399' }
])
</script>

<style scoped>
.stat-card .el-card__body { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; }
.stat-value { font-size: 24px; font-weight: 700; color: #303133; }
.stat-title { font-size: 13px; color: #909399; margin-top: 4px; }
.funnel-row { display: flex; align-items: center; margin-bottom: 12px; }
.funnel-label { width: 80px; text-align: right; margin-right: 12px; font-size: 13px; color: #606266; }
.funnel-bar { height: 28px; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 13px; min-width: 40px; transition: width 0.5s; }
</style>
