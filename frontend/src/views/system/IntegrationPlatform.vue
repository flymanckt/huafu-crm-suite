<template>
  <div class="page-container integration-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>集成平台</span>
          <div class="header-actions">
            <el-button v-if="activeTab === 'connections'" type="primary" @click="openConnection()">新增连接</el-button>
            <el-button v-if="activeTab === 'sap'" type="primary" @click="openSap()">新增SAP RFC</el-button>
            <el-button v-if="activeTab === 'interfaces'" type="primary" @click="openInterface()">新增接口</el-button>
            <el-button v-if="activeTab === 'logs'" @click="loadLogs">刷新日志</el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="通用连接" name="connections">
          <el-table :data="connections" v-loading="loading" border stripe max-height="calc(100vh - 280px)">
            <el-table-column prop="connectionCode" label="连接编码" min-width="140" />
            <el-table-column prop="connectionName" label="连接名称" min-width="160" />
            <el-table-column prop="connectionType" label="类型" width="110">
              <template #default="{ row }"><el-tag>{{ row.connectionType }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="baseUrl" label="地址/主机" min-width="220" show-overflow-tooltip />
            <el-table-column prop="authType" label="认证" width="100" />
            <el-table-column label="启用" width="80">
              <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openConnection(row)">编辑</el-button>
                <el-button link @click="handleTestConnection(row)">测试</el-button>
                <el-popconfirm title="确定删除该连接？" @confirm="handleDeleteConnection(row.id)">
                  <template #reference><el-button link type="danger">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="SAP RFC" name="sap">
          <el-table :data="sapConfigs" v-loading="loading" border stripe max-height="calc(100vh - 280px)">
            <el-table-column prop="configCode" label="配置编码" min-width="130" />
            <el-table-column prop="configName" label="配置名称" min-width="160" />
            <el-table-column prop="appServerHost" label="应用服务器" min-width="180" />
            <el-table-column prop="systemNumber" label="系统号" width="90" />
            <el-table-column prop="client" label="客户端" width="90" />
            <el-table-column prop="userName" label="用户" width="120" />
            <el-table-column label="启用" width="80">
              <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openSap(row)">编辑</el-button>
                <el-button link @click="handleTestSap(row)">测试</el-button>
                <el-popconfirm title="确定删除该SAP RFC配置？" @confirm="handleDeleteSap(row.id)">
                  <template #reference><el-button link type="danger">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="接口定义" name="interfaces">
          <div class="toolbar">
            <el-input v-model="interfaceQuery.keyword" placeholder="接口编码/名称/函数" clearable style="width:260px" @keyup.enter="loadInterfaces" />
            <el-select v-model="interfaceQuery.systemCode" placeholder="系统" clearable style="width:140px">
              <el-option label="SAP" value="SAP" />
              <el-option label="CRM" value="CRM" />
              <el-option label="外部系统" value="EXTERNAL" />
            </el-select>
            <el-button @click="loadInterfaces">查询</el-button>
          </div>
          <el-table :data="interfaces" v-loading="loading" border stripe max-height="calc(100vh - 330px)" @row-click="selectInterface">
            <el-table-column prop="interfaceCode" label="接口编码" min-width="150" />
            <el-table-column prop="interfaceName" label="接口名称" min-width="170" />
            <el-table-column prop="protocol" label="协议" width="110">
              <template #default="{ row }"><el-tag>{{ row.protocol }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="connectionCode" label="连接" width="130" />
            <el-table-column prop="direction" label="方向" width="120" />
            <el-table-column prop="sapFunctionName" label="SAP函数/对象" min-width="160" show-overflow-tooltip />
            <el-table-column prop="endpointPath" label="接口路径" min-width="180" show-overflow-tooltip />
            <el-table-column label="启用" width="80">
              <template #default="{ row }"><el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag></template>
            </el-table-column>
            <el-table-column label="操作" width="210" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openInterface(row)">编辑</el-button>
                <el-button link @click.stop="selectInterface(row)">字段映射</el-button>
                <el-popconfirm title="确定删除该接口？" @confirm="handleDeleteInterface(row.id)">
                  <template #reference><el-button link type="danger" @click.stop>删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="字段映射" name="mappings">
          <div class="toolbar">
            <el-select v-model="selectedInterfaceId" placeholder="选择接口" filterable style="width:320px" @change="loadMappings">
              <el-option v-for="item in interfaces" :key="item.id" :label="`${item.interfaceCode} - ${item.interfaceName}`" :value="item.id" />
            </el-select>
            <el-button type="primary" :disabled="!selectedInterfaceId" @click="openMapping()">新增映射</el-button>
          </div>
          <el-table :data="mappings" v-loading="loading" border stripe max-height="calc(100vh - 330px)">
            <el-table-column prop="sortOrder" label="序号" width="80" />
            <el-table-column prop="sourceField" label="来源字段" min-width="180" />
            <el-table-column prop="targetField" label="目标字段" min-width="180" />
            <el-table-column prop="fieldType" label="类型" width="110" />
            <el-table-column label="必填" width="80">
              <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
            </el-table-column>
            <el-table-column prop="defaultValue" label="默认值" min-width="140" />
            <el-table-column prop="transformRule" label="转换规则" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openMapping(row)">编辑</el-button>
                <el-popconfirm title="确定删除该字段映射？" @confirm="handleDeleteMapping(row.id)">
                  <template #reference><el-button link type="danger">删除</el-button></template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="平台日志" name="logs">
          <div class="toolbar">
            <el-input v-model="logQuery.keyword" placeholder="业务键/异常/报文" clearable style="width:240px" @keyup.enter="loadLogs" />
            <el-input v-model="logQuery.interfaceCode" placeholder="接口编码" clearable style="width:180px" @keyup.enter="loadLogs" />
            <el-select v-model="logQuery.status" placeholder="状态" clearable style="width:150px">
              <el-option v-for="item in logStatuses" :key="item" :label="item" :value="item" />
            </el-select>
            <el-button @click="loadLogs">查询</el-button>
          </div>
          <el-table :data="logs" v-loading="loading" border stripe max-height="calc(100vh - 370px)">
            <el-table-column prop="createdTime" label="时间" width="170" />
            <el-table-column prop="interfaceCode" label="接口" min-width="140" />
            <el-table-column prop="businessKey" label="业务键" min-width="140" />
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ row.status }}</el-tag></template>
            </el-table-column>
            <el-table-column prop="retryCount" label="重试" width="80" />
            <el-table-column prop="errorMessage" label="异常" min-width="260" show-overflow-tooltip />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="row.status === 'SUCCESS'" @click="handleRepush(row)">重新推送</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pager"
            layout="total, sizes, prev, pager, next"
            :total="logTotal"
            v-model:current-page="logQuery.current"
            v-model:page-size="logQuery.size"
            @current-change="loadLogs"
            @size-change="loadLogs"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="connectionDialog" :title="connectionForm.id ? '编辑连接' : '新增连接'" width="760px">
      <el-form :model="connectionForm" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="连接编码"><el-input v-model="connectionForm.connectionCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="连接名称"><el-input v-model="connectionForm.connectionName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="连接类型"><el-select v-model="connectionForm.connectionType" style="width:100%"><el-option v-for="item in connectionTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="认证方式"><el-select v-model="connectionForm.authType" style="width:100%"><el-option v-for="item in authTypes" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="地址/主机"><el-input v-model="connectionForm.baseUrl" placeholder="https://api.example.com 或 主机地址" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="认证配置"><el-input v-model="connectionForm.authConfig" type="textarea" :rows="3" placeholder='JSON，如 {"token":"***"}' /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="请求头"><el-input v-model="connectionForm.headerConfig" type="textarea" :rows="2" placeholder='JSON，如 {"Content-Type":"application/json"}' /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="超时毫秒"><el-input-number v-model="connectionForm.timeoutMs" :min="1000" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="启用"><el-switch v-model="connectionForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="connectionForm.remark" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="connectionDialog=false">取消</el-button><el-button type="primary" @click="submitConnection">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="sapDialog" :title="sapForm.id ? '编辑SAP RFC' : '新增SAP RFC'" width="760px">
      <el-form :model="sapForm" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="配置编码"><el-input v-model="sapForm.configCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="配置名称"><el-input v-model="sapForm.configName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="应用服务器"><el-input v-model="sapForm.appServerHost" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="系统号"><el-input v-model="sapForm.systemNumber" /></el-form-item></el-col>
          <el-col :span="6"><el-form-item label="客户端"><el-input v-model="sapForm.client" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="用户名"><el-input v-model="sapForm.userName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="密码/密文"><el-input v-model="sapForm.passwordCipher" type="password" show-password /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="语言"><el-input v-model="sapForm.language" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="连接池"><el-input-number v-model="sapForm.poolCapacity" :min="1" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="峰值"><el-input-number v-model="sapForm.peakLimit" :min="1" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="超时毫秒"><el-input-number v-model="sapForm.connectionTimeout" :min="1000" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="启用"><el-switch v-model="sapForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="sapForm.remark" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="sapDialog=false">取消</el-button><el-button type="primary" @click="submitSap">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="interfaceDialog" :title="interfaceForm.id ? '编辑接口' : '新增接口'" width="880px" class="interface-dialog">
      <el-form :model="interfaceForm" label-width="120px">
        <el-alert
          class="interface-alert"
          :title="interfaceMode === 'SAP' ? 'SAP接口填写SAP连接、RFC/BAPI函数或IDoc/OData对象，字段映射维护入参和出参。' : '通用接口填写连接配置、HTTP方法和接口路径，调用地址由连接地址与接口路径组合。'"
          type="info"
          show-icon
          :closable="false"
        />
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="接口类型">
              <el-radio-group :model-value="interfaceMode" @change="handleInterfaceModeChange">
                <el-radio-button label="SAP">SAP接口</el-radio-button>
                <el-radio-button label="GENERIC">通用接口</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="接口编码"><el-input v-model="interfaceForm.interfaceCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="接口名称"><el-input v-model="interfaceForm.interfaceName" /></el-form-item></el-col>
          <el-col :span="8">
            <el-form-item label="系统">
              <el-select v-model="interfaceForm.systemCode" style="width:100%" filterable allow-create default-first-option>
                <el-option label="SAP" value="SAP" />
                <el-option label="CRM" value="CRM" />
                <el-option label="外部系统" value="EXTERNAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="协议">
              <el-select v-model="interfaceForm.protocol" style="width:100%" @change="handleProtocolChange">
                <el-option v-for="item in availableProtocols" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="方向"><el-select v-model="interfaceForm.direction" style="width:100%"><el-option v-for="item in directions" :key="item" :label="item" :value="item" /></el-select></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item :label="connectionLabel">
              <el-select
                v-model="interfaceForm.connectionCode"
                :placeholder="connectionPlaceholder"
                style="width:100%"
                filterable
                allow-create
                default-first-option
              >
                <el-option
                  v-for="item in connectionSelectOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="业务模块"><el-input v-model="interfaceForm.businessModule" placeholder="如 CUSTOMER、ORDER、PRODUCT" /></el-form-item></el-col>

          <template v-if="interfaceMode === 'SAP'">
            <el-col :span="12">
              <el-form-item :label="sapObjectLabel">
                <el-input v-model="interfaceForm.sapFunctionName" :placeholder="sapObjectPlaceholder" />
              </el-form-item>
            </el-col>
            <el-col v-if="interfaceForm.protocol !== 'SAP_RFC'" :span="12">
              <el-form-item :label="interfaceForm.protocol === 'SAP_ODATA' ? 'OData路径' : '扩展对象路径'">
                <el-input v-model="interfaceForm.endpointPath" :placeholder="interfaceForm.protocol === 'SAP_ODATA' ? '/sap/opu/odata/sap/API_BUSINESS_PARTNER/A_BusinessPartner' : '可选，填写扩展对象或接收端路径'" />
              </el-form-item>
            </el-col>
            <el-col v-if="interfaceForm.protocol === 'SAP_ODATA'" :span="6">
              <el-form-item label="HTTP方法">
                <el-select v-model="interfaceForm.httpMethod" style="width:100%">
                  <el-option v-for="item in httpMethods" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col v-if="interfaceForm.protocol === 'SAP_ODATA'" :span="6">
              <el-form-item label="内容类型">
                <el-select v-model="interfaceForm.contentType" style="width:100%" filterable allow-create default-first-option>
                  <el-option v-for="item in contentTypes" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
          </template>

          <template v-else>
            <el-col :span="6">
              <el-form-item label="HTTP方法">
                <el-select v-model="interfaceForm.httpMethod" style="width:100%">
                  <el-option v-for="item in httpMethods" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="内容类型">
                <el-select v-model="interfaceForm.contentType" style="width:100%" filterable allow-create default-first-option>
                  <el-option v-for="item in contentTypes" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="接口路径">
                <el-input v-model="interfaceForm.endpointPath" placeholder="/api/v1/customer/sync" />
              </el-form-item>
            </el-col>
          </template>

          <el-col :span="12"><el-form-item label="重试次数"><el-input-number v-model="interfaceForm.retryLimit" :min="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="启用"><el-switch v-model="interfaceForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="说明"><el-input v-model="interfaceForm.description" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="interfaceDialog=false">取消</el-button><el-button type="primary" @click="submitInterface">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="mappingDialog" :title="mappingForm.id ? '编辑字段映射' : '新增字段映射'" width="700px">
      <el-form :model="mappingForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="来源字段"><el-input v-model="mappingForm.sourceField" placeholder="CRM字段或入参字段" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="目标字段"><el-input v-model="mappingForm.targetField" placeholder="外部接口字段或SAP参数名" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="字段类型"><el-input v-model="mappingForm.fieldType" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="序号"><el-input-number v-model="mappingForm.sortOrder" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="必填"><el-switch v-model="mappingForm.required" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="默认值"><el-input v-model="mappingForm.defaultValue" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="转换规则"><el-input v-model="mappingForm.transformRule" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="mappingForm.remark" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="mappingDialog=false">取消</el-button><el-button type="primary" @click="submitMapping">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  authTypes,
  connectionTypes,
  createConnection,
  createInterface,
  createMapping,
  createSapRfcConfig,
  deleteConnection,
  deleteInterface,
  deleteMapping,
  deleteSapRfcConfig,
  directions,
  listConnections,
  listInterfaces,
  listMappings,
  listSapRfcConfigs,
  logStatuses,
  pageLogs,
  repushLog,
  testConnection,
  testSapRfcConfig,
  updateConnection,
  updateInterface,
  updateMapping,
  updateSapRfcConfig
} from '@/api/integration'

