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
            <el-table-column prop="parameterMode" label="传参模式" width="100">
              <template #default="{ row }"><el-tag>{{ parameterModeLabel(row.parameterMode) }}</el-tag></template>
            </el-table-column>
            <el-table-column label="参数位置/表名" min-width="160" show-overflow-tooltip>
              <template #default="{ row }">{{ mappingParameterLocation(row) }}</template>
            </el-table-column>
            <el-table-column prop="mappingDirection" label="方向" width="110" />
            <el-table-column prop="sourceModule" label="CRM模块" min-width="150">
              <template #default="{ row }">{{ mappingModuleText(row) }}</template>
            </el-table-column>
            <el-table-column label="CRM字段" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">{{ mappingSourceFieldText(row) }}</template>
            </el-table-column>
            <el-table-column label="接口单值字段" min-width="170" show-overflow-tooltip>
              <template #default="{ row }">{{ interfaceScalarFieldText(row) }}</template>
            </el-table-column>
            <el-table-column label="表内字段" min-width="170" show-overflow-tooltip>
              <template #default="{ row }">{{ tableFieldText(row) }}</template>
            </el-table-column>
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
            <el-button type="primary" @click="handleExecutePending">执行待推送</el-button>
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
            <el-table-column label="操作" width="190" fixed="right">
              <template #default="{ row }">
                <el-button link @click="openLogDetail(row)">详情</el-button>
                <el-button link type="primary" :disabled="row.status === 'SUCCESS' || row.status === 'RUNNING'" @click="handleExecute(row)">立即推送</el-button>
                <el-button link type="warning" :disabled="row.status === 'SUCCESS' || row.status === 'RUNNING'" @click="handleRepush(row)">重推</el-button>
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
          <el-col :span="12">
            <el-form-item label="成功判断">
              <el-select v-model="interfaceForm.successRuleType" style="width:100%">
                <el-option v-for="item in successRuleTypes" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="消息字段">
              <el-input v-model="interfaceForm.successMessagePath" placeholder="返回消息字段，如 body.message / RETURN[].MESSAGE" />
            </el-form-item>
          </el-col>
          <el-col v-if="['JSON_FIELD','XML_FIELD','SAP_RETURN'].includes(interfaceForm.successRuleType)" :span="12">
            <el-form-item label="判断字段">
              <el-input v-model="interfaceForm.successFieldPath" placeholder="如 body.code / RETURN[].TYPE / TYPE" />
            </el-form-item>
          </el-col>
          <el-col v-if="interfaceForm.successRuleType !== 'HTTP_STATUS' && interfaceForm.successRuleType !== 'AUTO'" :span="12">
            <el-form-item label="成功值/关键字">
              <el-input v-model="interfaceForm.successExpectedValues" placeholder="多个用逗号分隔，如 S,0,success" />
            </el-form-item>
          </el-col>
          <el-col v-if="interfaceForm.successRuleType !== 'HTTP_STATUS'" :span="12">
            <el-form-item label="失败值/关键字">
              <el-input v-model="interfaceForm.failureExpectedValues" placeholder="多个用逗号分隔，如 E,A,X,error" />
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="说明"><el-input v-model="interfaceForm.description" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="interfaceDialog=false">取消</el-button><el-button type="primary" @click="submitInterface">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="mappingDialog" :title="mappingForm.id ? '编辑字段映射' : '新增字段映射'" width="1080px">
      <el-form :model="mappingForm" label-width="120px">
        <el-alert
          class="interface-alert"
          title="单值参数用于普通字段；表参数用于 SAP TABLES 或 JSON数组行字段。表参数的每一行都可以选择不同CRM模块字段。"
          type="info"
          show-icon
          :closable="false"
        />
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="传参模式">
              <el-radio-group v-model="mappingForm.parameterMode" @change="handleParameterModeChange">
                <el-radio-button label="SINGLE">单值参数</el-radio-button>
                <el-radio-button label="TABLE">表参数</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="参数方向">
              <el-select v-model="mappingForm.mappingDirection" style="width:100%">
                <el-option label="出站：CRM -> 外部" value="OUTBOUND" />
                <el-option label="入站：外部 -> CRM" value="INBOUND" />
                <el-option label="双向" value="BIDIRECTIONAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="序号"><el-input-number v-model="mappingForm.sortOrder" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item :label="parameterGroupLabel">
              <el-input v-model="mappingForm.parameterGroup" :placeholder="parameterGroupPlaceholder" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="isTableMapping ? '默认CRM模块' : 'CRM模块'">
              <el-select v-model="mappingForm.sourceModule" style="width:100%" filterable @change="handleMappingModuleChange">
                <el-option v-for="item in crmModuleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="!isTableMapping" :span="12">
            <el-form-item label="CRM字段">
              <el-select
                v-model="mappingForm.sourceField"
                style="width:100%"
                filterable
                allow-create
                default-first-option
                placeholder="选择CRM模块字段"
                @change="handleMappingSourceFieldChange"
              >
                <el-option
                  v-for="item in currentCrmFieldOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="!isTableMapping" :span="12">
            <el-form-item label="字段显示名">
              <el-input v-model="mappingForm.sourceFieldLabel" placeholder="自动带出，可手工调整" />
            </el-form-item>
          </el-col>
          <el-col :span="24" v-if="isTableMapping">
            <el-alert
              class="table-field-alert"
              title="一个表参数映射维护一组表内字段；每一行可独立选择CRM模块，适合同一接口同时读取客户主数据、SAP信息、SAP组织等字段。"
              type="success"
              show-icon
              :closable="false"
            />
          </el-col>
          <el-col v-if="isTableMapping" :span="24">
            <div class="table-field-toolbar">
              <span>表内字段</span>
              <el-button size="small" type="primary" @click="addTableField">新增字段</el-button>
            </div>
            <el-table :data="mappingForm.tableFields" border size="small" class="table-field-editor">
              <el-table-column label="序号" width="64" align="center">
                <template #default="{ $index }">{{ $index + 1 }}</template>
              </el-table-column>
              <el-table-column label="CRM模块" min-width="170">
                <template #default="{ row }">
                  <el-select v-model="row.sourceModule" style="width:100%" filterable @change="handleTableFieldModuleChange(row)">
                    <el-option v-for="item in crmModuleOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="CRM字段" min-width="240">
                <template #default="{ row }">
                  <el-select v-model="row.sourceField" style="width:100%" filterable allow-create default-first-option @change="handleTableFieldSourceChange(row)">
                    <el-option v-for="item in tableFieldOptions(row)" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="CRM字段名" min-width="140">
                <template #default="{ row }"><el-input v-model="row.sourceFieldLabel" /></template>
              </el-table-column>
              <el-table-column label="表内字段名" min-width="150">
                <template #default="{ row }"><el-input v-model="row.targetField" placeholder="如 KUNNR / NAME1" /></template>
              </el-table-column>
              <el-table-column label="表字段名" min-width="130">
                <template #default="{ row }"><el-input v-model="row.targetFieldLabel" /></template>
              </el-table-column>
              <el-table-column label="类型" width="130">
                <template #default="{ row }">
                  <el-select v-model="row.fieldType" style="width:100%" filterable allow-create default-first-option>
                    <el-option v-for="item in fieldTypes" :key="item" :label="item" :value="item" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="必填" width="80" align="center">
                <template #default="{ row }"><el-switch v-model="row.required" :active-value="1" :inactive-value="0" /></template>
              </el-table-column>
              <el-table-column label="默认值" min-width="120">
                <template #default="{ row }"><el-input v-model="row.defaultValue" /></template>
              </el-table-column>
              <el-table-column label="转换规则" min-width="180">
                <template #default="{ row }"><el-input v-model="row.transformRule" /></template>
              </el-table-column>
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ $index }"><el-button link type="danger" @click="removeTableField($index)">删除</el-button></template>
              </el-table-column>
            </el-table>
          </el-col>
          <el-col v-if="!isTableMapping" :span="12">
            <el-form-item :label="targetFieldLabel">
              <el-input v-model="mappingForm.targetField" :placeholder="targetFieldPlaceholder" />
            </el-form-item>
          </el-col>
          <el-col v-if="!isTableMapping" :span="12">
            <el-form-item :label="targetFieldNameLabel">
              <el-input v-model="mappingForm.targetFieldLabel" placeholder="展示名，可选" />
            </el-form-item>
          </el-col>
          <el-col v-if="!isTableMapping" :span="8">
            <el-form-item label="字段类型">
              <el-select v-model="mappingForm.fieldType" style="width:100%" filterable allow-create default-first-option>
                <el-option v-for="item in fieldTypes" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="!isTableMapping" :span="8"><el-form-item label="必填"><el-switch v-model="mappingForm.required" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col v-if="!isTableMapping" :span="24"><el-form-item label="默认值"><el-input v-model="mappingForm.defaultValue" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="转换规则"><el-input v-model="mappingForm.transformRule" type="textarea" :rows="3" placeholder="可写枚举映射、格式化规则、简单表达式说明，如 date:yyyyMMdd 或 map:{A:01,B:02}" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="mappingForm.remark" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="mappingDialog=false">取消</el-button><el-button type="primary" @click="submitMapping">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="logDetailDialog" title="接口推送日志详情" width="1080px" class="log-detail-dialog">
      <el-descriptions :column="3" border size="small" class="log-summary">
        <el-descriptions-item label="接口">{{ logDetail.interfaceCode }}</el-descriptions-item>
        <el-descriptions-item label="业务键">{{ logDetail.businessKey }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="statusType(logDetail.status)">{{ logDetail.status }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="时间">{{ logDetail.createdTime }}</el-descriptions-item>
        <el-descriptions-item label="重试">{{ logDetail.retryCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="异常">{{ logDetail.errorMessage || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-tabs class="log-detail-tabs">
        <el-tab-pane label="字段映射明细">
          <el-table :data="logMappingDetails" border size="small" max-height="420">
            <el-table-column prop="parameterMode" label="模式" width="90" />
            <el-table-column prop="parameterGroup" label="参数/表名" min-width="130" show-overflow-tooltip />
            <el-table-column label="行号" width="70">
              <template #default="{ row }">{{ row.rowIndex == null ? '-' : row.rowIndex + 1 }}</template>
            </el-table-column>
            <el-table-column label="CRM字段" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">{{ row.sourceFieldLabel || row.sourceField }}</template>
            </el-table-column>
            <el-table-column label="接口字段" min-width="180" show-overflow-tooltip>
              <template #default="{ row }">{{ row.targetFieldLabel || row.targetField }}</template>
            </el-table-column>
            <el-table-column label="推送值" min-width="240" show-overflow-tooltip>
              <template #default="{ row }">{{ formatLogValue(row.value) }}</template>
            </el-table-column>
            <el-table-column label="默认值" width="90">
              <template #default="{ row }"><el-tag v-if="row.usedDefault" type="warning" size="small">是</el-tag><span v-else>否</span></template>
            </el-table-column>
            <el-table-column label="缺失" width="80">
              <template #default="{ row }"><el-tag v-if="row.missing" type="danger" size="small">是</el-tag><span v-else>否</span></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="请求报文"><pre class="payload-view">{{ prettyPayload(logDetail.requestPayload) }}</pre></el-tab-pane>
        <el-tab-pane label="接口返回"><pre class="payload-view">{{ prettyPayload(logDetail.responsePayload) }}</pre></el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { businessModules } from '@/config/businessModules'
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
  executeLog,
  executePendingLogs,
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
const logDetailDialog = ref(false)
const logDetail = ref({})
const defaultConnection = () => ({ connectionCode: '', connectionName: '', connectionType: 'REST', enabled: 1, baseUrl: '', authType: 'NONE', authConfig: '', headerConfig: '', timeoutMs: 30000, remark: '' })
const defaultSap = () => ({ configCode: 'SAP_PRD', configName: 'SAP生产环境', enabled: 1, appServerHost: '', systemNumber: '00', client: '800', userName: '', passwordCipher: '', language: 'ZH', poolCapacity: 5, peakLimit: 10, connectionTimeout: 30000, remark: '' })
const defaultInterface = () => ({
  interfaceCode: '',
  interfaceName: '',
  systemCode: 'SAP',
  connectionCode: '',
  protocol: 'SAP_RFC',
  direction: 'OUTBOUND',
  businessModule: '',
  sapFunctionName: '',
  httpMethod: 'POST',
  endpointPath: '',
  contentType: 'application/json',
  enabled: 1,
  retryLimit: 3,
  successRuleType: 'AUTO',
  successFieldPath: '',
  successExpectedValues: '',
  failureExpectedValues: '',
  successMessagePath: '',
  description: ''
})
const defaultMapping = () => ({
  interfaceId: null,
  parameterMode: 'SINGLE',
  parameterGroup: '',
  mappingDirection: 'OUTBOUND',
  sourceModule: 'customer',
  sourceField: '',
  sourceFieldLabel: '',
  targetField: '',
  targetFieldLabel: '',
  tableFieldMappings: '',
  tableFields: [],
  fieldType: 'STRING',
  required: 0,
  defaultValue: '',
  transformRule: '',
  sortOrder: 0,
  remark: ''
})
const connectionForm = ref(defaultConnection())
const sapForm = ref(defaultSap())
const interfaceForm = ref(defaultInterface())
const mappingForm = ref(defaultMapping())

const customerFields = [
  { key: 'id', label: '客户ID', type: 'number' },
  { key: 'tenantId', label: '租户ID', type: 'number' },
  { key: 'customerCode', label: '客户编码' },
  { key: 'customerName', label: '客户名称', required: true },
  { key: 'customerShortName', label: '客户简称' },
  { key: 'type', label: '客户类型', type: 'number' },
  { key: 'level', label: '客户等级', type: 'number' },
  { key: 'status', label: '客户状态', type: 'number' },
  { key: 'province', label: '省份' },
  { key: 'city', label: '城市' },
  { key: 'district', label: '区县' },
  { key: 'address', label: '详细地址' },
  { key: 'mainContactName', label: '主联系人' },
  { key: 'mainContactPhone', label: '主联系人电话' },
  { key: 'mainContactRole', label: '主联系人角色' },
  { key: 'annualRevenue', label: '年销售额', type: 'number' },
  { key: 'creditLimit', label: '信用额度', type: 'number' },
  { key: 'taxRate', label: '税率', type: 'number' },
  { key: 'paymentDays', label: '账期天数', type: 'number' },
  { key: 'sapCustomerCode', label: 'SAP客户编码' },
  { key: 'remark', label: '备注' },
  { key: 'lastVisitTime', label: '最近拜访时间', type: 'datetime' },
  { key: 'ownerUserId', label: '负责人ID', type: 'number' },
  { key: 'publicPoolTime', label: '进入公海时间', type: 'datetime' },
  { key: 'customerCategory', label: '客户分类' },
  { key: 'customerSegment', label: '客户细分' },
  { key: 'businessType', label: '业务类型', type: 'number' },
  { key: 'countryRegion', label: '国家区域' },
  { key: 'mainBrand', label: '主要品牌' },
  { key: 'annualYarnVolume', label: '年用纱量', type: 'number' },
  { key: 'machineCount', label: '机台数量', type: 'number' },
  { key: 'productionCapacity', label: '产能信息' },
  { key: 'industryPosition', label: '行业地位' },
  { key: 'mainCustomerGroup', label: '客户组' },
  { key: 'bundleCustomerName', label: '捆绑客户名称' },
  { key: 'bundleBrand', label: '捆绑品牌' },
  { key: 'ownerDeptId', label: '负责部门ID', type: 'number' },
  { key: 'salesMerchandiser', label: '销售跟单' },
  { key: 'locationLat', label: '纬度', type: 'number' },
  { key: 'locationLng', label: '经度', type: 'number' },
  { key: 'unifiedSocialCreditCode', label: '统一社会信用代码' },
  { key: 'englishName', label: '英文名称' },
  { key: 'assetType', label: '资产类型' },
  { key: 'customerSource', label: '客户来源' },
  { key: 'customerStage', label: '客户阶段', type: 'number' },
  { key: 'competitorShareJson', label: '竞品份额JSON', type: 'json' },
  { key: 'cooperationBrandJson', label: '合作品牌JSON', type: 'json' },
  { key: 'blacklist', label: '是否黑名单', type: 'number' },
  { key: 'riskLevel', label: '风险等级', type: 'number' },
  { key: 'taxId', label: '税号' },
  { key: 'bankName', label: '开户行' },
  { key: 'bankAccount', label: '银行账号' },
  { key: 'invoiceTitle', label: '发票抬头' },
  { key: 'companyCode', label: '公司代码' },
  { key: 'salesGroup', label: '销售组' },
  { key: 'priceList', label: '价格清单' },
  { key: 'currency', label: '币种' },
  { key: 'deliveryFactory', label: '交货工厂' },
  { key: 'accountAssignmentGroup', label: '账户分配组' },
  { key: 'taxClassification', label: '税分类' },
  { key: 'shipToParty', label: '送达方' },
  { key: 'soldToParty', label: '售达方' },
  { key: 'payerParty', label: '付款方' },
  { key: 'countryCode', label: '国家代码' },
  { key: 'createdBy', label: '创建人' },
  { key: 'createdTime', label: '创建时间', type: 'datetime' },
  { key: 'updatedBy', label: '更新人' },
  { key: 'updatedTime', label: '更新时间', type: 'datetime' },
  { key: 'deleted', label: '删除标记', type: 'number' },
  { key: 'version', label: '版本号', type: 'number' }
]

const customerSapInfoFields = [
  { key: 'sapInfos[].id', label: 'SAP信息ID', type: 'number' },
  { key: 'sapInfos[].tenantId', label: 'SAP信息-租户ID', type: 'number' },
  { key: 'sapInfos[].customerId', label: 'SAP信息-客户ID', type: 'number' },
  { key: 'sapInfos[].sapCode', label: 'SAP信息-SAP编号' },
  { key: 'sapInfos[].accountGroup', label: 'SAP信息-账户组' },
  { key: 'sapInfos[].countryCode', label: 'SAP信息-国家代码' },
  { key: 'sapInfos[].companyCode', label: 'SAP信息-公司代码' },
  { key: 'sapInfos[].salesOrg', label: 'SAP信息-销售组织' },
  { key: 'sapInfos[].distributionChannel', label: 'SAP信息-分销渠道' },
  { key: 'sapInfos[].division', label: 'SAP信息-产品组' },
  { key: 'sapInfos[].description', label: 'SAP信息-说明' },
  { key: 'sapInfos[].isDefault', label: 'SAP信息-是否默认', type: 'number' },
  { key: 'sapInfos[].version', label: 'SAP信息-版本号', type: 'number' },
  { key: 'sapInfos[].createdBy', label: 'SAP信息-创建人' },
  { key: 'sapInfos[].createdTime', label: 'SAP信息-创建时间', type: 'datetime' },
  { key: 'sapInfos[].updatedBy', label: 'SAP信息-更新人' },
  { key: 'sapInfos[].updatedTime', label: 'SAP信息-更新时间', type: 'datetime' },
  { key: 'sapInfos[].deleted', label: 'SAP信息-删除标记', type: 'number' }
]

const customerSapOrgFields = [
  { key: 'sapOrgs[].id', label: 'SAP组织ID', type: 'number' },
  { key: 'sapOrgs[].customerId', label: 'SAP组织-客户ID', type: 'number' },
  { key: 'sapOrgs[].sapCode', label: 'SAP组织-SAP编号' },
  { key: 'sapOrgs[].companyCode', label: 'SAP组织-公司代码', required: true },
  { key: 'sapOrgs[].salesOrg', label: 'SAP组织-销售组织' },
  { key: 'sapOrgs[].salesOffice', label: 'SAP组织-销售办公室' },
  { key: 'sapOrgs[].salesGroup', label: 'SAP组织-销售组' },
  { key: 'sapOrgs[].priceList', label: 'SAP组织-价格清单' },
  { key: 'sapOrgs[].currency', label: 'SAP组织-货币' },
  { key: 'sapOrgs[].deliveryPlant', label: 'SAP组织-交货工厂' },
  { key: 'sapOrgs[].paymentTerms', label: 'SAP组织-付款条件' },
  { key: 'sapOrgs[].taxClassification', label: 'SAP组织-税分类' },
  { key: 'sapOrgs[].version', label: 'SAP组织-版本号', type: 'number' },
  { key: 'sapOrgs[].createdBy', label: 'SAP组织-创建人' },
  { key: 'sapOrgs[].createdTime', label: 'SAP组织-创建时间', type: 'datetime' },
  { key: 'sapOrgs[].updatedBy', label: 'SAP组织-更新人' },
  { key: 'sapOrgs[].updatedTime', label: 'SAP组织-更新时间', type: 'datetime' },
  { key: 'sapOrgs[].deleted', label: 'SAP组织-删除标记', type: 'number' }
]

const baseRecordFields = [
  { key: 'recordNo', label: '记录编号' },
  { key: 'title', label: '记录标题' },
  { key: 'status', label: '状态' },
  { key: 'ownerName', label: '负责人' },
  { key: 'recordDate', label: '日期', type: 'date' },
  { key: 'remark', label: '备注' }
]

const crmModuleFieldMap = {
  customer: customerFields,
  customerSapInfo: customerSapInfoFields,
  customerSapOrg: customerSapOrgFields,
  ...Object.fromEntries(Object.entries(businessModules).map(([key, config]) => [
    key,
    [
      ...baseRecordFields,
      ...(config.fields || []).map(field => ({
        key: `payload.${field.key}`,
        label: field.label,
        type: field.type,
        required: field.required
      }))
    ]
  ]))
}

const crmModuleOptions = [
  { label: '客户主数据', value: 'customer' },
  { label: '客户SAP信息（明细）', value: 'customerSapInfo' },
  { label: '客户SAP组织（明细）', value: 'customerSapOrg' },
  ...Object.entries(businessModules).map(([value, config]) => ({ label: config.title, value }))
]
const fieldTypes = ['STRING', 'NUMBER', 'DATE', 'DATETIME', 'BOOLEAN', 'DECIMAL', 'JSON', 'ARRAY']
const parameterModes = { SINGLE: '单值', TABLE: '表参数' }
const successRuleTypes = [
  { label: '自动判断', value: 'AUTO' },
  { label: 'HTTP状态码 2xx', value: 'HTTP_STATUS' },
  { label: '返回文本包含关键字', value: 'TEXT_CONTAINS' },
  { label: 'JSON字段等于指定值', value: 'JSON_FIELD' },
  { label: 'XML字段等于指定值', value: 'XML_FIELD' },
  { label: 'SAP RETURN消息/表', value: 'SAP_RETURN' }
]
const defaultTableField = () => ({
  sourceModule: '',
  sourceField: '',
  sourceFieldLabel: '',
  targetField: '',
  targetFieldLabel: '',
  fieldType: 'STRING',
  required: 0,
  defaultValue: '',
  transformRule: '',
  remark: ''
})

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
const isTableMapping = computed(() => mappingForm.value.parameterMode === 'TABLE')
const parameterGroupLabel = computed(() => isTableMapping.value ? '表/数组名称' : '参数位置')
const parameterGroupPlaceholder = computed(() => isTableMapping.value ? 'SAP TABLES参数名，如 IT_CUSTOMER；或JSON数组名，如 items' : '如 IMPORT / EXPORT / body / query')
const targetFieldLabel = computed(() => isTableMapping.value ? '表内字段名' : '接口字段')
const targetFieldNameLabel = computed(() => isTableMapping.value ? '表字段显示名' : '接口字段名')
const targetFieldPlaceholder = computed(() => isTableMapping.value ? '表参数行内字段，如 KUNNR、NAME1、MATNR' : 'SAP单值参数名、JSON Path 或外部接口字段')
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
const currentCrmFieldOptions = computed(() => {
  const fields = crmModuleFieldMap[mappingForm.value.sourceModule] || []
  return fields.map(item => ({
    label: `${item.label} (${item.key})`,
    value: item.key,
    raw: item
  }))
})
const logMappingDetails = computed(() => parseJsonArray(logDetail.value.mappingDetail))

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
  payload.successRuleType = payload.successRuleType || 'AUTO'
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
    if (!validateSuccessRule(payload)) return false
    return true
  }
  if (!payload.httpMethod || !payload.endpointPath) {
    ElMessage.warning('请填写HTTP方法和接口路径')
    return false
  }
  if (!validateSuccessRule(payload)) return false
  return true
}

function validateSuccessRule(payload) {
  if (['JSON_FIELD', 'XML_FIELD'].includes(payload.successRuleType) && !payload.successFieldPath) {
    ElMessage.warning('字段判断规则需要填写判断字段路径')
    return false
  }
  if (payload.successRuleType === 'TEXT_CONTAINS' && !payload.successExpectedValues) {
    ElMessage.warning('文本关键字规则需要填写成功值/关键字')
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
  mappingForm.value = row
    ? { ...defaultMapping(), ...row }
    : { ...defaultMapping(), interfaceId: selectedInterfaceId.value }
  mappingForm.value.tableFields = parseTableFields(mappingForm.value.tableFieldMappings)
  if (mappingForm.value.parameterMode === 'TABLE' && mappingForm.value.tableFields.length === 0) {
    mappingForm.value.tableFields = buildLegacyTableFields(mappingForm.value)
  }
  normalizeMappingLabels()
  mappingDialog.value = true
}

async function submitMapping() {
  if (!mappingForm.value.interfaceId) {
    ElMessage.warning('请先选择接口')
    return
  }
  const payload = buildMappingPayload()
  if (!validateMappingPayload(payload)) return
  if (payload.id) await updateMapping(payload.id, payload)
  else await createMapping(payload)
  ElMessage.success('字段映射已保存')
  mappingDialog.value = false
  loadMappings()
}

function handleMappingModuleChange() {
  mappingForm.value.sourceField = ''
  mappingForm.value.sourceFieldLabel = ''
  if (mappingForm.value.parameterMode === 'TABLE') {
    mappingForm.value.tableFields = mappingForm.value.tableFields.map(row => ({
      ...row,
      sourceModule: row.sourceModule || mappingForm.value.sourceModule || 'customer'
    }))
  }
}

function handleParameterModeChange(mode) {
  if (mode === 'TABLE' && mappingForm.value.tableFields.length === 0) {
    mappingForm.value.tableFields = buildLegacyTableFields(mappingForm.value)
    if (mappingForm.value.tableFields.length === 0) addTableField()
  }
}

function handleMappingSourceFieldChange() {
  normalizeMappingLabels()
  const field = findCurrentCrmField(mappingForm.value.sourceField)
  if (field?.type === 'number') mappingForm.value.fieldType = 'NUMBER'
  else if (field?.type === 'date') mappingForm.value.fieldType = 'DATE'
  else if (!mappingForm.value.fieldType) mappingForm.value.fieldType = 'STRING'
}

function addTableField() {
  mappingForm.value.tableFields.push({
    ...defaultTableField(),
    sourceModule: mappingForm.value.sourceModule || 'customer'
  })
}

function removeTableField(index) {
  mappingForm.value.tableFields.splice(index, 1)
}

function handleTableFieldSourceChange(row) {
  row.sourceModule = row.sourceModule || mappingForm.value.sourceModule || 'customer'
  const field = findCrmField(row.sourceField, row.sourceModule)
  if (field) {
    row.sourceFieldLabel = field.label
    if (field.type === 'number') row.fieldType = 'NUMBER'
    else if (field.type === 'date') row.fieldType = 'DATE'
    else if (!row.fieldType) row.fieldType = 'STRING'
  }
}

function handleTableFieldModuleChange(row) {
  row.sourceField = ''
  row.sourceFieldLabel = ''
}

function normalizeMappingLabels() {
  if (mappingForm.value.parameterMode === 'TABLE') {
    mappingForm.value.tableFields.forEach(row => {
      row.sourceModule = row.sourceModule || mappingForm.value.sourceModule || 'customer'
      const field = findCrmField(row.sourceField, row.sourceModule)
      if (field && !row.sourceFieldLabel) row.sourceFieldLabel = field.label
      if (!row.targetFieldLabel && row.targetField) row.targetFieldLabel = row.targetField
      if (!row.fieldType) row.fieldType = 'STRING'
      if (row.required == null) row.required = 0
    })
    return
  }
  const field = findCurrentCrmField(mappingForm.value.sourceField)
  if (field && !mappingForm.value.sourceFieldLabel) {
    mappingForm.value.sourceFieldLabel = field.label
  }
  if (!mappingForm.value.targetFieldLabel && mappingForm.value.targetField) {
    mappingForm.value.targetFieldLabel = mappingForm.value.targetField
  }
}

function findCurrentCrmField(fieldKey) {
  return findCrmField(fieldKey, mappingForm.value.sourceModule)
}

function findCrmField(fieldKey, moduleKey) {
  return (crmModuleFieldMap[moduleKey || 'customer'] || []).find(item => item.key === fieldKey)
}

function tableFieldOptions(row) {
  const fields = crmModuleFieldMap[row.sourceModule || mappingForm.value.sourceModule || 'customer'] || []
  return fields.map(item => ({
    label: `${item.label} (${item.key})`,
    value: item.key,
    raw: item
  }))
}

function buildMappingPayload() {
  normalizeMappingLabels()
  if (mappingForm.value.parameterMode === 'TABLE') {
    const tableFields = normalizeTableFields(mappingForm.value.tableFields)
    const first = tableFields[0] || defaultTableField()
    const modules = uniqueTableModules(tableFields)
    return {
      ...mappingForm.value,
      sourceField: first.sourceField || '__TABLE__',
      sourceFieldLabel: first.sourceFieldLabel || '表字段组',
      targetField: first.targetField || '__TABLE__',
      targetFieldLabel: first.targetFieldLabel || mappingForm.value.parameterGroup || '表参数',
      tableFieldMappings: JSON.stringify(tableFields),
      fieldType: 'ARRAY',
      required: 0,
      defaultValue: '',
      parameterMode: 'TABLE',
      mappingDirection: mappingForm.value.mappingDirection || 'OUTBOUND',
      sourceModule: modules.length > 1 ? 'MULTI' : (modules[0] || mappingForm.value.sourceModule || 'customer')
    }
  }
  return {
    ...mappingForm.value,
    tableFieldMappings: '',
    parameterMode: mappingForm.value.parameterMode || 'SINGLE',
    mappingDirection: mappingForm.value.mappingDirection || 'OUTBOUND',
    sourceModule: mappingForm.value.sourceModule || 'customer',
    fieldType: mappingForm.value.fieldType || 'STRING'
  }
}

function validateMappingPayload(payload) {
  if (!payload.sourceModule) {
    ElMessage.warning('请选择CRM模块')
    return false
  }
  if (payload.parameterMode === 'TABLE') {
    if (!payload.parameterGroup) {
      ElMessage.warning('表参数需要填写表/数组名称')
      return false
    }
    const tableFields = parseTableFields(payload.tableFieldMappings)
    if (tableFields.length === 0) {
      ElMessage.warning('表参数至少需要维护一行表内字段')
      return false
    }
    const invalidIndex = tableFields.findIndex(row => !row.sourceField || !row.targetField)
    if (invalidIndex >= 0) {
      ElMessage.warning(`第${invalidIndex + 1}行需要配置CRM字段和表内字段名`)
      return false
    }
    const invalidModuleIndex = tableFields.findIndex(row => !row.sourceModule)
    if (invalidModuleIndex >= 0) {
      ElMessage.warning(`第${invalidModuleIndex + 1}行需要选择CRM模块`)
      return false
    }
    return true
  }
  if (!payload.sourceField) {
    ElMessage.warning('请选择CRM字段')
    return false
  }
  if (!payload.targetField) {
    ElMessage.warning(payload.parameterMode === 'TABLE' ? '请填写表内字段名' : '请填写接口字段')
    return false
  }
  return true
}

function parseTableFields(value) {
  if (!value) return []
  if (Array.isArray(value)) return normalizeTableFields(value)
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? normalizeTableFields(parsed) : []
  } catch {
    return []
  }
}

function normalizeTableFields(rows) {
  return (rows || [])
    .map(row => ({
      sourceModule: row.sourceModule || mappingForm.value.sourceModule || 'customer',
      sourceField: row.sourceField || '',
      sourceFieldLabel: row.sourceFieldLabel || '',
      targetField: row.targetField || '',
      targetFieldLabel: row.targetFieldLabel || '',
      fieldType: row.fieldType || 'STRING',
      required: row.required ? 1 : 0,
      defaultValue: row.defaultValue || '',
      transformRule: row.transformRule || '',
      remark: row.remark || ''
    }))
    .filter(row => row.sourceField || row.targetField || row.sourceFieldLabel || row.targetFieldLabel)
}

function buildLegacyTableFields(mapping) {
  if (!mapping.sourceField && !mapping.targetField) return []
  return [{
    ...defaultTableField(),
    sourceModule: mapping.sourceModule || 'customer',
    sourceField: mapping.sourceField,
    sourceFieldLabel: mapping.sourceFieldLabel,
    targetField: mapping.targetField,
    targetFieldLabel: mapping.targetFieldLabel,
    fieldType: mapping.fieldType || 'STRING',
    required: mapping.required ? 1 : 0,
    defaultValue: mapping.defaultValue || '',
    transformRule: mapping.transformRule || '',
    remark: mapping.remark || ''
  }]
}

function parameterModeLabel(value) {
  return parameterModes[value] || value || '单值'
}

function mappingSourceFieldText(row) {
  if (row.parameterMode !== 'TABLE') return row.sourceFieldLabel || row.sourceField || '-'
  const fields = parseTableFields(row.tableFieldMappings)
  if (fields.length === 0) return row.sourceFieldLabel || row.sourceField || '-'
  return `${fields.length}个字段：${fields.map(item => `${moduleLabel(item.sourceModule)}.${item.sourceFieldLabel || item.sourceField}`).join('、')}`
}

function mappingModuleText(row) {
  if (row.parameterMode !== 'TABLE') return moduleLabel(row.sourceModule)
  const modules = uniqueTableModules(parseTableFields(row.tableFieldMappings))
  if (modules.length === 0) return moduleLabel(row.sourceModule)
  return modules.map(moduleLabel).join('、')
}

function uniqueTableModules(fields) {
  return [...new Set((fields || []).map(item => item.sourceModule).filter(Boolean))]
}

function mappingParameterLocation(row) {
  if (row.parameterMode === 'TABLE') return row.parameterGroup ? `表参数：${row.parameterGroup}` : '表参数：未填写'
  return row.parameterGroup || '单值参数'
}

function interfaceScalarFieldText(row) {
  if (row.parameterMode === 'TABLE') return '-'
  return row.targetFieldLabel || row.targetField || '-'
}

function tableFieldText(row) {
  if (row.parameterMode !== 'TABLE') return '-'
  const fields = parseTableFields(row.tableFieldMappings)
  if (fields.length === 0) return row.targetFieldLabel || row.targetField || '-'
  return fields.map(item => item.targetFieldLabel || item.targetField).join('、')
}

function moduleLabel(value) {
  if (value === 'MULTI') return '多个模块'
  return crmModuleOptions.find(item => item.value === value)?.label || value || '-'
}

async function handleDeleteMapping(id) {
  await deleteMapping(id)
  ElMessage.success('字段映射已删除')
  loadMappings()
}

async function handleRepush(row) {
  await repushLog(row.id)
  ElMessage.success('重推已执行')
  loadLogs()
}

async function handleExecute(row) {
  await executeLog(row.id)
  ElMessage.success('已执行推送')
  loadLogs()
}

async function handleExecutePending() {
  const count = await executePendingLogs(20)
  ElMessage.success(`已执行${count || 0}条待推送日志`)
  loadLogs()
}

function openLogDetail(row) {
  logDetail.value = row || {}
  logDetailDialog.value = true
}

function parseJsonArray(value) {
  if (!value) return []
  if (Array.isArray(value)) return value
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function prettyPayload(value) {
  if (!value) return ''
  if (typeof value !== 'string') return JSON.stringify(value, null, 2)
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function formatLogValue(value) {
  if (value === null || value === undefined || value === '') return '-'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
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
.interface-alert, .table-field-alert { margin-bottom: 14px; }
.interface-dialog :deep(.el-radio-button__inner) { min-width: 92px; }
.table-field-toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; font-weight:600; color:#303133; }
.table-field-editor { margin-bottom:14px; }
.log-summary { margin-bottom: 12px; }
.log-detail-tabs { margin-top: 8px; }
.payload-view {
  min-height: 260px;
  max-height: 480px;
  overflow: auto;
  padding: 12px;
  margin: 0;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #f7f8fa;
  color: #303133;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
