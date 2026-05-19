package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.context.UserContext;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.util.InputSanitizer;
import com.huafu.crm.customer.dto.IntegrationFieldMappingDTO;
import com.huafu.crm.customer.dto.IntegrationConnectionConfigDTO;
import com.huafu.crm.customer.dto.IntegrationInterfaceDTO;
import com.huafu.crm.customer.dto.IntegrationLogCreateDTO;
import com.huafu.crm.customer.dto.SapRfcConfigDTO;
import com.huafu.crm.customer.entity.IntegrationConnectionConfig;
import com.huafu.crm.customer.entity.IntegrationFieldMapping;
import com.huafu.crm.customer.entity.IntegrationInterface;
import com.huafu.crm.customer.entity.IntegrationLog;
import com.huafu.crm.customer.entity.SapRfcConfig;
import com.huafu.crm.customer.mapper.IntegrationConnectionConfigMapper;
import com.huafu.crm.customer.mapper.IntegrationFieldMappingMapper;
import com.huafu.crm.customer.mapper.IntegrationInterfaceMapper;
import com.huafu.crm.customer.mapper.IntegrationLogMapper;
import com.huafu.crm.customer.mapper.SapRfcConfigMapper;
import com.huafu.crm.customer.service.IntegrationPlatformService;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class IntegrationPlatformServiceImpl implements IntegrationPlatformService {
    private static final Set<String> SAP_PROTOCOLS = Set.of("SAP_RFC", "SAP_ODATA", "SAP_IDOC");
    private static final Set<String> GENERIC_PROTOCOLS = Set.of("REST", "SOAP", "WEBHOOK", "SFTP", "FTP", "DATABASE", "KAFKA", "RABBITMQ", "CUSTOM");
    private static final Set<String> PARAMETER_MODES = Set.of("SINGLE", "TABLE");
    private static final Set<String> MAPPING_DIRECTIONS = Set.of("OUTBOUND", "INBOUND", "BIDIRECTIONAL");

    private final IntegrationConnectionConfigMapper connectionMapper;
    private final SapRfcConfigMapper sapRfcConfigMapper;
    private final IntegrationInterfaceMapper interfaceMapper;
    private final IntegrationFieldMappingMapper mappingMapper;
    private final IntegrationLogMapper logMapper;
    private final UserContext userContext;

    public IntegrationPlatformServiceImpl(
        IntegrationConnectionConfigMapper connectionMapper,
        SapRfcConfigMapper sapRfcConfigMapper,
        IntegrationInterfaceMapper interfaceMapper,
        IntegrationFieldMappingMapper mappingMapper,
        IntegrationLogMapper logMapper,
        UserContext userContext
    ) {
        this.connectionMapper = connectionMapper;
        this.sapRfcConfigMapper = sapRfcConfigMapper;
        this.interfaceMapper = interfaceMapper;
        this.mappingMapper = mappingMapper;
        this.logMapper = logMapper;
        this.userContext = userContext;
    }

    @Override
    public List<IntegrationConnectionConfig> listConnectionConfigs(String connectionType) {
        LambdaQueryWrapper<IntegrationConnectionConfig> wrapper = new LambdaQueryWrapper<IntegrationConnectionConfig>()
            .eq(IntegrationConnectionConfig::getDeleted, (short) 0)
            .orderByDesc(IntegrationConnectionConfig::getEnabled)
            .orderByAsc(IntegrationConnectionConfig::getConnectionCode);
        if (StringUtils.hasText(connectionType)) {
            wrapper.eq(IntegrationConnectionConfig::getConnectionType, connectionType);
        }
        return connectionMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public IntegrationConnectionConfig saveConnectionConfig(IntegrationConnectionConfigDTO dto) {
        if (existsConnectionCode(dto.connectionCode(), null)) {
            throw new BizException(1001, "连接编码已存在：" + dto.connectionCode());
        }
        IntegrationConnectionConfig entity = toConnection(dto, new IntegrationConnectionConfig());
        entity.setCreatedBy(currentUserId());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        entity.setDeleted((short) 0);
        connectionMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public IntegrationConnectionConfig updateConnectionConfig(Long id, IntegrationConnectionConfigDTO dto) {
        IntegrationConnectionConfig entity = requireConnection(id);
        if (existsConnectionCode(dto.connectionCode(), id)) {
            throw new BizException(1001, "连接编码已存在：" + dto.connectionCode());
        }
        toConnection(dto, entity);
        entity.setId(id);
        entity.setUpdatedBy(currentUserId());
        entity.setUpdatedTime(LocalDateTime.now());
        connectionMapper.updateById(entity);
        return requireConnection(id);
    }

    @Override
    @Transactional
    public void deleteConnectionConfig(Long id) {
        requireConnection(id);
        connectionMapper.deleteById(id);
    }

    @Override
    public String testConnectionConfig(Long id) {
        IntegrationConnectionConfig cfg = requireConnection(id);
        if (cfg.getEnabled() == null || cfg.getEnabled() == 0) {
            throw new BizException(1001, "连接配置未启用");
        }
        if (!StringUtils.hasText(cfg.getConnectionType())) {
            throw new BizException(1001, "连接类型不能为空");
        }
        String type = cfg.getConnectionType();
        if (("REST".equals(type) || "SOAP".equals(type) || "WEBHOOK".equals(type) || "SAP_ODATA".equals(type))
            && !StringUtils.hasText(cfg.getBaseUrl())) {
            throw new BizException(1001, "HTTP类连接必须配置Base URL");
        }
        return "连接配置参数校验通过，真实连通性将在对应协议执行器启用后验证";
    }

    @Override
    public List<SapRfcConfig> listSapRfcConfigs() {
        return sapRfcConfigMapper.selectList(new LambdaQueryWrapper<SapRfcConfig>()
            .eq(SapRfcConfig::getDeleted, (short) 0)
            .orderByDesc(SapRfcConfig::getEnabled)
            .orderByAsc(SapRfcConfig::getConfigCode));
    }

    @Override
    @Transactional
    public SapRfcConfig saveSapRfcConfig(SapRfcConfigDTO dto) {
        if (existsSapConfigCode(dto.configCode(), null)) {
            throw new BizException(1001, "SAP RFC配置编码已存在：" + dto.configCode());
        }
        SapRfcConfig entity = toSapConfig(dto, new SapRfcConfig());
        entity.setCreatedBy(currentUserId());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        entity.setDeleted((short) 0);
        sapRfcConfigMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public SapRfcConfig updateSapRfcConfig(Long id, SapRfcConfigDTO dto) {
        SapRfcConfig entity = requireSapConfig(id);
        if (existsSapConfigCode(dto.configCode(), id)) {
            throw new BizException(1001, "SAP RFC配置编码已存在：" + dto.configCode());
        }
        toSapConfig(dto, entity);
        entity.setId(id);
        entity.setUpdatedBy(currentUserId());
        entity.setUpdatedTime(LocalDateTime.now());
        sapRfcConfigMapper.updateById(entity);
        return requireSapConfig(id);
    }

    @Override
    @Transactional
    public void deleteSapRfcConfig(Long id) {
        requireSapConfig(id);
        sapRfcConfigMapper.deleteById(id);
    }

    @Override
    public String testSapRfcConfig(Long id) {
        SapRfcConfig cfg = requireSapConfig(id);
        if (cfg.getEnabled() == null || cfg.getEnabled() == 0) {
            throw new BizException(1001, "SAP RFC配置未启用");
        }
        if (!StringUtils.hasText(cfg.getAppServerHost()) || !StringUtils.hasText(cfg.getSystemNumber())
            || !StringUtils.hasText(cfg.getClient()) || !StringUtils.hasText(cfg.getUserName())) {
            throw new BizException(1001, "SAP RFC连接参数不完整");
        }
        return "SAP RFC配置参数校验通过，真实连通性将在接入SAP JCo执行器后验证";
    }

    @Override
    public List<IntegrationInterface> listInterfaces(String keyword, String systemCode, String enabled) {
        LambdaQueryWrapper<IntegrationInterface> wrapper = new LambdaQueryWrapper<IntegrationInterface>()
            .eq(IntegrationInterface::getDeleted, (short) 0)
            .orderByDesc(IntegrationInterface::getEnabled)
            .orderByAsc(IntegrationInterface::getInterfaceCode);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IntegrationInterface::getInterfaceCode, keyword)
                .or().like(IntegrationInterface::getInterfaceName, keyword)
                .or().like(IntegrationInterface::getSapFunctionName, keyword));
        }
        if (StringUtils.hasText(systemCode)) {
            wrapper.eq(IntegrationInterface::getSystemCode, systemCode);
        }
        if (StringUtils.hasText(enabled)) {
            wrapper.eq(IntegrationInterface::getEnabled, Boolean.parseBoolean(enabled) ? (short) 1 : (short) 0);
        }
        return interfaceMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public IntegrationInterface saveInterface(IntegrationInterfaceDTO dto) {
        validateInterfaceConfig(dto);
        if (existsInterfaceCode(dto.interfaceCode(), null)) {
            throw new BizException(1001, "接口编码已存在：" + dto.interfaceCode());
        }
        IntegrationInterface entity = toInterface(dto, new IntegrationInterface());
        entity.setCreatedBy(currentUserId());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        entity.setDeleted((short) 0);
        interfaceMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public IntegrationInterface updateInterface(Long id, IntegrationInterfaceDTO dto) {
        IntegrationInterface entity = requireInterface(id);
        validateInterfaceConfig(dto);
        if (existsInterfaceCode(dto.interfaceCode(), id)) {
            throw new BizException(1001, "接口编码已存在：" + dto.interfaceCode());
        }
        toInterface(dto, entity);
        entity.setId(id);
        entity.setUpdatedBy(currentUserId());
        entity.setUpdatedTime(LocalDateTime.now());
        interfaceMapper.updateById(entity);
        return requireInterface(id);
    }

    @Override
    @Transactional
    public void deleteInterface(Long id) {
        requireInterface(id);
        interfaceMapper.deleteById(id);
    }

    @Override
    public List<IntegrationFieldMapping> listMappings(Long interfaceId) {
        return mappingMapper.selectList(new LambdaQueryWrapper<IntegrationFieldMapping>()
            .eq(IntegrationFieldMapping::getInterfaceId, interfaceId)
            .eq(IntegrationFieldMapping::getDeleted, (short) 0)
            .orderByAsc(IntegrationFieldMapping::getSortOrder)
            .orderByAsc(IntegrationFieldMapping::getId));
    }

    @Override
    @Transactional
    public IntegrationFieldMapping saveMapping(IntegrationFieldMappingDTO dto) {
        requireInterface(dto.interfaceId());
        validateMappingConfig(dto);
        IntegrationFieldMapping entity = toMapping(dto, new IntegrationFieldMapping());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        entity.setDeleted((short) 0);
        mappingMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public IntegrationFieldMapping updateMapping(Long id, IntegrationFieldMappingDTO dto) {
        requireInterface(dto.interfaceId());
        validateMappingConfig(dto);
        IntegrationFieldMapping entity = requireMapping(id);
        toMapping(dto, entity);
        entity.setId(id);
        entity.setUpdatedTime(LocalDateTime.now());
        mappingMapper.updateById(entity);
        return requireMapping(id);
    }

    @Override
    @Transactional
    public void deleteMapping(Long id) {
        requireMapping(id);
        mappingMapper.deleteById(id);
    }

    @Override
    public PageResult<IntegrationLog> pageLogs(long current, long size, String interfaceCode, String status, String keyword) {
        LambdaQueryWrapper<IntegrationLog> wrapper = new LambdaQueryWrapper<IntegrationLog>()
            .eq(IntegrationLog::getDeleted, (short) 0)
            .orderByDesc(IntegrationLog::getCreatedTime);
        if (StringUtils.hasText(interfaceCode)) {
            wrapper.eq(IntegrationLog::getInterfaceCode, interfaceCode);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(IntegrationLog::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IntegrationLog::getBusinessKey, keyword)
                .or().like(IntegrationLog::getErrorMessage, keyword)
                .or().like(IntegrationLog::getRequestPayload, keyword));
        }
        Page<IntegrationLog> page = logMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    @Override
    @Transactional
    public IntegrationLog createLog(IntegrationLogCreateDTO dto) {
        IntegrationLog log = new IntegrationLog();
        log.setInterfaceCode(InputSanitizer.safeText(dto.interfaceCode()));
        log.setInterfaceName(InputSanitizer.safeText(dto.interfaceName()));
        log.setDirection(InputSanitizer.safeText(dto.direction()));
        log.setBusinessKey(InputSanitizer.safeText(dto.businessKey()));
        log.setStatus(StringUtils.hasText(dto.status()) ? InputSanitizer.safeText(dto.status()) : "PENDING");
        log.setRequestPayload(InputSanitizer.rejectUnsafeHtml(dto.requestPayload()));
        log.setResponsePayload(InputSanitizer.rejectUnsafeHtml(dto.responsePayload()));
        log.setErrorMessage(InputSanitizer.safeText(dto.errorMessage()));
        log.setRetryCount(0);
        log.setCreatedBy(currentUserId());
        log.setCreatedTime(LocalDateTime.now());
        log.setUpdatedTime(LocalDateTime.now());
        log.setDeleted((short) 0);
        logMapper.insert(log);
        return log;
    }

    @Override
    @Transactional
    public IntegrationLog repushLog(Long id) {
        IntegrationLog log = requireLog(id);
        log.setStatus("PENDING");
        log.setRetryCount((log.getRetryCount() == null ? 0 : log.getRetryCount()) + 1);
        log.setNextRetryTime(LocalDateTime.now());
        log.setErrorMessage(null);
        log.setUpdatedBy(currentUserId());
        log.setUpdatedTime(LocalDateTime.now());
        logMapper.updateById(log);
        return requireLog(id);
    }

    private SapRfcConfig toSapConfig(SapRfcConfigDTO dto, SapRfcConfig entity) {
        entity.setConfigCode(InputSanitizer.safeText(dto.configCode()));
        entity.setConfigName(InputSanitizer.safeText(dto.configName()));
        entity.setEnabled(dto.enabled() == null ? (short) 1 : dto.enabled());
        entity.setAppServerHost(InputSanitizer.safeText(dto.appServerHost()));
        entity.setSystemNumber(InputSanitizer.safeText(dto.systemNumber()));
        entity.setClient(InputSanitizer.safeText(dto.client()));
        entity.setUserName(InputSanitizer.safeText(dto.userName()));
        entity.setPasswordCipher(InputSanitizer.rejectUnsafeHtml(dto.passwordCipher()));
        entity.setLanguage(StringUtils.hasText(dto.language()) ? InputSanitizer.safeText(dto.language()) : "ZH");
        entity.setPoolCapacity(dto.poolCapacity() == null ? 5 : dto.poolCapacity());
        entity.setPeakLimit(dto.peakLimit() == null ? 10 : dto.peakLimit());
        entity.setConnectionTimeout(dto.connectionTimeout() == null ? 30000 : dto.connectionTimeout());
        entity.setRemark(InputSanitizer.safeText(dto.remark()));
        return entity;
    }

    private IntegrationInterface toInterface(IntegrationInterfaceDTO dto, IntegrationInterface entity) {
        entity.setInterfaceCode(InputSanitizer.safeText(dto.interfaceCode()));
        entity.setInterfaceName(InputSanitizer.safeText(dto.interfaceName()));
        entity.setSystemCode(StringUtils.hasText(dto.systemCode()) ? InputSanitizer.safeText(dto.systemCode()) : "SAP");
        entity.setConnectionCode(InputSanitizer.safeText(dto.connectionCode()));
        entity.setProtocol(StringUtils.hasText(dto.protocol()) ? InputSanitizer.safeText(dto.protocol()) : "SAP_RFC");
        entity.setDirection(StringUtils.hasText(dto.direction()) ? InputSanitizer.safeText(dto.direction()) : "OUTBOUND");
        entity.setBusinessModule(InputSanitizer.safeText(dto.businessModule()));
        entity.setSapFunctionName(InputSanitizer.safeText(dto.sapFunctionName()));
        entity.setHttpMethod(InputSanitizer.safeText(dto.httpMethod()));
        entity.setEndpointPath(InputSanitizer.safeText(dto.endpointPath()));
        entity.setContentType(InputSanitizer.safeText(dto.contentType()));
        entity.setEnabled(dto.enabled() == null ? (short) 1 : dto.enabled());
        entity.setRetryLimit(dto.retryLimit() == null ? 3 : dto.retryLimit());
        entity.setDescription(InputSanitizer.safeText(dto.description()));
        return entity;
    }

    private void validateInterfaceConfig(IntegrationInterfaceDTO dto) {
        String protocol = StringUtils.hasText(dto.protocol()) ? dto.protocol().trim() : "";
        if (!SAP_PROTOCOLS.contains(protocol) && !GENERIC_PROTOCOLS.contains(protocol)) {
            throw new BizException(1001, "不支持的接口协议：" + dto.protocol());
        }
        if (!StringUtils.hasText(dto.connectionCode())) {
            throw new BizException(1001, SAP_PROTOCOLS.contains(protocol) ? "SAP接口需要选择SAP连接配置" : "通用接口需要选择连接配置");
        }
        if (SAP_PROTOCOLS.contains(protocol)) {
            if ("SAP_RFC".equals(protocol) && !StringUtils.hasText(dto.sapFunctionName())) {
                throw new BizException(1001, "SAP RFC接口需要填写RFC/BAPI函数名");
            }
            if ("SAP_IDOC".equals(protocol) && !StringUtils.hasText(dto.sapFunctionName())) {
                throw new BizException(1001, "SAP IDoc接口需要填写IDoc或消息类型");
            }
            if ("SAP_ODATA".equals(protocol)
                && (!StringUtils.hasText(dto.sapFunctionName()) || !StringUtils.hasText(dto.endpointPath()))) {
                throw new BizException(1001, "SAP OData接口需要填写OData对象和OData路径");
            }
            return;
        }
        if (!StringUtils.hasText(dto.httpMethod()) || !StringUtils.hasText(dto.endpointPath())) {
            throw new BizException(1001, "通用接口需要填写HTTP方法和接口路径");
        }
    }

    private IntegrationConnectionConfig toConnection(IntegrationConnectionConfigDTO dto, IntegrationConnectionConfig entity) {
        entity.setConnectionCode(InputSanitizer.safeText(dto.connectionCode()));
        entity.setConnectionName(InputSanitizer.safeText(dto.connectionName()));
        entity.setConnectionType(StringUtils.hasText(dto.connectionType()) ? InputSanitizer.safeText(dto.connectionType()) : "REST");
        entity.setEnabled(dto.enabled() == null ? (short) 1 : dto.enabled());
        entity.setBaseUrl(InputSanitizer.safeText(dto.baseUrl()));
        entity.setAuthType(StringUtils.hasText(dto.authType()) ? InputSanitizer.safeText(dto.authType()) : "NONE");
        entity.setAuthConfig(InputSanitizer.rejectUnsafeHtml(dto.authConfig()));
        entity.setHeaderConfig(InputSanitizer.rejectUnsafeHtml(dto.headerConfig()));
        entity.setTimeoutMs(dto.timeoutMs() == null ? 30000 : dto.timeoutMs());
        entity.setRemark(InputSanitizer.safeText(dto.remark()));
        return entity;
    }

    private IntegrationConnectionConfig requireConnection(Long id) {
        IntegrationConnectionConfig entity = connectionMapper.selectById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BizException(1001, "连接配置不存在");
        }
        return entity;
    }

    private IntegrationFieldMapping toMapping(IntegrationFieldMappingDTO dto, IntegrationFieldMapping entity) {
        entity.setInterfaceId(dto.interfaceId());
        entity.setParameterMode(StringUtils.hasText(dto.parameterMode()) ? InputSanitizer.safeText(dto.parameterMode()) : "SINGLE");
        entity.setParameterGroup(InputSanitizer.safeText(dto.parameterGroup()));
        entity.setMappingDirection(StringUtils.hasText(dto.mappingDirection()) ? InputSanitizer.safeText(dto.mappingDirection()) : "OUTBOUND");
        entity.setSourceModule(StringUtils.hasText(dto.sourceModule()) ? InputSanitizer.safeText(dto.sourceModule()) : "customer");
        entity.setSourceField(InputSanitizer.safeText(dto.sourceField()));
        entity.setSourceFieldLabel(InputSanitizer.safeText(dto.sourceFieldLabel()));
        entity.setTargetField(InputSanitizer.safeText(dto.targetField()));
        entity.setTargetFieldLabel(InputSanitizer.safeText(dto.targetFieldLabel()));
        entity.setFieldType(StringUtils.hasText(dto.fieldType()) ? InputSanitizer.safeText(dto.fieldType()) : "STRING");
        entity.setRequired(dto.required() == null ? (short) 0 : dto.required());
        entity.setDefaultValue(InputSanitizer.safeText(dto.defaultValue()));
        entity.setTransformRule(InputSanitizer.rejectUnsafeHtml(dto.transformRule()));
        entity.setSortOrder(dto.sortOrder() == null ? 0 : dto.sortOrder());
        entity.setRemark(InputSanitizer.safeText(dto.remark()));
        return entity;
    }

    private void validateMappingConfig(IntegrationFieldMappingDTO dto) {
        String mode = StringUtils.hasText(dto.parameterMode()) ? dto.parameterMode().trim() : "SINGLE";
        String direction = StringUtils.hasText(dto.mappingDirection()) ? dto.mappingDirection().trim() : "OUTBOUND";
        if (!PARAMETER_MODES.contains(mode)) {
            throw new BizException(1001, "不支持的传参模式：" + dto.parameterMode());
        }
        if (!MAPPING_DIRECTIONS.contains(direction)) {
            throw new BizException(1001, "不支持的映射方向：" + dto.mappingDirection());
        }
        if ("TABLE".equals(mode) && !StringUtils.hasText(dto.parameterGroup())) {
            throw new BizException(1001, "表参数需要填写参数组或表名");
        }
        if (!StringUtils.hasText(dto.sourceModule())) {
            throw new BizException(1001, "请选择CRM模块");
        }
        if (!StringUtils.hasText(dto.sourceField()) || !StringUtils.hasText(dto.targetField())) {
            throw new BizException(1001, "请配置CRM字段和接口字段");
        }
    }

    private SapRfcConfig requireSapConfig(Long id) {
        SapRfcConfig entity = sapRfcConfigMapper.selectById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BizException(1001, "SAP RFC配置不存在");
        }
        return entity;
    }

    private IntegrationInterface requireInterface(Long id) {
        IntegrationInterface entity = interfaceMapper.selectById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BizException(1001, "集成接口不存在");
        }
        return entity;
    }

    private IntegrationFieldMapping requireMapping(Long id) {
        IntegrationFieldMapping entity = mappingMapper.selectById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BizException(1001, "字段映射不存在");
        }
        return entity;
    }

    private IntegrationLog requireLog(Long id) {
        IntegrationLog entity = logMapper.selectById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BizException(1001, "集成日志不存在");
        }
        return entity;
    }

    private boolean existsSapConfigCode(String code, Long excludeId) {
        LambdaQueryWrapper<SapRfcConfig> wrapper = new LambdaQueryWrapper<SapRfcConfig>()
            .eq(SapRfcConfig::getConfigCode, code)
            .eq(SapRfcConfig::getDeleted, (short) 0);
        if (excludeId != null) {
            wrapper.ne(SapRfcConfig::getId, excludeId);
        }
        return sapRfcConfigMapper.selectCount(wrapper) > 0;
    }

    private boolean existsConnectionCode(String code, Long excludeId) {
        LambdaQueryWrapper<IntegrationConnectionConfig> wrapper = new LambdaQueryWrapper<IntegrationConnectionConfig>()
            .eq(IntegrationConnectionConfig::getConnectionCode, code)
            .eq(IntegrationConnectionConfig::getDeleted, (short) 0);
        if (excludeId != null) {
            wrapper.ne(IntegrationConnectionConfig::getId, excludeId);
        }
        return connectionMapper.selectCount(wrapper) > 0;
    }

    private boolean existsInterfaceCode(String code, Long excludeId) {
        LambdaQueryWrapper<IntegrationInterface> wrapper = new LambdaQueryWrapper<IntegrationInterface>()
            .eq(IntegrationInterface::getInterfaceCode, code)
            .eq(IntegrationInterface::getDeleted, (short) 0);
        if (excludeId != null) {
            wrapper.ne(IntegrationInterface::getId, excludeId);
        }
        return interfaceMapper.selectCount(wrapper) > 0;
    }

    private Long currentUserId() {
        return userContext.getCurrentUserId().orElse(1L);
    }
}