const activeTab = ref('connections')
const loading = ref(false)
const connections = ref([])
const sapConfigs = ref([])
const interfaces = ref([])
const mappings = ref([])
const logs = ref([])
const logTotal = ref(0)
const selectedInterfaceId = ref(null)
const interfaceQuery = ref({ keyword: '', systemCode: '' })
const logQuery = ref({ current: 1, size: 20, interfaceCode: '', status: '', keyword: '' })

const connectionDialog = ref(false)
const sapDialog = ref(false)
const interfaceDialog = ref(false)
const mappingDialog = ref(false)
const defaultConnection = () => ({ connectionCode: '', connectionName: '', connectionType: 'REST', enabled: 1, baseUrl: '', authType: 'NONE', authConfig: '', headerConfig: '', timeoutMs: 30000, remark: '' })
const defaultSap = () => ({ configCode: 'SAP_PRD', configName: 'SAP生产环境', enabled: 1, appServerHost: '', systemNumber: '00', client: '800', userName: '', passwordCipher: '', language: 'ZH', poolCapacity: 5, peakLimit: 10, connectionTimeout: 30000, remark: '' })
const defaultInterface = () => ({ interfaceCode: '', interfaceName: '', systemCode: 'SAP', connectionCode: '', protocol: 'SAP_RFC', direction: 'OUTBOUND', businessModule: '', sapFunctionName: '', httpMethod: 'POST', endpointPath: '', contentType: 'application/json', enabled: 1, retryLimit: 3, description: '' })
const defaultMapping = () => ({ interfaceId: null, sourceField: '', targetField: '', fieldType: 'STRING', required: 0, defaultValue: '', transformRule: '', sortOrder: 0, remark: '' })
const connectionForm = ref(defaultConnection())
const sapForm = ref(defaultSap())
const interfaceForm = ref(defaultInterface())
const mappingForm = ref(defaultMapping())

