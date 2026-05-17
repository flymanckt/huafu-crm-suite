<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <div class="header-actions">
            <el-button :icon="Setting" circle title="列配置" @click="columnConfig.openDrawer()" />
            <el-button type="primary" @click="openCreate">新建配置</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeGroup" type="card" @tab-change="loadByGroup">
        <el-tab-pane v-for="g in groups" :key="g" :label="groupLabel(g)" :name="g" />
      </el-tabs>

      <ConfigurableFilterForm
        v-model="query"
        :page-code="pageCode"
        :default-filters="filterFields"
        @search="loadByGroup"
        @reset="resetQuery"
      />

      <div v-if="activeGroup === 'AI'" class="ai-quick-config">
        <el-form :model="aiForm" label-width="100px">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="供应商">
                <el-select v-model="aiForm.provider" filterable style="width:100%" @change="handleAiProviderChange">
                  <el-option v-for="item in aiProviders" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="模型">
                <el-select v-model="aiForm.model" filterable allow-create style="width:100%">
                  <el-option v-for="model in currentAiProvider.models" :key="model" :label="model" :value="model" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="API Key">
                <el-input v-model="aiForm.apiKey" type="password" show-password placeholder="填入对应平台密钥" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-form-item label="协议">
                <DictSelect v-model="aiForm.protocol" dict-code="ai_protocol" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="18">
              <el-form-item label="接口地址">
                <el-input v-model="aiForm.baseUrl" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="6">
              <el-form-item label=" ">
                <el-button type="primary" :loading="savingAiConfig" @click="saveAiQuickConfig">保存AI配置</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <BatchUpdateBar resource="system-config" :fields="batchFields" :selected-rows="selectedRows" @clear="clearBatchSelection" @done="loadByGroup" />
      <el-table ref="tableRef" :data="filteredTableData" v-loading="loading" border stripe class="data-table" max-height="calc(100vh - 390px)" style="margin-top:12px" row-key="id" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" fixed />
        <el-table-column v-if="columnVisible('configName')" prop="configName" label="配置名称" min-width="180" />
        <el-table-column v-if="columnVisible('configKey')" prop="configKey" label="配置键" width="280" show-overflow-tooltip />
        <el-table-column v-if="columnVisible('configValue')" label="配置值" min-width="220">
          <template #default="{ row }">
            <template v-if="row.type === 'BOOLEAN'">
              <el-switch v-model="row._displayValue" @change="v => saveInline(row, v)" :disabled="!row.editable" />
            </template>
            <template v-else-if="row.type === 'INT' || row.type === 'FLOAT'">
              <el-input-number v-model="row._displayValue" :precision="row.type === 'FLOAT' ? 2 : 0" controls-position="right" style="width:160px" :disabled="!row.editable" @change="v => saveInline(row, v)" />
            </template>
            <template v-else>
              <el-input
                v-model="row._displayValue"
                style="width:100%"
                :type="isSecretConfig(row) ? 'password' : 'text'"
                :show-password="isSecretConfig(row)"
                :disabled="!row.editable"
                @blur="saveInline(row, row._displayValue)"
              >
                <template #append v-if="row.editable">
                  <el-button @click="saveInline(row, row._displayValue)">保存</el-button>
                </template>
              </el-input>
            </template>
          </template>
        </el-table-column>
        <el-table-column v-if="columnVisible('type')" prop="type" label="类型" width="80">
          <template #default="{ row }"><el-tag size="small">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column v-if="columnVisible('description')" prop="description" label="说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)" :disabled="!row.editable">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)" :disabled="!row.editable">
              <template #reference><el-button link type="danger" :disabled="!row.editable">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑配置' : '新建配置'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" :disabled="isEdit" placeholder="唯一标识，如 ai.api_key" />
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="展示名称" />
        </el-form-item>
        <el-form-item label="配置分组" prop="configGroup">
          <el-select v-model="form.configGroup" style="width:100%">
            <el-option v-for="g in groups" :key="g" :label="groupLabel(g)" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <DictSelect v-model="form.type" dict-code="config_type" style="width:100%" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="3" placeholder="配置值" />
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="配置说明" />
        </el-form-item>
        <el-form-item label="可编辑" prop="editable">
          <el-switch v-model="form.editable" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
    <ColumnConfigDrawer :page-code="pageCode" :default-columns="defaultColumns" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { getConfigsByGroup, getConfigGroups, createConfig, updateConfig, deleteConfig } from '@/api/system/config'
