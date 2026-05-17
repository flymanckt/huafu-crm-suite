<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>AI日报解析</template>
          <el-input v-model="inputText" type="textarea" :rows="12" placeholder="粘贴日报原文，AI将自动提取商机、商情、丢单并写入CRM..." />
          <el-button type="primary" style="margin-top:12px;width:100%" @click="handleParse" :loading="parsing">
            解析并写入CRM
          </el-button>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>解析结果</template>
          <div v-if="!result && !errorMsg" class="placeholder">解析结果将显示在这里</div>
          <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false" style="margin-bottom:12px" />
          <template v-if="result">
            <el-alert
              v-if="result.写入结果"
              type="success"
              show-icon
              :closable="false"
              style="margin-bottom:12px"
            >
              <template #title>
                已写入CRM：日报 {{ result.写入结果.日报ID || '-' }}，
                商机 {{ countOf(result.写入结果.商机ID列表) }} 条，
                商情 {{ countOf(result.写入结果.商情ID列表) }} 条，
                丢单 {{ countOf(result.写入结果.丢单ID列表) }} 条，
                拜访/计划 {{ countOf(result.写入结果.拜访记录ID列表) }} 条
              </template>
            </el-alert>
            <!-- 商机 -->
            <el-collapse v-model="activeCollapse">
              <el-collapse-item title="商机" name="opp" v-if="result.商机列表 && result.商机列表.length">
                <template #title>商机 ({{ result.商机列表.length }})</template>
                <el-card v-for="(item, idx) in result.商机列表" :key="idx" shadow="never" style="margin-bottom:8px">
                  <el-descriptions :column="2" size="small" border>
                    <el-descriptions-item label="客户">{{ item.客户名称 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="产品">{{ item.产品需求 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="匹配客户">
                      <el-tag size="small" :type="item.客户匹配状态==='已匹配' ? 'success' : 'warning'">{{ item.客户匹配状态 || '未匹配' }}</el-tag>
                      <span class="match-text">{{ item.客户匹配名称 || item.客户编码 || '-' }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="匹配产品">
                      <el-tag size="small" :type="item.产品匹配状态==='已匹配' ? 'success' : 'warning'">{{ item.产品匹配状态 || '未匹配' }}</el-tag>
                      <span class="match-text">{{ item.产品匹配名称 || item.产品编码 || '-' }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="预估金额">{{ item['预估金额(万元)'] ?? '-' }}万</el-descriptions-item>
                    <el-descriptions-item label="意向度">
                      <el-tag :type="item.意向度==='高'?'danger':item.意向度==='中'?'warning':'info'" size="small">{{ item.意向度 }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="商机ID">{{ item.商机ID || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="产品编码">{{ item.产品编码 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="跟进要点" :span="2">{{ item.跟进要点 || '-' }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>
              </el-collapse-item>
              <el-collapse-item name="intel" v-if="result.商情列表 && result.商情列表.length">
                <template #title>商情 ({{ result.商情列表.length }})</template>
                <el-card v-for="(item, idx) in result.商情列表" :key="idx" shadow="never" style="margin-bottom:8px">
                  <el-descriptions :column="2" size="small" border>
                    <el-descriptions-item label="竞品">{{ item.竞品名称 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="竞品价格">{{ item.竞品价格 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="折扣">{{ item.折扣 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="毛利影响">{{ item.我方毛利影响 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="商情ID">{{ item.商情ID || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="匹配产品">{{ item.产品匹配名称 || item.产品编码 || '-' }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>
              </el-collapse-item>
              <el-collapse-item name="lost" v-if="result.丢单记录 && result.丢单记录.length">
                <template #title>丢单 ({{ result.丢单记录.length }})</template>
                <el-card v-for="(item, idx) in result.丢单记录" :key="idx" shadow="never" style="margin-bottom:8px">
                  <el-descriptions :column="2" size="small" border>
                    <el-descriptions-item label="客户">{{ item.客户名称 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="原因">{{ item.丢单原因 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="竞品">{{ item.竞品名称 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="竞品价格">{{ item.竞品价格 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="我方报价">{{ item.我方报价 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="补救建议">{{ item.补救建议 || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="丢单ID">{{ item.丢单ID || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="匹配客户">{{ item.客户匹配名称 || item.客户编码 || '-' }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>
              </el-collapse-item>
            </el-collapse>
            <!-- 拜访与计划 -->
            <el-descriptions :column="1" border size="small" style="margin-top:12px" v-if="result.今日拜访 || result.明日计划">
              <el-descriptions-item label="今日拜访" v-if="result.今日拜访">
                {{ result.今日拜访.客户名称 }} / {{ result.今日拜访.拜访类型 }} / {{ result.今日拜访.拜访成效 }}
                <span class="match-text">记录ID：{{ result.今日拜访.拜访记录ID || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="明日计划" v-if="result.明日计划">
                {{ result.明日计划.计划拜访客户 }} / {{ result.明日计划.计划事项 }}
                <span class="match-text">记录ID：{{ result.明日计划.拜访记录ID || '-' }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const inputText = ref('')
const result = ref(null)
const errorMsg = ref('')
const parsing = ref(false)
const activeCollapse = ref(['opp','intel','lost'])
const countOf = (rows) => Array.isArray(rows) ? rows.length : 0

const handleParse = async () => {
  if (!inputText.value.trim()) {
    ElMessage.warning('请输入日报内容')
    return
  }
  parsing.value = true
  result.value = null
  errorMsg.value = ''
  try {
    const res = await request.post('/ai/parse/daily-report', { text: inputText.value }, { timeout: 120000 })
    result.value = res?.parsedJson || res
    ElMessage.success('解析并写入完成')
  } catch (e) {
    errorMsg.value = e.message || 'AI解析失败'
  } finally { parsing.value = false }
}
</script>

<style scoped>
.placeholder{color:#c0c4cc;text-align:center;padding:60px 0;font-size:14px}
.match-text{margin-left:8px;color:#606266}
</style>