const sapProtocolOptions = [
  { label: 'SAP RFC / BAPI', value: 'SAP_RFC' },
  { label: 'SAP OData', value: 'SAP_ODATA' },
  { label: 'SAP IDoc', value: 'SAP_IDOC' }
]
const genericProtocolOptions = [
  { label: 'REST', value: 'REST' },
  { label: 'SOAP', value: 'SOAP' },
  { label: 'Webhook', value: 'WEBHOOK' },
  { label: 'SFTP', value: 'SFTP' },
  { label: 'FTP', value: 'FTP' },
  { label: 'Database', value: 'DATABASE' },
  { label: 'Kafka', value: 'KAFKA' },
  { label: 'RabbitMQ', value: 'RABBITMQ' },
  { label: '自定义', value: 'CUSTOM' }
]
const sapProtocols = sapProtocolOptions.map(item => item.value)
const httpMethods = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE']
const contentTypes = ['application/json', 'application/xml', 'text/xml', 'application/x-www-form-urlencoded', 'multipart/form-data', 'text/plain']

const interfaceMode = computed(() => sapProtocols.includes(interfaceForm.value.protocol) ? 'SAP' : 'GENERIC')
const availableProtocols = computed(() => interfaceMode.value === 'SAP' ? sapProtocolOptions : genericProtocolOptions)
const isSapRfcInterface = computed(() => interfaceForm.value.protocol === 'SAP_RFC')
const connectionLabel = computed(() => {
  if (isSapRfcInterface.value) return 'SAP RFC配置'
  return interfaceMode.value === 'SAP' ? 'SAP连接配置' : '连接配置'
})
const connectionPlaceholder = computed(() => {
  if (isSapRfcInterface.value) return '选择SAP RFC连接配置'
  return interfaceMode.value === 'SAP' ? '选择SAP连接配置' : '选择通用连接配置'
})
const sapObjectLabel = computed(() => {
  if (interfaceForm.value.protocol === 'SAP_IDOC') return 'IDoc/消息类型'
  if (interfaceForm.value.protocol === 'SAP_ODATA') return 'OData对象'
  return 'RFC/BAPI函数'
})
const sapObjectPlaceholder = computed(() => {
  if (interfaceForm.value.protocol === 'SAP_IDOC') return '如 DEBMAS / ORDERS'
  if (interfaceForm.value.protocol === 'SAP_ODATA') return '如 API_BUSINESS_PARTNER'
  return '如 ZCRM_CUSTOMER_SYNC / BAPI_CUSTOMER_CREATEFROMDATA1'
})
const connectionSelectOptions = computed(() => {
  if (isSapRfcInterface.value) {
    return sapConfigs.value.map(item => ({
      label: `${item.configCode} - ${item.configName}`,
      value: item.configCode
    }))
  }
  const matched = connections.value.filter(item => !interfaceForm.value.protocol || item.connectionType === interfaceForm.value.protocol)
  const source = matched.length ? matched : connections.value
  return source.map(item => ({
    label: `${item.connectionCode} - ${item.connectionName} (${item.connectionType})`,
    value: item.connectionCode
  }))
})