import BatchUpdateBar from '@/components/common/BatchUpdateBar.vue'
import DictSelect from '@/components/Dict/DictSelect.vue'
import ColumnConfigDrawer from '@/components/ColumnConfig/ColumnConfigDrawer.vue'
import ConfigurableFilterForm from '@/components/FilterConfig/ConfigurableFilterForm.vue'
import { useColumnConfig } from '@/composables/useColumnConfig'
import { Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeGroup = ref('AMAP')
const groups = ref(['AMAP', 'AI', 'API', 'SYSTEM'])
const tableData = ref([])
const loading = ref(false)
const tableRef = ref()
const selectedRows = ref([])
const pageCode = 'system-config-list'
const initialQuery = () => ({ configName: '', configKey: '', type: '', editable: null })
const query = ref(initialQuery())
const filterFields = [
  { key: 'configName', label: '配置名称', placement: 'default' },
  { key: 'configKey', label: '配置键', placement: 'default', width: 220 },
  { key: 'type', label: '类型', type: 'dict', dictCode: 'config_type', placement: 'more' },
  { key: 'editable', label: '可编辑', type: 'dict', dictCode: 'yes_no', valueType: 'number', placement: 'more' }
]
const defaultColumns = [
  { key: 'configName', label: '配置名称', width: 180, visible: true, fixed: true },
  { key: 'configKey', label: '配置键', width: 280, visible: true },
  { key: 'configValue', label: '配置值', width: 220, visible: true },
  { key: 'type', label: '类型', width: 80, visible: true },
  { key: 'description', label: '说明', width: 200, visible: true }
]
const columnConfig = useColumnConfig({ pageCode, defaultColumns })
const columnVisible = key => columnConfig.columns.value.find(c => c.key === key)?.visible !== false
const filteredTableData = computed(() => tableData.value.filter(row => {
  const q = query.value
  if (q.configName && !String(row.configName || '').includes(q.configName)) return false
  if (q.configKey && !String(row.configKey || '').includes(q.configKey)) return false
  if (q.type && row.type !== q.type) return false
  if (q.editable !== null && q.editable !== '' && Number(Boolean(row.editable)) !== Number(q.editable)) return false
  return true
}))
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const savingAiConfig = ref(false)
const batchFields = [
  { key: 'configValue', label: '配置值' },
  { key: 'configGroup', label: '配置分组' },
  { key: 'type', label: '类型', dictCode: 'config_type' },
  { key: 'description', label: '说明' }
]

const aiProviders = [
  { value: 'OPENAI', label: 'OpenAI', protocol: 'OPENAI_CHAT', baseUrl: 'https://api.openai.com/v1', models: ['gpt-4.1', 'gpt-4o', 'gpt-4o-mini'] },
  { value: 'ANTHROPIC', label: 'Anthropic Claude', protocol: 'ANTHROPIC_MESSAGES', baseUrl: 'https://api.anthropic.com/v1', models: ['claude-sonnet-4-5', 'claude-opus-4-1', 'claude-haiku-4-5'] },
  { value: 'DEEPSEEK', label: 'DeepSeek', protocol: 'OPENAI_CHAT', baseUrl: 'https://api.deepseek.com', models: ['deepseek-chat', 'deepseek-reasoner'] },
  { value: 'QWEN', label: '通义千问 DashScope', protocol: 'OPENAI_CHAT', baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1', models: ['qwen-plus', 'qwen-turbo', 'qwen-max'] },
  { value: 'MINIMAX', label: 'MiniMax', protocol: 'OPENAI_CHAT', baseUrl: 'https://api.minimaxi.com/v1', models: ['MiniMax-M2.7', 'MiniMax-M2.7-highspeed', 'MiniMax-M2.5'] },
  { value: 'KIMI', label: 'Moonshot Kimi', protocol: 'OPENAI_CHAT', baseUrl: 'https://api.moonshot.cn/v1', models: ['kimi-k2-0711-preview', 'moonshot-v1-32k', 'moonshot-v1-128k'] },
  { value: 'ZHIPU', label: '智谱 GLM', protocol: 'OPENAI_CHAT', baseUrl: 'https://open.bigmodel.cn/api/paas/v4', models: ['glm-4.5', 'glm-4.5-air', 'glm-4-plus'] },
  { value: 'GEMINI', label: 'Google Gemini', protocol: 'OPENAI_CHAT', baseUrl: 'https://generativelanguage.googleapis.com/v1beta/openai', models: ['gemini-2.5-pro', 'gemini-2.5-flash'] },
  { value: 'DOUBAO', label: '火山方舟 Doubao', protocol: 'OPENAI_CHAT', baseUrl: 'https://ark.cn-beijing.volces.com/api/v3', models: ['doubao-seed-1-6', 'doubao-1-5-pro-32k'] },
  { value: 'OPENROUTER', label: 'OpenRouter', protocol: 'OPENAI_CHAT', baseUrl: 'https://openrouter.ai/api/v1', models: ['openai/gpt-4.1', 'anthropic/claude-sonnet-4.5', 'google/gemini-2.5-pro'] },
  { value: 'CUSTOM', label: '自定义 OpenAI兼容', protocol: 'OPENAI_CHAT', baseUrl: '', models: [] }
]

const aiForm = ref({
  provider: 'OPENAI',
  protocol: 'OPENAI_CHAT',
  baseUrl: 'https://api.openai.com/v1',
  model: 'gpt-4.1',
  apiKey: ''
})

const defaultForm = () => ({
  configKey: '',
  configValue: '',
  configName: '',
  configGroup: 'AMAP',
  description: '',
  type: 'STRING',
  editable: true,
  visible: true
})

const form = ref(defaultForm())

const rules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configGroup: [{ required: true, message: '请选择分组', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const groupLabel = (g) => ({ AMAP: '高德地图', AI: 'AI服务', API: '接口配置', SYSTEM: '系统配置' }[g] || g)

const currentAiProvider = computed(() => aiProviders.find(item => item.value === aiForm.value.provider) || aiProviders[0])

const isSecretConfig = (row) => {
  const key = String(row?.configKey || '').toLowerCase()
  return key.includes('key') || key.includes('secret') || key.includes('token') || key.includes('password')
}

async function loadGroups() {
  try {
    const data = await getConfigGroups()
    const merged = new Set(['AMAP', 'AI', 'API', 'SYSTEM', ...(data || [])])
    groups.value = Array.from(merged)
  } catch (e) {
    groups.value = ['AMAP', 'AI', 'API', 'SYSTEM']
  }
}

function parseValue(row, val) {
  if (val == null) return ''
  if (row.type === 'BOOLEAN') return val === true || val === 'true' || val === 1 || val === '1'
  if (row.type === 'INT') return val === '' || val == null ? null : Number(val)
  if (row.type === 'FLOAT') return val === '' || val == null ? null : parseFloat(val)
  return String(val)
}

async function loadByGroup() {
  loading.value = true
  try {
    const data = await getConfigsByGroup(activeGroup.value)
    tableData.value = (data || []).map(row => ({
      ...row,
      _displayValue: parseValue(row, row.configValue)
    }))
    if (activeGroup.value === 'AI') syncAiForm()
  } catch (e) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.value = initialQuery()
  loadByGroup()
}

function configValue(key) {
  return tableData.value.find(item => item.configKey === key)?.configValue || ''
}

function syncAiForm() {
  const baseUrl = configValue('ai.base_url')
  const provider = inferAiProvider(configValue('ai.provider') || 'OPENAI', baseUrl)
  const preset = aiProviders.find(item => item.value === provider) || aiProviders[0]
  aiForm.value = {
    provider,
    protocol: configValue('ai.protocol') || preset.protocol,
    baseUrl: baseUrl || preset.baseUrl,
    model: configValue('ai.model') || preset.models[0] || '',
    apiKey: configValue('ai.api_key')
  }
}

function inferAiProvider(provider, baseUrl) {
  const value = String(baseUrl || '').toLowerCase()
  if (value.includes('minimax')) return 'MINIMAX'
  if (value.includes('deepseek')) return 'DEEPSEEK'
  if (value.includes('dashscope')) return 'QWEN'
  if (value.includes('moonshot')) return 'KIMI'
  if (value.includes('bigmodel')) return 'ZHIPU'
  if (value.includes('generativelanguage')) return 'GEMINI'
  if (value.includes('volces') || value.includes('ark.cn')) return 'DOUBAO'
  if (value.includes('anthropic')) return 'ANTHROPIC'
  if (value.includes('openrouter')) return 'OPENROUTER'
  return provider
}

function handleAiProviderChange(provider) {
  const preset = aiProviders.find(item => item.value === provider) || aiProviders[0]
  aiForm.value.protocol = preset.protocol
  aiForm.value.baseUrl = preset.baseUrl
  aiForm.value.model = preset.models[0] || ''
}

async function saveAiQuickConfig() {
  if (!aiForm.value.apiKey || !aiForm.value.baseUrl || !aiForm.value.model) {
    ElMessage.warning('请补齐 API Key、接口地址和模型')
    return
  }
  savingAiConfig.value = true
  try {
    await Promise.all([
      upsertConfig('ai.provider', aiForm.value.provider, 'AI供应商', 'AI服务供应商预设', false),
      upsertConfig('ai.protocol', aiForm.value.protocol, 'AI调用协议', 'AI服务调用协议', false),
      upsertConfig('ai.base_url', aiForm.value.baseUrl, 'AI服务接口地址', 'AI服务接口 Base URL', false),
      upsertConfig('ai.model', aiForm.value.model, 'AI默认模型', 'AI服务默认模型名称', false),
      upsertConfig('ai.api_key', aiForm.value.apiKey, 'AI服务 API Key', 'AI服务调用密钥', true)
    ])
    ElMessage.success('AI配置已保存')
    await loadByGroup()
  } finally {
    savingAiConfig.value = false
  }
}

async function upsertConfig(key, value, name, description) {
  const existing = tableData.value.find(item => item.configKey === key)
  const payload = {
    id: existing?.id,
    configKey: key,
    configValue: value,
    configName: existing?.configName || name,
    configGroup: 'AI',
    type: 'STRING',
    description: existing?.description || description,
    editable: true,
    visible: true
  }
  if (existing) {
    await updateConfig(payload)
  } else {
    await createConfig(payload)
  }
}

async function saveInline(row, val) {
  try {
    const payload = {
      id: row.id,
      configKey: row.configKey,
      configValue: String(val === true ? 'true' : val === false ? 'false' : (val ?? '')),
      configName: row.configName,
      configGroup: row.configGroup,
      type: row.type,
      description: row.description,
      editable: row.editable,
      visible: row.visible
    }
    await updateConfig(payload)
    ElMessage.success('保存成功')
    loadByGroup()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

function openCreate() {
  isEdit.value = false
  form.value = { ...defaultForm(), configGroup: activeGroup.value }
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  form.value = {
    id: row.id,
    configKey: row.configKey,
    configValue: row.configValue,
    configName: row.configName,
    configGroup: row.configGroup,
    description: row.description,
    type: row.type,
    editable: row.editable,
    visible: row.visible
  }
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateConfig(form.value)
      ElMessage.success('更新成功')
    } else {
      await createConfig(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadByGroup()
  } catch (e) {
    // error already shown by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteConfig(id)
    ElMessage.success('删除成功')
    loadByGroup()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function handleSelectionChange(rows) {
  selectedRows.value = rows.filter(row => row.editable)
}

function clearBatchSelection() {
  selectedRows.value = []
  tableRef.value?.clearSelection?.()
}

onMounted(async () => {
  await loadGroups()
  loadByGroup()
})
</script>

<style scoped>
.card-header, .header-actions { display:flex; justify-content:space-between; align-items:center; }
.header-actions { gap: 8px; }
.ai-quick-config {
  margin-top: 14px;
  padding: 16px 16px 0;
  background: #f8fafc;
  border: 1px solid #e5eaf3;
  border-radius: 6px;
}
</style>