async function loadConnections() {
  loading.value = true
  try { connections.value = await listConnections() } finally { loading.value = false }
}

async function loadSap() {
  loading.value = true
  try { sapConfigs.value = await listSapRfcConfigs() } finally { loading.value = false }
}

async function loadInterfaces() {
  loading.value = true
  try { interfaces.value = await listInterfaces(interfaceQuery.value) } finally { loading.value = false }
}

async function loadMappings() {
  if (!selectedInterfaceId.value) {
    mappings.value = []
    return
  }
  loading.value = true
  try { mappings.value = await listMappings(selectedInterfaceId.value) } finally { loading.value = false }
}

async function loadLogs() {
  loading.value = true
  try {
    const data = await pageLogs(logQuery.value)
    logs.value = data?.records || []
    logTotal.value = Number(data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleTabChange(tab) {
  if (tab === 'connections') loadConnections()
  if (tab === 'sap') loadSap()
  if (tab === 'interfaces') loadInterfaces()
  if (tab === 'mappings') loadInterfaces()
  if (tab === 'logs') loadLogs()
}

function openConnection(row) {
  connectionForm.value = row ? { ...row } : defaultConnection()
  connectionDialog.value = true
}

async function submitConnection() {
  if (connectionForm.value.id) await updateConnection(connectionForm.value.id, connectionForm.value)
  else await createConnection(connectionForm.value)
  ElMessage.success('连接配置已保存')
  connectionDialog.value = false
  loadConnections()
}

async function handleDeleteConnection(id) {
  await deleteConnection(id)
  ElMessage.success('连接配置已删除')
  loadConnections()
}

async function handleTestConnection(row) {
  const message = await testConnection(row.id)
  ElMessage.success(message)
}

function openSap(row) {
  sapForm.value = row ? { ...row } : defaultSap()
  sapDialog.value = true
}

async function submitSap() {
  if (sapForm.value.id) await updateSapRfcConfig(sapForm.value.id, sapForm.value)
  else await createSapRfcConfig(sapForm.value)
  ElMessage.success('SAP RFC配置已保存')
  sapDialog.value = false
  loadSap()
}

async function handleDeleteSap(id) {
  await deleteSapRfcConfig(id)
  ElMessage.success('SAP RFC配置已删除')
  loadSap()
}

async function handleTestSap(row) {
  const message = await testSapRfcConfig(row.id)
  ElMessage.success(message)
}

function openInterface(row) {
  interfaceForm.value = row ? { ...row } : defaultInterface()
  applyProtocolDefaults(false)
  if (connections.value.length === 0) loadConnections()
  if (sapConfigs.value.length === 0) loadSap()
  interfaceDialog.value = true
}

async function submitInterface() {
  const payload = buildInterfacePayload()
  if (!validateInterfacePayload(payload)) return
  if (payload.id) await updateInterface(payload.id, payload)
  else await createInterface(payload)
  ElMessage.success('接口定义已保存')
  interfaceDialog.value = false
  loadInterfaces()
}

function handleInterfaceModeChange(mode) {
  interfaceForm.value.protocol = mode === 'SAP' ? 'SAP_RFC' : 'REST'
  interfaceForm.value.connectionCode = ''
  applyProtocolDefaults(true)
}

function handleProtocolChange() {
  interfaceForm.value.connectionCode = ''
  applyProtocolDefaults(true)
}

function applyProtocolDefaults(resetProtocolFields) {
  if (interfaceMode.value === 'SAP') {
    interfaceForm.value.systemCode = interfaceForm.value.systemCode || 'SAP'
    if (interfaceForm.value.systemCode === 'EXTERNAL') interfaceForm.value.systemCode = 'SAP'
    if (interfaceForm.value.protocol === 'SAP_RFC') {
      if (resetProtocolFields) {
        interfaceForm.value.endpointPath = ''
        interfaceForm.value.contentType = ''
      }
      interfaceForm.value.httpMethod = ''
    } else {
      interfaceForm.value.httpMethod = interfaceForm.value.httpMethod || 'POST'
      interfaceForm.value.contentType = interfaceForm.value.contentType || 'application/json'
    }
    return
  }
  if (!interfaceForm.value.systemCode || interfaceForm.value.systemCode === 'SAP') {
    interfaceForm.value.systemCode = 'EXTERNAL'
  }
  interfaceForm.value.httpMethod = interfaceForm.value.httpMethod || 'POST'
  interfaceForm.value.contentType = interfaceForm.value.contentType || 'application/json'
  if (resetProtocolFields) interfaceForm.value.sapFunctionName = ''
}

function buildInterfacePayload() {
  const payload = { ...interfaceForm.value }
  if (sapProtocols.includes(payload.protocol)) {
    payload.systemCode = payload.systemCode || 'SAP'
    if (payload.protocol === 'SAP_RFC') {
      payload.httpMethod = ''
      payload.endpointPath = ''
      payload.contentType = ''
    }
  } else {
    payload.systemCode = payload.systemCode || 'EXTERNAL'
    payload.sapFunctionName = ''
    payload.httpMethod = payload.httpMethod || 'POST'
    payload.contentType = payload.contentType || 'application/json'
  }
  return payload
}

function validateInterfacePayload(payload) {
  if (!payload.interfaceCode || !payload.interfaceName) {
    ElMessage.warning('请填写接口编码和接口名称')
    return false
  }
  if (!payload.connectionCode) {
    ElMessage.warning(interfaceMode.value === 'SAP' ? '请选择SAP连接配置' : '请选择通用连接配置')
    return false
  }
  if (sapProtocols.includes(payload.protocol)) {
    if (payload.protocol !== 'SAP_ODATA' && !payload.sapFunctionName) {
      ElMessage.warning('请填写SAP函数、BAPI或IDoc消息类型')
      return false
    }
    if (payload.protocol === 'SAP_ODATA' && (!payload.sapFunctionName || !payload.endpointPath)) {
      ElMessage.warning('请填写OData对象和OData路径')
      return false
    }
    return true
  }
  if (!payload.httpMethod || !payload.endpointPath) {
    ElMessage.warning('请填写HTTP方法和接口路径')
    return false
  }
  return true
}

async function handleDeleteInterface(id) {
  await deleteInterface(id)
  ElMessage.success('接口定义已删除')
  loadInterfaces()
}

function selectInterface(row) {
  selectedInterfaceId.value = row.id
  activeTab.value = 'mappings'
  loadMappings()
}

function openMapping(row) {
  mappingForm.value = row ? { ...row } : { ...defaultMapping(), interfaceId: selectedInterfaceId.value }
  mappingDialog.value = true
}

async function submitMapping() {
  if (!mappingForm.value.interfaceId) {
    ElMessage.warning('请先选择接口')
    return
  }
  if (mappingForm.value.id) await updateMapping(mappingForm.value.id, mappingForm.value)
  else await createMapping(mappingForm.value)
  ElMessage.success('字段映射已保存')
  mappingDialog.value = false
  loadMappings()
}

async function handleDeleteMapping(id) {
  await deleteMapping(id)
  ElMessage.success('字段映射已删除')
  loadMappings()
}

async function handleRepush(row) {
  await repushLog(row.id)
  ElMessage.success('已加入重新推送队列')
  loadLogs()
}

function statusType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'RETRYING') return 'warning'
  return 'info'
}

onMounted(() => {
  loadConnections()
})
</script>

<style scoped>
.integration-page :deep(.el-card__body) { padding-top: 12px; }
.card-header, .header-actions, .toolbar { display: flex; align-items: center; gap: 10px; }
.card-header { justify-content: space-between; }
.toolbar { margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
.interface-alert { margin-bottom: 14px; }
.interface-dialog :deep(.el-radio-button__inner) { min-width: 92px; }
</style>
