package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.context.UserContext;
import com.huafu.crm.common.entity.CrmCustomerSapInfo;
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
import com.huafu.crm.customer.mapper.CrmCustomerSapInfoMapper;
import com.huafu.crm.customer.mapper.CustomerMapper;
import com.huafu.crm.customer.service.IntegrationPlatformService;
import com.huafu.crm.customer.service.sap.SapJcoResult;
import com.huafu.crm.customer.service.sap.SapJcoService;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class IntegrationPlatformServiceImpl implements IntegrationPlatformService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> TABLE_FIELD_LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Map<String, Object>>> LIST_MAP_TYPE = new TypeReference<>() {};
    private static final Set<String> SAP_PROTOCOLS = Set.of("SAP_RFC", "SAP_ODATA", "SAP_IDOC");
    private static final Set<String> GENERIC_PROTOCOLS = Set.of("REST", "SOAP", "WEBHOOK", "WECOM", "SFTP", "FTP", "DATABASE", "KAFKA", "RABBITMQ", "CUSTOM");
    private static final Set<String> PARAMETER_MODES = Set.of("SINGLE", "TABLE");
    private static final Set<String> MAPPING_DIRECTIONS = Set.of("OUTBOUND", "INBOUND", "BIDIRECTIONAL");
    private static final Set<String> HTTP_PROTOCOLS = Set.of("REST", "SOAP", "WEBHOOK", "WECOM", "SAP_ODATA");
    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> AUTH_TYPES = Set.of("NONE", "BASIC", "BEARER", "API_KEY");
    private static final Set<String> SUCCESS_RULE_TYPES = Set.of("AUTO", "HTTP_STATUS", "TEXT_CONTAINS", "JSON_FIELD", "XML_FIELD", "SAP_RETURN");
    private static final Set<String> TRIGGER_MODES = Set.of("MANUAL", "ON_CREATE", "ON_UPDATE", "ON_SAVE", "ON_DELETE", "CUSTOM");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final IntegrationConnectionConfigMapper connectionMapper;
    private final SapRfcConfigMapper sapRfcConfigMapper;
    private final IntegrationInterfaceMapper interfaceMapper;
    private final IntegrationFieldMappingMapper mappingMapper;
    private final IntegrationLogMapper logMapper;
    private final CrmCustomerSapInfoMapper sapInfoMapper;
    private final CustomerMapper customerMapper;
    private final UserContext userContext;
    private final SapJcoService sapJcoService;

    public IntegrationPlatformServiceImpl(
        IntegrationConnectionConfigMapper connectionMapper,
        SapRfcConfigMapper sapRfcConfigMapper,
        IntegrationInterfaceMapper interfaceMapper,
        IntegrationFieldMappingMapper mappingMapper,
        IntegrationLogMapper logMapper,
        CrmCustomerSapInfoMapper sapInfoMapper,
        CustomerMapper customerMapper,
        UserContext userContext,
        SapJcoService sapJcoService
    ) {
        this.connectionMapper = connectionMapper;
        this.sapRfcConfigMapper = sapRfcConfigMapper;
        this.interfaceMapper = interfaceMapper;
        this.mappingMapper = mappingMapper;
        this.logMapper = logMapper;
        this.sapInfoMapper = sapInfoMapper;
        this.customerMapper = customerMapper;
        this.userContext = userContext;
        this.sapJcoService = sapJcoService;
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
        if (("REST".equals(type) || "SOAP".equals(type) || "WEBHOOK".equals(type) || "WECOM".equals(type) || "SAP_ODATA".equals(type))
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
        try {
            return sapJcoService.testConnection(cfg);
        } catch (IllegalStateException ex) {
            throw new BizException(1001, ex.getMessage());
        }
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
        log.setMappingDetail(InputSanitizer.rejectUnsafeHtml(dto.mappingDetail()));
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
        logMapper.update(null, new LambdaUpdateWrapper<IntegrationLog>()
            .eq(IntegrationLog::getId, id)
            .set(IntegrationLog::getStatus, "PENDING")
            .set(IntegrationLog::getRetryCount, (log.getRetryCount() == null ? 0 : log.getRetryCount()) + 1)
            .set(IntegrationLog::getNextRetryTime, LocalDateTime.now())
            .set(IntegrationLog::getErrorMessage, null)
            .set(IntegrationLog::getUpdatedBy, currentUserId())
            .set(IntegrationLog::getUpdatedTime, LocalDateTime.now()));
        return executeLog(id);
    }

    @Override
    public IntegrationLog executeLog(Long id) {
        IntegrationLog log = requireLog(id);
        if ("SUCCESS".equals(log.getStatus())) {
            return log;
        }
        markRunning(log);
        ExecuteResult result;
        try {
            result = executeOutbound(log);
        } catch (Exception ex) {
            result = ExecuteResult.failed(ex.getMessage(), true);
        }
        return finishExecution(id, result);
    }

    @Override
    public int executePendingLogs(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<IntegrationLog> pendingLogs = logMapper.selectList(new LambdaQueryWrapper<IntegrationLog>()
            .eq(IntegrationLog::getDeleted, (short) 0)
            .in(IntegrationLog::getStatus, List.of("PENDING", "RETRYING"))
            .and(w -> w.isNull(IntegrationLog::getNextRetryTime).or().le(IntegrationLog::getNextRetryTime, LocalDateTime.now()))
            .orderByAsc(IntegrationLog::getCreatedTime)
            .last("LIMIT " + safeLimit));
        int executed = 0;
        for (IntegrationLog log : pendingLogs) {
            try {
                executeLog(log.getId());
                executed++;
            } catch (Exception ignored) {
                // Individual failures are recorded on the integration log.
            }
        }
        return executed;
    }

    private void markRunning(IntegrationLog log) {
        logMapper.update(null, new LambdaUpdateWrapper<IntegrationLog>()
            .eq(IntegrationLog::getId, log.getId())
            .set(IntegrationLog::getStatus, "RUNNING")
            .set(IntegrationLog::getErrorMessage, null)
            .set(IntegrationLog::getUpdatedBy, currentUserId())
            .set(IntegrationLog::getUpdatedTime, LocalDateTime.now()));
    }

    private IntegrationLog finishExecution(Long id, ExecuteResult result) {
        IntegrationLog log = requireLog(id);
        IntegrationInterface iface = findEnabledInterface(log.getInterfaceCode());
        boolean canRetry = result.retryable()
            && iface != null
            && (log.getRetryCount() == null ? 0 : log.getRetryCount()) < (iface.getRetryLimit() == null ? 3 : iface.getRetryLimit());
        LambdaUpdateWrapper<IntegrationLog> wrapper = new LambdaUpdateWrapper<IntegrationLog>()
            .eq(IntegrationLog::getId, id)
            .set(IntegrationLog::getUpdatedBy, currentUserId())
            .set(IntegrationLog::getUpdatedTime, LocalDateTime.now());
        if (result.success()) {
            wrapper.set(IntegrationLog::getStatus, "SUCCESS")
                .set(IntegrationLog::getResponsePayload, result.responsePayload())
                .set(IntegrationLog::getErrorMessage, null)
                .set(IntegrationLog::getNextRetryTime, null)
                .set(IntegrationLog::getPushedTime, LocalDateTime.now());
        } else {
            wrapper.set(IntegrationLog::getStatus, canRetry ? "RETRYING" : "FAILED")
                .set(IntegrationLog::getResponsePayload, result.responsePayload())
                .set(IntegrationLog::getErrorMessage, result.errorMessage())
                .set(IntegrationLog::getNextRetryTime, canRetry ? LocalDateTime.now().plusMinutes(1) : null);
        }
        logMapper.update(null, wrapper);
        return requireLog(id);
    }

    private ExecuteResult executeOutbound(IntegrationLog log) {
        IntegrationInterface iface = findEnabledInterface(log.getInterfaceCode());
        if (iface == null) {
            return ExecuteResult.failed("接口定义不存在或未启用：" + log.getInterfaceCode(), false);
        }
        List<IntegrationFieldMapping> outboundMappings = listMappings(iface.getId()).stream()
            .filter(mapping -> "OUTBOUND".equals(mapping.getMappingDirection()) || "BIDIRECTIONAL".equals(mapping.getMappingDirection()))
            .toList();
        updateMappingDetail(log.getId(), buildMappingDetail(log.getRequestPayload(), outboundMappings));
        String protocol = iface.getProtocol();
        if (HTTP_PROTOCOLS.contains(protocol)) {
            return executeHttp(iface, log, outboundMappings);
        }
        if ("SAP_RFC".equals(protocol)) {
            return executeSapRfc(iface, log, outboundMappings);
        }
        return ExecuteResult.failed("接口协议暂未接入执行器：" + protocol, false);
    }

    private ExecuteResult executeHttp(IntegrationInterface iface, IntegrationLog log, List<IntegrationFieldMapping> mappings) {
        IntegrationConnectionConfig connection = findConnection(iface.getConnectionCode());
        if (connection == null) {
            return ExecuteResult.failed("连接配置不存在或未启用：" + iface.getConnectionCode(), false);
        }
        if (!StringUtils.hasText(connection.getBaseUrl())) {
            return ExecuteResult.failed("连接配置缺少Base URL：" + connection.getConnectionCode(), false);
        }
        try {
            String method = StringUtils.hasText(iface.getHttpMethod()) ? iface.getHttpMethod().toUpperCase() : "POST";
            String contentType = StringUtils.hasText(iface.getContentType()) ? iface.getContentType() : "application/json";
            HttpMappedPayload mappedPayload = buildHttpMappedPayload(log.getRequestPayload(), mappings);
            String url = appendQueryParams(joinUrl(connection.getBaseUrl(), iface.getEndpointPath()), mappedPayload.queryParams());
            String body = mappedPayload.body();
            if (isWeComWebhook(iface, connection)) {
                method = "POST";
                contentType = "application/json";
                url = buildWeComWebhookUrl(connection, iface);
                body = buildWeComWebhookBody(body, connection);
            }
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(connection.getTimeoutMs() == null ? 30000 : connection.getTimeoutMs()))
                .header("Content-Type", contentType);
            applyHeaders(builder, connection.getHeaderConfig());
            mappedPayload.headers().forEach(builder::header);
            applyAuth(builder, connection);
            if ("GET".equals(method)) {
                builder.GET();
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            }
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connection.getTimeoutMs() == null ? 30000 : connection.getTimeoutMs()))
                .build()
                .send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String responsePayload = toJson(Map.of("statusCode", response.statusCode(), "body", response.body()));
            ResponseDecision decision = evaluateResponseSuccess(iface, responsePayload, response.statusCode());
            boolean ok = decision.success();
            return ok
                ? ExecuteResult.success(responsePayload)
                : ExecuteResult.failed("接口返回判断为失败：" + decision.message(), responsePayload, response.statusCode() >= 500);
        } catch (Exception ex) {
            return ExecuteResult.failed("HTTP调用异常：" + ex.getMessage(), true);
        }
    }

    private HttpMappedPayload buildHttpMappedPayload(String sourcePayload, List<IntegrationFieldMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            return new HttpMappedPayload(sourcePayload == null ? "" : sourcePayload, Map.of(), Map.of());
        }
        try {
            JsonNode root = StringUtils.hasText(sourcePayload)
                ? OBJECT_MAPPER.readTree(sourcePayload)
                : OBJECT_MAPPER.createObjectNode();
            Map<String, Object> body = new LinkedHashMap<>();
            Map<String, String> queryParams = new LinkedHashMap<>();
            Map<String, String> headers = new LinkedHashMap<>();
            boolean mapped = false;
            for (IntegrationFieldMapping mapping : mappings) {
                if ("TABLE".equals(mapping.getParameterMode())) {
                    mapped = appendMappedTable(body, root, mapping) || mapped;
                } else {
                    ResolvedValue resolved = resolveMappingValue(root, mapping.getSourceModule(), mapping.getSourceField(), mapping.getDefaultValue(), 0);
                    if (resolved.value() == null || !StringUtils.hasText(mapping.getTargetField())) {
                        continue;
                    }
                    String location = StringUtils.hasText(mapping.getParameterGroup()) ? mapping.getParameterGroup().trim().toLowerCase() : "body";
                    if ("query".equals(location)) {
                        queryParams.put(mapping.getTargetField(), String.valueOf(resolved.value()));
                    } else if ("header".equals(location) || "headers".equals(location)) {
                        headers.put(mapping.getTargetField(), String.valueOf(resolved.value()));
                    } else {
                        putNestedValue(body, normalizeHttpTargetPath(mapping.getTargetField()), resolved.value());
                    }
                    mapped = true;
                }
            }
            String bodyPayload = mapped && !body.isEmpty() ? toJson(body) : (sourcePayload == null ? "" : sourcePayload);
            return new HttpMappedPayload(bodyPayload, queryParams, headers);
        } catch (Exception ex) {
            throw new BizException(1001, "HTTP接口字段映射生成失败：" + ex.getMessage());
        }
    }

    private boolean isWeComWebhook(IntegrationInterface iface, IntegrationConnectionConfig connection) {
        return "WECOM".equals(iface.getProtocol()) || "WECOM".equals(connection.getConnectionType());
    }

    private String buildWeComWebhookUrl(IntegrationConnectionConfig connection, IntegrationInterface iface) throws Exception {
        String url = joinUrl(connection.getBaseUrl(), iface.getEndpointPath());
        Map<String, Object> auth = readAuthConfig(connection);
        String key = firstTextValue(auth, List.of("key", "robotKey", "webhookKey"));
        if (!StringUtils.hasText(key) || url.contains("key=")) {
            return url;
        }
        return appendQueryParams(url, Map.of("key", key));
    }

    private String buildWeComWebhookBody(String sourceBody, IntegrationConnectionConfig connection) throws Exception {
        Map<String, Object> auth = readAuthConfig(connection);
        if (StringUtils.hasText(sourceBody)) {
            try {
                JsonNode node = OBJECT_MAPPER.readTree(sourceBody);
                if (node.hasNonNull("msgtype")) {
                    return sourceBody;
                }
                Map<String, Object> officialBody = inferWeComMessage(node, auth);
                if (!officialBody.isEmpty()) {
                    return toJson(officialBody);
                }
                String content = node.hasNonNull("content") ? node.path("content").asText() : node.toString();
                return toJson(defaultWeComMessage(content, auth));
            } catch (Exception ignored) {
                return toJson(defaultWeComMessage(sourceBody, auth));
            }
        }
        return toJson(defaultWeComMessage(firstTextValue(auth, List.of("defaultContent", "content")), auth));
    }

    private Map<String, Object> inferWeComMessage(JsonNode node, Map<String, Object> auth) {
        String msgType = firstTextValue(auth, List.of("defaultMsgType", "msgType"));
        if (node.hasNonNull("msgType")) {
            msgType = node.path("msgType").asText();
        }
        if (node.hasNonNull("messageType")) {
            msgType = node.path("messageType").asText();
        }
        if (!StringUtils.hasText(msgType)) {
            if (node.has("markdown")) msgType = "markdown";
            else if (node.has("image")) msgType = "image";
            else if (node.has("file")) msgType = "file";
            else if (node.has("news") || node.has("articles")) msgType = "news";
            else msgType = "text";
        }
        msgType = msgType.toLowerCase();
        String content = node.hasNonNull("content") ? node.path("content").asText() : firstTextValue(auth, List.of("defaultContent", "content"));
        if ("text".equals(msgType)) {
            Map<String, Object> textAuth = new LinkedHashMap<>(auth);
            textAuth.put("defaultMsgType", "text");
            if (node.has("mentioned_list")) {
                textAuth.put("mentionedList", node.get("mentioned_list"));
            }
            if (node.has("mentioned_mobile_list")) {
                textAuth.put("mentionedMobileList", node.get("mentioned_mobile_list"));
            }
            return defaultWeComMessage(content, textAuth);
        }
        if ("markdown".equals(msgType)) {
            String markdown = content;
            if (!StringUtils.hasText(markdown) && node.path("markdown").hasNonNull("content")) {
                markdown = node.path("markdown").path("content").asText();
            }
            return Map.of("msgtype", "markdown", "markdown", Map.of("content", StringUtils.hasText(markdown) ? markdown : "CRM通知"));
        }
        if ("image".equals(msgType)) {
            String base64 = firstTextValue(node, List.of("base64", "imageBase64"));
            String md5 = firstTextValue(node, List.of("md5", "imageMd5"));
            if (!StringUtils.hasText(base64) && node.path("image").hasNonNull("base64")) {
                base64 = node.path("image").path("base64").asText();
            }
            if (!StringUtils.hasText(md5) && node.path("image").hasNonNull("md5")) {
                md5 = node.path("image").path("md5").asText();
            }
            if (StringUtils.hasText(base64) && StringUtils.hasText(md5)) {
                return Map.of("msgtype", "image", "image", Map.of("base64", base64, "md5", md5));
            }
            return defaultWeComMessage("企微图片消息缺少 base64 或 md5，已转为文本提醒。", auth);
        }
        if ("file".equals(msgType)) {
            String mediaId = firstTextValue(node, List.of("media_id", "mediaId"));
            if (!StringUtils.hasText(mediaId) && node.path("file").hasNonNull("media_id")) {
                mediaId = node.path("file").path("media_id").asText();
            }
            if (!StringUtils.hasText(mediaId) && node.path("file").hasNonNull("mediaId")) {
                mediaId = node.path("file").path("mediaId").asText();
            }
            if (StringUtils.hasText(mediaId)) {
                return Map.of("msgtype", "file", "file", Map.of("media_id", mediaId));
            }
            return defaultWeComMessage("企微文件消息缺少 media_id，已转为文本提醒。", auth);
        }
        if ("news".equals(msgType)) {
            JsonNode articles = node.has("articles") ? node.path("articles") : node.path("news").path("articles");
            if (articles.isArray() && !articles.isEmpty()) {
                try {
                    return Map.of("msgtype", "news", "news", Map.of("articles", OBJECT_MAPPER.convertValue(articles, LIST_MAP_TYPE)));
                } catch (IllegalArgumentException ex) {
                    return defaultWeComMessage("企微图文消息 articles 格式错误，已转为文本提醒。", auth);
                }
            }
            return defaultWeComMessage("企微图文消息缺少 articles，已转为文本提醒。", auth);
        }
        return defaultWeComMessage(content, auth);
    }

    private Map<String, Object> defaultWeComMessage(String content, Map<String, Object> auth) {
        String msgType = firstTextValue(auth, List.of("defaultMsgType", "msgType"));
        if (!StringUtils.hasText(msgType)) {
            msgType = "text";
        }
        String safeContent = StringUtils.hasText(content) ? content : "CRM通知";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("msgtype", msgType);
        if ("markdown".equalsIgnoreCase(msgType)) {
            body.put("markdown", Map.of("content", safeContent));
            return body;
        }
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("content", safeContent);
        List<String> mentionedList = stringList(auth.get("mentionedList"));
        List<String> mentionedMobileList = stringList(auth.get("mentionedMobileList"));
        if (!mentionedList.isEmpty()) {
            text.put("mentioned_list", mentionedList);
        }
        if (!mentionedMobileList.isEmpty()) {
            text.put("mentioned_mobile_list", mentionedMobileList);
        }
        body.put("text", text);
        return body;
    }

    private Map<String, Object> readAuthConfig(IntegrationConnectionConfig connection) throws Exception {
        return StringUtils.hasText(connection.getAuthConfig())
            ? OBJECT_MAPPER.readValue(connection.getAuthConfig(), MAP_TYPE)
            : Map.of();
    }

    private String firstTextValue(Map<String, Object> source, List<String> keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null && StringUtils.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private String firstTextValue(JsonNode source, List<String> keys) {
        for (String key : keys) {
            if (source.hasNonNull(key) && StringUtils.hasText(source.path(key).asText())) {
                return source.path(key).asText();
            }
        }
        return null;
    }

    private List<String> stringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).map(String::trim).filter(StringUtils::hasText).toList();
        }
        if (value instanceof JsonNode node && node.isArray()) {
            List<String> result = new ArrayList<>();
            node.forEach(item -> {
                String text = item.asText();
                if (StringUtils.hasText(text)) {
                    result.add(text.trim());
                }
            });
            return result;
        }
        return Arrays.stream(String.valueOf(value).split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }

    private boolean appendMappedTable(Map<String, Object> body, JsonNode root, IntegrationFieldMapping mapping) throws Exception {
        if (!StringUtils.hasText(mapping.getParameterGroup()) || !StringUtils.hasText(mapping.getTableFieldMappings())) {
            return false;
        }
        List<Map<String, Object>> fields = OBJECT_MAPPER.readValue(mapping.getTableFieldMappings(), TABLE_FIELD_LIST_TYPE);
        int rowCount = Math.max(1, inferTableRowCount(root, fields));
        List<Map<String, Object>> tableRows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            Map<String, Object> tableRow = new LinkedHashMap<>();
            for (Map<String, Object> field : fields) {
                String targetField = stringValue(field.get("targetField"));
                if (!StringUtils.hasText(targetField)) {
                    continue;
                }
                ResolvedValue resolved = resolveMappingValue(
                    root,
                    stringValue(field.get("sourceModule")),
                    stringValue(field.get("sourceField")),
                    stringValue(field.get("defaultValue")),
                    i
                );
                if (resolved.value() != null) {
                    putNestedValue(tableRow, normalizeHttpTargetPath(targetField), resolved.value());
                }
            }
            if (!tableRow.isEmpty()) {
                tableRows.add(tableRow);
            }
        }
        if (tableRows.isEmpty()) {
            return false;
        }
        putNestedValue(body, normalizeHttpTargetPath(mapping.getParameterGroup()), tableRows);
        return true;
    }

    private String normalizeHttpTargetPath(String targetField) {
        if (!StringUtils.hasText(targetField)) {
            return targetField;
        }
        String normalized = targetField.trim();
        if (normalized.startsWith("body.")) {
            return normalized.substring("body.".length());
        }
        if (normalized.startsWith("$."))
        {
            return normalized.substring(2);
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private void putNestedValue(Map<String, Object> target, String path, Object value) {
        if (!StringUtils.hasText(path)) {
            return;
        }
        String[] parts = path.split("\\.");
        Map<String, Object> current = target;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (!StringUtils.hasText(part)) {
                continue;
            }
            if (i == parts.length - 1) {
                current.put(part, value);
                return;
            }
            Object next = current.get(part);
            if (!(next instanceof Map)) {
                next = new LinkedHashMap<String, Object>();
                current.put(part, next);
            }
            current = (Map<String, Object>) next;
        }
    }

    private String appendQueryParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        builder.append(url.contains("?") ? "&" : "?");
        boolean first = true;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (!StringUtils.hasText(entry.getKey())) {
                continue;
            }
            if (!first) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue() == null ? "" : entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        return builder.toString();
    }

    private ExecuteResult executeSapRfc(IntegrationInterface iface, IntegrationLog log, List<IntegrationFieldMapping> mappings) {
        SapRfcConfig cfg = findSapConfig(iface.getConnectionCode());
        if (cfg == null) {
            return ExecuteResult.failed("SAP RFC配置不存在或未启用：" + iface.getConnectionCode(), false);
        }
        if (!StringUtils.hasText(iface.getSapFunctionName())) {
            return ExecuteResult.failed("接口定义缺少SAP RFC/BAPI函数名", false);
        }
        SapJcoResult result = sapJcoService.execute(cfg, iface, log, mappings);
        if (!result.success()) {
            return ExecuteResult.failed(result.errorMessage(), result.retryable());
        }
        ResponseDecision decision = evaluateResponseSuccess(iface, result.responsePayload(), null);
        if (!decision.success()) {
            return ExecuteResult.failed("接口返回判断为失败：" + decision.message(), result.responsePayload(), false);
        }
        applySapCusResponseBackfill(iface, log, result.responsePayload());
        return ExecuteResult.success(result.responsePayload());
    }

    private void applySapCusResponseBackfill(IntegrationInterface iface, IntegrationLog log, String responsePayload) {
        if (!"SAP_CUS".equals(iface.getInterfaceCode()) || !StringUtils.hasText(responsePayload)) {
            return;
        }
        SapCusBusinessKey key = parseSapCusBusinessKey(log.getBusinessKey());
        if (key == null) {
            return;
        }
        String sapCode = resolveXmlFieldValue(responsePayload, "IO_ITAB.KUNNR");
        if (!StringUtils.hasText(sapCode)) {
            sapCode = resolveXmlFieldValue(responsePayload, "KUNNR");
        }
        if (!StringUtils.hasText(sapCode)) {
            return;
        }
        CrmCustomerSapInfo existing = sapInfoMapper.selectById(key.sapInfoId());
        if (existing == null || existing.getDeleted() != null && existing.getDeleted() == 1 || !key.customerId().equals(existing.getCustomerId())) {
            return;
        }
        String safeSapCode = InputSanitizer.safeText(sapCode).trim();
        sapInfoMapper.update(null, new LambdaUpdateWrapper<CrmCustomerSapInfo>()
            .eq(CrmCustomerSapInfo::getId, key.sapInfoId())
            .eq(CrmCustomerSapInfo::getCustomerId, key.customerId())
            .set(CrmCustomerSapInfo::getSapCode, safeSapCode)
            .set(CrmCustomerSapInfo::getUpdatedBy, String.valueOf(currentUserId()))
            .set(CrmCustomerSapInfo::getUpdatedTime, java.time.OffsetDateTime.now()));
        if (Integer.valueOf(1).equals(existing.getIsDefault())) {
            customerMapper.update(null, new LambdaUpdateWrapper<com.huafu.crm.customer.entity.Customer>()
                .eq(com.huafu.crm.customer.entity.Customer::getId, key.customerId())
                .set(com.huafu.crm.customer.entity.Customer::getSapCustomerCode, safeSapCode));
        }
    }

    private SapCusBusinessKey parseSapCusBusinessKey(String businessKey) {
        if (!StringUtils.hasText(businessKey) || !businessKey.startsWith("CUSTOMER_SAP_INFO:")) {
            return null;
        }
        String[] parts = businessKey.split(":");
        if (parts.length < 3) {
            return null;
        }
        try {
            return new SapCusBusinessKey(Long.parseLong(parts[1]), Long.parseLong(parts[2]));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void updateMappingDetail(Long logId, String mappingDetail) {
        logMapper.update(null, new LambdaUpdateWrapper<IntegrationLog>()
            .eq(IntegrationLog::getId, logId)
            .set(IntegrationLog::getMappingDetail, mappingDetail)
            .set(IntegrationLog::getUpdatedBy, currentUserId())
            .set(IntegrationLog::getUpdatedTime, LocalDateTime.now()));
    }

    private String buildMappingDetail(String requestPayload, List<IntegrationFieldMapping> mappings) {
        try {
            JsonNode root = StringUtils.hasText(requestPayload)
                ? OBJECT_MAPPER.readTree(requestPayload)
                : OBJECT_MAPPER.createObjectNode();
            List<Map<String, Object>> details = new ArrayList<>();
            for (IntegrationFieldMapping mapping : mappings) {
                if ("TABLE".equals(mapping.getParameterMode())) {
                    appendTableMappingDetail(details, root, mapping);
                } else {
                    ResolvedValue resolved = resolveMappingValue(root, mapping.getSourceModule(), mapping.getSourceField(), mapping.getDefaultValue(), 0);
                    details.add(mappingDetailRow(mapping, null, null, resolved));
                }
            }
            return OBJECT_MAPPER.writeValueAsString(details);
        } catch (Exception ex) {
            return toJson(List.of(Map.of("error", "字段映射明细生成失败：" + ex.getMessage())));
        }
    }

    private void appendTableMappingDetail(List<Map<String, Object>> details, JsonNode root, IntegrationFieldMapping mapping) throws Exception {
        if (!StringUtils.hasText(mapping.getTableFieldMappings())) {
            return;
        }
        List<Map<String, Object>> rows = OBJECT_MAPPER.readValue(mapping.getTableFieldMappings(), TABLE_FIELD_LIST_TYPE);
        int rowCount = Math.max(1, inferTableRowCount(root, rows));
        for (int i = 0; i < rowCount; i++) {
            for (Map<String, Object> row : rows) {
                String sourceField = stringValue(row.get("sourceField"));
                String defaultValue = stringValue(row.get("defaultValue"));
                ResolvedValue resolved = resolveMappingValue(root, stringValue(row.get("sourceModule")), sourceField, defaultValue, i);
                details.add(mappingDetailRow(mapping, row, i, resolved));
            }
        }
    }

    private Map<String, Object> mappingDetailRow(
        IntegrationFieldMapping mapping,
        Map<String, Object> tableField,
        Integer rowIndex,
        ResolvedValue resolved
    ) {
        Map<String, Object> row = new LinkedHashMap<>();
        boolean tableMode = tableField != null;
        row.put("mappingId", mapping.getId());
        row.put("parameterMode", mapping.getParameterMode());
        row.put("parameterGroup", mapping.getParameterGroup());
        row.put("mappingDirection", mapping.getMappingDirection());
        row.put("rowIndex", rowIndex);
        row.put("sourceModule", tableMode ? stringValue(tableField.get("sourceModule")) : mapping.getSourceModule());
        row.put("sourceField", tableMode ? stringValue(tableField.get("sourceField")) : mapping.getSourceField());
        row.put("sourceFieldLabel", tableMode ? stringValue(tableField.get("sourceFieldLabel")) : mapping.getSourceFieldLabel());
        row.put("targetField", tableMode ? stringValue(tableField.get("targetField")) : mapping.getTargetField());
        row.put("targetFieldLabel", tableMode ? stringValue(tableField.get("targetFieldLabel")) : mapping.getTargetFieldLabel());
        row.put("fieldType", tableMode ? stringValue(tableField.get("fieldType")) : mapping.getFieldType());
        row.put("required", tableMode ? tableField.get("required") : mapping.getRequired());
        row.put("defaultValue", tableMode ? stringValue(tableField.get("defaultValue")) : mapping.getDefaultValue());
        row.put("transformRule", tableMode ? stringValue(tableField.get("transformRule")) : mapping.getTransformRule());
        row.put("value", resolved.value());
        row.put("usedDefault", resolved.usedDefault());
        row.put("missing", resolved.missing());
        return row;
    }

    private int inferTableRowCount(JsonNode root, List<Map<String, Object>> rows) {
        int count = 0;
        for (Map<String, Object> row : rows) {
            String path = stringValue(row.get("sourceField"));
            for (String candidate : sourcePathCandidates(stringValue(row.get("sourceModule")), path)) {
                int arrayMarker = candidate.indexOf("[].");
                if (arrayMarker <= 0) {
                    continue;
                }
                JsonNode array = resolveJsonPath(root, candidate.substring(0, arrayMarker), 0);
                if (array != null && array.isArray()) {
                    count = Math.max(count, array.size());
                    break;
                }
            }
        }
        return count;
    }

    private ResolvedValue resolveMappingValue(JsonNode root, String sourceModule, String sourceField, String defaultValue, int rowIndex) {
        if (!StringUtils.hasText(sourceField)) {
            return new ResolvedValue(StringUtils.hasText(defaultValue) ? defaultValue : null, StringUtils.hasText(defaultValue), true);
        }
        JsonNode node = resolveSourceValue(root, sourceModule, sourceField, rowIndex);
        if (node == null || node.isMissingNode() || node.isNull()) {
            return new ResolvedValue(StringUtils.hasText(defaultValue) ? defaultValue : null, StringUtils.hasText(defaultValue), true);
        }
        Object value;
        if (node.isNumber()) {
            value = node.numberValue();
        } else if (node.isBoolean()) {
            value = node.booleanValue();
        } else if (node.isContainerNode()) {
            value = node.toString();
        } else {
            value = node.asText();
        }
        return new ResolvedValue(value, false, false);
    }

    private JsonNode resolveSourceValue(JsonNode root, String sourceModule, String sourceField, int rowIndex) {
        for (String candidate : sourcePathCandidates(sourceModule, sourceField)) {
            JsonNode node = resolveJsonPath(root, candidate, rowIndex);
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                return node;
            }
        }
        return null;
    }

    private JsonNode resolveJsonPath(JsonNode root, String normalized, int rowIndex) {
        JsonNode node = root;
        for (String part : normalized.split("\\.")) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            if (part.endsWith("[]")) {
                String arrayName = part.substring(0, part.length() - 2);
                node = node.path(arrayName);
                if (!node.isArray()) {
                    return null;
                }
                node = node.path(Math.min(rowIndex, Math.max(0, node.size() - 1)));
            } else {
                node = node.path(part);
            }
        }
        return node;
    }

    private List<String> sourcePathCandidates(String sourceModule, String sourceField) {
        if (!StringUtils.hasText(sourceField)) {
            return List.of();
        }
        List<String> candidates = new ArrayList<>();
        String normalized = normalizeSourceField(sourceModule, sourceField);
        candidates.add(normalized);
        if (!normalized.equals(sourceField)) {
            candidates.add(sourceField);
        }
        if ("customerSapInfo".equals(sourceModule) && normalized.startsWith("sapInfos[].")) {
            candidates.add("sapInfo." + normalized.substring("sapInfos[].".length()));
        }
        return candidates;
    }

    private String normalizeSourceField(String sourceModule, String sourceField) {
        if (sourceField.startsWith("customer.")
            || sourceField.startsWith("sapInfo.")
            || sourceField.startsWith("sapInfos[].")
            || sourceField.startsWith("allSapInfos[].")
            || sourceField.startsWith("sapOrgs[].")
            || sourceField.startsWith("payload.")) {
            return sourceField;
        }
        if (sourceField.startsWith("customerSapInfo.")) {
            return "sapInfos[]." + sourceField.substring(sourceField.indexOf('.') + 1);
        }
        if (sourceField.startsWith("customerSapOrg.")) {
            return "sapOrgs[]." + sourceField.substring(sourceField.indexOf('.') + 1);
        }
        if ("customer".equals(sourceModule)) {
            return "customer." + sourceField;
        }
        if ("customerSapInfo".equals(sourceModule)) {
            return "sapInfos[]." + sourceField;
        }
        if ("customerSapInfoAll".equals(sourceModule)) {
            return "allSapInfos[]." + sourceField;
        }
        if ("customerSapOrg".equals(sourceModule)) {
            return "sapOrgs[]." + sourceField;
        }
        return sourceField;
    }

    private ResponseDecision evaluateResponseSuccess(IntegrationInterface iface, String responsePayload, Integer httpStatus) {
        String type = StringUtils.hasText(iface.getSuccessRuleType()) ? iface.getSuccessRuleType() : "AUTO";
        String message = resolveResponseMessage(responsePayload, iface.getSuccessMessagePath());
        if ("AUTO".equals(type) && "WECOM".equals(iface.getProtocol())) {
            String errCode = resolveJsonResponseValue(responsePayload, "body.errcode");
            boolean success = "0".equals(errCode);
            return new ResponseDecision(success, StringUtils.hasText(message) ? message : "企微机器人返回 errcode=" + errCode);
        }
        if ("HTTP_STATUS".equals(type) || ("AUTO".equals(type) && HTTP_PROTOCOLS.contains(iface.getProtocol()) && !"SAP_ODATA".equals(iface.getProtocol()))) {
            boolean success = httpStatus != null && httpStatus >= 200 && httpStatus < 300;
            return new ResponseDecision(success, StringUtils.hasText(message) ? message : "HTTP状态码：" + httpStatus);
        }
        if ("TEXT_CONTAINS".equals(type)) {
            return evaluateTextContains(responsePayload, iface, message);
        }
        if ("JSON_FIELD".equals(type)) {
            return evaluateFieldValue(resolveJsonResponseValue(responsePayload, iface.getSuccessFieldPath()), iface, message);
        }
        if ("XML_FIELD".equals(type)) {
            return evaluateFieldValue(resolveXmlFieldValue(responsePayload, iface.getSuccessFieldPath()), iface, message);
        }
        if ("SAP_RETURN".equals(type) || ("AUTO".equals(type) && "SAP_RFC".equals(iface.getProtocol()))) {
            return evaluateSapReturn(responsePayload, iface, null);
        }
        boolean success = httpStatus == null || httpStatus >= 200 && httpStatus < 300;
        return new ResponseDecision(success, StringUtils.hasText(message) ? message : "AUTO规则判断：" + (success ? "成功" : "失败"));
    }

    private ResponseDecision evaluateTextContains(String responsePayload, IntegrationInterface iface, String message) {
        String payload = responsePayload == null ? "" : responsePayload;
        for (String value : splitValues(iface.getFailureExpectedValues())) {
            if (payload.contains(value)) {
                return new ResponseDecision(false, StringUtils.hasText(message) ? message : "返回包含失败关键字：" + value);
            }
        }
        List<String> successValues = splitValues(iface.getSuccessExpectedValues());
        if (successValues.isEmpty()) {
            return new ResponseDecision(false, StringUtils.hasText(message) ? message : "未配置成功关键字");
        }
        for (String value : successValues) {
            if (payload.contains(value)) {
                return new ResponseDecision(true, StringUtils.hasText(message) ? message : "返回包含成功关键字：" + value);
            }
        }
        return new ResponseDecision(false, StringUtils.hasText(message) ? message : "返回未包含成功关键字");
    }

    private ResponseDecision evaluateFieldValue(String actualValue, IntegrationInterface iface, String message) {
        if (!StringUtils.hasText(actualValue)) {
            return new ResponseDecision(false, StringUtils.hasText(message) ? message : "未找到成功判断字段：" + iface.getSuccessFieldPath());
        }
        for (String value : splitValues(iface.getFailureExpectedValues())) {
            if (actualValue.equalsIgnoreCase(value)) {
                return new ResponseDecision(false, StringUtils.hasText(message) ? message : "返回字段命中失败值：" + actualValue);
            }
        }
        List<String> successValues = splitValues(iface.getSuccessExpectedValues());
        if (successValues.isEmpty()) {
            return new ResponseDecision(true, StringUtils.hasText(message) ? message : "返回字段值：" + actualValue);
        }
        boolean success = successValues.stream().anyMatch(value -> actualValue.equalsIgnoreCase(value));
        return new ResponseDecision(success, StringUtils.hasText(message) ? message : "返回字段值：" + actualValue);
    }

    private ResponseDecision evaluateSapReturn(String responsePayload, IntegrationInterface iface, String message) {
        List<String> failureValues = splitValues(StringUtils.hasText(iface.getFailureExpectedValues()) ? iface.getFailureExpectedValues() : "E,A,X");
        List<String> successValues = splitValues(iface.getSuccessExpectedValues());
        List<SapReturnRow> returnRows = resolveSapReturnRows(responsePayload, iface);
        if (!returnRows.isEmpty()) {
            List<SapReturnRow> failedRows = returnRows.stream()
                .filter(row -> failureValues.stream().anyMatch(value -> row.type().equalsIgnoreCase(value)))
                .toList();
            if (!failedRows.isEmpty()) {
                return new ResponseDecision(false, StringUtils.hasText(message) ? message : formatSapReturnRows(failedRows));
            }
            boolean success = successValues.isEmpty()
                || returnRows.stream().anyMatch(row -> successValues.stream().anyMatch(value -> row.type().equalsIgnoreCase(value)));
            return new ResponseDecision(success, StringUtils.hasText(message) ? message : formatSapReturnRows(returnRows));
        }
        List<String> returnTypes = resolveXmlFieldValues(responsePayload, StringUtils.hasText(iface.getSuccessFieldPath()) ? iface.getSuccessFieldPath() : "TYPE");
        for (String type : returnTypes) {
            if (failureValues.stream().anyMatch(value -> type.equalsIgnoreCase(value))) {
                return new ResponseDecision(false, StringUtils.hasText(message) ? message : "SAP RETURN TYPE=" + type);
            }
        }
        if (returnTypes.isEmpty()) {
            return new ResponseDecision(true, StringUtils.hasText(message) ? message : "SAP未返回失败消息");
        }
        boolean success = successValues.isEmpty()
            || returnTypes.stream().anyMatch(type -> successValues.stream().anyMatch(value -> type.equalsIgnoreCase(value)));
        return new ResponseDecision(success, StringUtils.hasText(message) ? message : "SAP RETURN TYPE=" + String.join(",", returnTypes));
    }

    private List<SapReturnRow> resolveSapReturnRows(String responsePayload, IntegrationInterface iface) {
        String typePath = StringUtils.hasText(iface.getSuccessFieldPath()) ? iface.getSuccessFieldPath() : "RETURN[].TYPE";
        String messagePath = StringUtils.hasText(iface.getSuccessMessagePath()) ? iface.getSuccessMessagePath() : "RETURN[].MESSAGE";
        String typeTag = lastXmlTag(typePath);
        String messageTag = lastXmlTag(messagePath);
        try {
            Document document = parseXmlDocument(responsePayload);
            NodeList typeNodes = document.getElementsByTagName(typeTag);
            List<SapReturnRow> rows = new ArrayList<>();
            for (int i = 0; i < typeNodes.getLength(); i++) {
                Node typeNode = typeNodes.item(i);
                String type = text(typeNode);
                if (!StringUtils.hasText(type)) {
                    continue;
                }
                Element container = nearestElement(typeNode.getParentNode());
                String message = findText(container, messageTag);
                rows.add(new SapReturnRow(
                    type.trim(),
                    findText(container, "ID"),
                    firstText(container, List.of("NUMBER", "NO")),
                    StringUtils.hasText(message) ? message : findText(container, "MESSAGE_V1")
                ));
            }
            return rows;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String formatSapReturnRows(List<SapReturnRow> rows) {
        return rows.stream()
            .limit(8)
            .map(row -> {
                String prefix = "TYPE=" + row.type();
                if (StringUtils.hasText(row.id())) {
                    prefix += " ID=" + row.id();
                }
                if (StringUtils.hasText(row.number())) {
                    prefix += " NO=" + row.number();
                }
                return StringUtils.hasText(row.message()) ? prefix + " " + row.message() : prefix;
            })
            .reduce((left, right) -> left + "；" + right)
            .orElse("SAP RETURN无消息");
    }

    private String resolveResponseMessage(String responsePayload, String messagePath) {
        if (!StringUtils.hasText(messagePath)) {
            return null;
        }
        String value = resolveJsonResponseValue(responsePayload, messagePath);
        if (StringUtils.hasText(value)) {
            return value;
        }
        return resolveXmlFieldValue(responsePayload, messagePath);
    }

    private String resolveJsonResponseValue(String responsePayload, String path) {
        if (!StringUtils.hasText(responsePayload) || !StringUtils.hasText(path)) {
            return null;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(responsePayload);
            JsonNode node = resolveJsonPath(root, path, 0);
            if (node != null && !node.isMissingNode() && !node.isNull()) {
                return node.isContainerNode() ? node.toString() : node.asText();
            }
            JsonNode body = root.path("body");
            if (body.isTextual()) {
                JsonNode bodyRoot = OBJECT_MAPPER.readTree(body.asText());
                String bodyPath = path.startsWith("body.") ? path.substring("body.".length()) : path;
                JsonNode bodyNode = resolveJsonPath(bodyRoot, bodyPath, 0);
                if (bodyNode != null && !bodyNode.isMissingNode() && !bodyNode.isNull()) {
                    return bodyNode.isContainerNode() ? bodyNode.toString() : bodyNode.asText();
                }
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private String resolveXmlFieldValue(String responsePayload, String path) {
        List<String> values = resolveXmlFieldValues(responsePayload, path);
        return values.isEmpty() ? null : values.get(0);
    }

    private List<String> resolveXmlFieldValues(String responsePayload, String path) {
        if (!StringUtils.hasText(responsePayload) || !StringUtils.hasText(path)) {
            return List.of();
        }
        try {
            Document document = parseXmlDocument(responsePayload);
            String tagName = lastXmlTag(path);
            NodeList nodes = document.getElementsByTagName(tagName);
            List<String> values = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                String value = nodes.item(i).getTextContent();
                if (StringUtils.hasText(value)) {
                    values.add(value.trim());
                }
            }
            return values;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private Document parseXmlDocument(String responsePayload) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder()
            .parse(new ByteArrayInputStream(responsePayload.getBytes(StandardCharsets.UTF_8)));
    }

    private String lastXmlTag(String path) {
        String tagName = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return tagName.replace("[]", "");
    }

    private Element nearestElement(Node node) {
        Node current = node;
        while (current != null && current.getNodeType() != Node.ELEMENT_NODE) {
            current = current.getParentNode();
        }
        return (Element) current;
    }

    private String firstText(Element element, List<String> tagNames) {
        for (String tagName : tagNames) {
            String value = findText(element, tagName);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String findText(Element element, String tagName) {
        if (element == null || !StringUtils.hasText(tagName)) {
            return null;
        }
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return text(nodes.item(0));
    }

    private String text(Node node) {
        if (node == null || node.getTextContent() == null) {
            return null;
        }
        String value = node.getTextContent().trim();
        return StringUtils.hasText(value) ? value : null;
    }

    private List<String> splitValues(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split("[,|，]"))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }

    private IntegrationInterface findEnabledInterface(String interfaceCode) {
        if (!StringUtils.hasText(interfaceCode)) return null;
        return interfaceMapper.selectOne(new LambdaQueryWrapper<IntegrationInterface>()
            .eq(IntegrationInterface::getInterfaceCode, interfaceCode)
            .eq(IntegrationInterface::getEnabled, (short) 1)
            .eq(IntegrationInterface::getDeleted, (short) 0)
            .last("LIMIT 1"));
    }

    private IntegrationConnectionConfig findConnection(String connectionCode) {
        if (!StringUtils.hasText(connectionCode)) return null;
        return connectionMapper.selectOne(new LambdaQueryWrapper<IntegrationConnectionConfig>()
            .eq(IntegrationConnectionConfig::getConnectionCode, connectionCode)
            .eq(IntegrationConnectionConfig::getEnabled, (short) 1)
            .eq(IntegrationConnectionConfig::getDeleted, (short) 0)
            .last("LIMIT 1"));
    }

    private SapRfcConfig findSapConfig(String configCode) {
        if (!StringUtils.hasText(configCode)) return null;
        return sapRfcConfigMapper.selectOne(new LambdaQueryWrapper<SapRfcConfig>()
            .eq(SapRfcConfig::getConfigCode, configCode)
            .eq(SapRfcConfig::getEnabled, (short) 1)
            .eq(SapRfcConfig::getDeleted, (short) 0)
            .last("LIMIT 1"));
    }

    private void applyHeaders(HttpRequest.Builder builder, String headerConfig) throws Exception {
        if (!StringUtils.hasText(headerConfig)) return;
        Map<String, Object> headers = OBJECT_MAPPER.readValue(headerConfig, MAP_TYPE);
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if (StringUtils.hasText(entry.getKey()) && entry.getValue() != null) {
                builder.header(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    private void applyAuth(HttpRequest.Builder builder, IntegrationConnectionConfig connection) throws Exception {
        if (!StringUtils.hasText(connection.getAuthType()) || "NONE".equals(connection.getAuthType())) return;
        Map<String, Object> auth = StringUtils.hasText(connection.getAuthConfig())
            ? OBJECT_MAPPER.readValue(connection.getAuthConfig(), MAP_TYPE)
            : Map.of();
        if ("BASIC".equals(connection.getAuthType())) {
            String username = stringValue(auth.get("username"));
            String password = stringValue(auth.get("password"));
            String token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            builder.header("Authorization", "Basic " + token);
        } else if ("BEARER".equals(connection.getAuthType())) {
            builder.header("Authorization", "Bearer " + stringValue(auth.get("token")));
        } else if ("API_KEY".equals(connection.getAuthType())) {
            builder.header(StringUtils.hasText(stringValue(auth.get("headerName"))) ? stringValue(auth.get("headerName")) : "X-API-Key",
                stringValue(auth.get("apiKey")));
        }
    }

    private String joinUrl(String baseUrl, String path) {
        if (!StringUtils.hasText(path)) return baseUrl;
        if (baseUrl.endsWith("/") && path.startsWith("/")) return baseUrl + path.substring(1);
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) return baseUrl + "/" + path;
        return baseUrl + path;
    }

    private String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record ExecuteResult(boolean success, String responsePayload, String errorMessage, boolean retryable) {
        static ExecuteResult success(String responsePayload) {
            return new ExecuteResult(true, responsePayload, null, false);
        }
        static ExecuteResult failed(String errorMessage, boolean retryable) {
            return new ExecuteResult(false, null, errorMessage, retryable);
        }
        static ExecuteResult failed(String errorMessage, String responsePayload, boolean retryable) {
            return new ExecuteResult(false, responsePayload, errorMessage, retryable);
        }
    }

    private record ResolvedValue(Object value, boolean usedDefault, boolean missing) {}

    private record ResponseDecision(boolean success, String message) {}

    private record SapReturnRow(String type, String id, String number, String message) {}

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
        entity.setEndpointPath(safeUrlText(dto.endpointPath()));
        entity.setContentType(InputSanitizer.safeText(dto.contentType()));
        entity.setTriggerMode(StringUtils.hasText(dto.triggerMode()) ? InputSanitizer.safeText(dto.triggerMode()) : "MANUAL");
        entity.setTriggerResource(InputSanitizer.safeText(dto.triggerResource()));
        entity.setTriggerConditionJson(StringUtils.hasText(dto.triggerConditionJson()) ? InputSanitizer.rejectUnsafeHtml(dto.triggerConditionJson()) : null);
        entity.setEnabled(dto.enabled() == null ? (short) 1 : dto.enabled());
        entity.setRetryLimit(dto.retryLimit() == null ? 3 : dto.retryLimit());
        entity.setSuccessRuleType(StringUtils.hasText(dto.successRuleType()) ? InputSanitizer.safeText(dto.successRuleType()) : "AUTO");
        entity.setSuccessFieldPath(InputSanitizer.safeText(dto.successFieldPath()));
        entity.setSuccessExpectedValues(InputSanitizer.safeText(dto.successExpectedValues()));
        entity.setFailureExpectedValues(InputSanitizer.safeText(dto.failureExpectedValues()));
        entity.setSuccessMessagePath(InputSanitizer.safeText(dto.successMessagePath()));
        entity.setDescription(InputSanitizer.safeText(dto.description()));
        return entity;
    }

    private void validateInterfaceConfig(IntegrationInterfaceDTO dto) {
        String protocol = StringUtils.hasText(dto.protocol()) ? dto.protocol().trim() : "";
        String successRuleType = StringUtils.hasText(dto.successRuleType()) ? dto.successRuleType().trim() : "AUTO";
        String triggerMode = StringUtils.hasText(dto.triggerMode()) ? dto.triggerMode().trim() : "MANUAL";
        if (!SAP_PROTOCOLS.contains(protocol) && !GENERIC_PROTOCOLS.contains(protocol)) {
            throw new BizException(1001, "不支持的接口协议：" + dto.protocol());
        }
        if (!SUCCESS_RULE_TYPES.contains(successRuleType)) {
            throw new BizException(1001, "不支持的成功判断规则：" + dto.successRuleType());
        }
        if (!TRIGGER_MODES.contains(triggerMode)) {
            throw new BizException(1001, "不支持的触发模式：" + dto.triggerMode());
        }
        if (StringUtils.hasText(dto.triggerConditionJson())) {
            try {
                OBJECT_MAPPER.readTree(dto.triggerConditionJson());
            } catch (Exception ex) {
                throw new BizException(1001, "触发条件JSON格式不正确");
            }
        }
        if (("JSON_FIELD".equals(successRuleType) || "XML_FIELD".equals(successRuleType)) && !StringUtils.hasText(dto.successFieldPath())) {
            throw new BizException(1001, "字段规则需要填写成功判断字段路径");
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
        if (!HTTP_METHODS.contains(dto.httpMethod().trim().toUpperCase())) {
            throw new BizException(1001, "不支持的HTTP方法：" + dto.httpMethod());
        }
        if (dto.endpointPath().contains("://") || dto.endpointPath().startsWith("//")) {
            throw new BizException(1001, "接口路径只能填写相对路径，完整域名请配置在连接Base URL中");
        }
    }

    private IntegrationConnectionConfig toConnection(IntegrationConnectionConfigDTO dto, IntegrationConnectionConfig entity) {
        validateConnectionConfig(dto);
        entity.setConnectionCode(InputSanitizer.safeText(dto.connectionCode()));
        entity.setConnectionName(InputSanitizer.safeText(dto.connectionName()));
        entity.setConnectionType(StringUtils.hasText(dto.connectionType()) ? InputSanitizer.safeText(dto.connectionType()).toUpperCase() : "REST");
        entity.setEnabled(dto.enabled() == null ? (short) 1 : dto.enabled());
        entity.setBaseUrl(safeUrlText(dto.baseUrl()));
        entity.setAuthType(StringUtils.hasText(dto.authType()) ? InputSanitizer.safeText(dto.authType()).toUpperCase() : "NONE");
        entity.setAuthConfig(InputSanitizer.rejectUnsafeHtml(dto.authConfig()));
        entity.setHeaderConfig(InputSanitizer.rejectUnsafeHtml(dto.headerConfig()));
        entity.setTimeoutMs(dto.timeoutMs() == null ? 30000 : dto.timeoutMs());
        entity.setRemark(InputSanitizer.safeText(dto.remark()));
        return entity;
    }

    private String safeUrlText(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return InputSanitizer.rejectUnsafeHtml(value).trim();
    }

    private void validateConnectionConfig(IntegrationConnectionConfigDTO dto) {
        String type = StringUtils.hasText(dto.connectionType()) ? dto.connectionType().trim().toUpperCase() : "REST";
        String authType = StringUtils.hasText(dto.authType()) ? dto.authType().trim().toUpperCase() : "NONE";
        if (!SAP_PROTOCOLS.contains(type) && !GENERIC_PROTOCOLS.contains(type)) {
            throw new BizException(1001, "不支持的连接类型：" + dto.connectionType());
        }
        if (!AUTH_TYPES.contains(authType)) {
            throw new BizException(1001, "不支持的认证方式：" + dto.authType());
        }
        if (HTTP_PROTOCOLS.contains(type)) {
            validateHttpUrl(dto.baseUrl(), "Base URL");
        }
        validateJsonObject(dto.authConfig(), "认证配置");
        validateJsonObject(dto.headerConfig(), "请求头配置");
        if (dto.timeoutMs() != null && (dto.timeoutMs() < 1000 || dto.timeoutMs() > 120000)) {
            throw new BizException(1001, "超时时间需在1000到120000毫秒之间");
        }
    }

    private void validateHttpUrl(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BizException(1001, fieldName + "不能为空");
        }
        URI uri;
        try {
            uri = URI.create(value.trim());
        } catch (Exception ex) {
            throw new BizException(1001, fieldName + "格式不正确");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new BizException(1001, fieldName + "仅支持HTTP或HTTPS");
        }
        if (!StringUtils.hasText(uri.getHost()) || StringUtils.hasText(uri.getUserInfo())) {
            throw new BizException(1001, fieldName + "不能包含用户凭据，且必须填写主机名");
        }
    }

    private void validateJsonObject(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(value);
            if (!node.isObject()) {
                throw new BizException(1001, fieldName + "必须是JSON对象");
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(1001, fieldName + "JSON格式不正确");
        }
    }

    private IntegrationConnectionConfig requireConnection(Long id) {
        IntegrationConnectionConfig entity = connectionMapper.selectById(id);
        if (entity == null || entity.getDeleted() != null && entity.getDeleted() == 1) {
            throw new BizException(1001, "连接配置不存在");
        }
        return entity;
    }

    private IntegrationFieldMapping toMapping(IntegrationFieldMappingDTO dto, IntegrationFieldMapping entity) {
        String mode = StringUtils.hasText(dto.parameterMode()) ? InputSanitizer.safeText(dto.parameterMode()) : "SINGLE";
        boolean tableMode = "TABLE".equals(mode);
        entity.setInterfaceId(dto.interfaceId());
        entity.setParameterMode(mode);
        entity.setParameterGroup(InputSanitizer.safeText(dto.parameterGroup()));
        entity.setMappingDirection(StringUtils.hasText(dto.mappingDirection()) ? InputSanitizer.safeText(dto.mappingDirection()) : "OUTBOUND");
        entity.setSourceModule(StringUtils.hasText(dto.sourceModule()) ? InputSanitizer.safeText(dto.sourceModule()) : "customer");
        entity.setSourceField(StringUtils.hasText(dto.sourceField()) ? InputSanitizer.safeText(dto.sourceField()) : tableMode ? "__TABLE__" : null);
        entity.setSourceFieldLabel(InputSanitizer.safeText(dto.sourceFieldLabel()));
        entity.setTargetField(StringUtils.hasText(dto.targetField()) ? InputSanitizer.safeText(dto.targetField()) : tableMode ? "__TABLE__" : null);
        entity.setTargetFieldLabel(InputSanitizer.safeText(dto.targetFieldLabel()));
        entity.setTableFieldMappings(StringUtils.hasText(dto.tableFieldMappings()) ? InputSanitizer.rejectUnsafeHtml(dto.tableFieldMappings()) : null);
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
        if ("TABLE".equals(mode)) {
            validateTableFieldMappings(dto.tableFieldMappings());
        } else if (!StringUtils.hasText(dto.sourceField()) || !StringUtils.hasText(dto.targetField())) {
            throw new BizException(1001, "请配置CRM字段和接口字段");
        }
    }

    private void validateTableFieldMappings(String tableFieldMappings) {
        if (!StringUtils.hasText(tableFieldMappings)) {
            throw new BizException(1001, "表参数至少需要维护一行表内字段");
        }
        try {
            List<Map<String, Object>> rows = OBJECT_MAPPER.readValue(tableFieldMappings, TABLE_FIELD_LIST_TYPE);
            if (rows.isEmpty()) {
                throw new BizException(1001, "表参数至少需要维护一行表内字段");
            }
            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> row = rows.get(i);
                if (!hasText(row.get("sourceModule"))) {
                    throw new BizException(1001, "第" + (i + 1) + "行表字段需要选择CRM模块");
                }
                if (!hasText(row.get("sourceField")) || !hasText(row.get("targetField"))) {
                    throw new BizException(1001, "第" + (i + 1) + "行表字段需要配置CRM字段和表内字段名");
                }
            }
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BizException(1001, "表字段配置不是合法JSON数组");
        }
    }

    private boolean hasText(Object value) {
        return value != null && StringUtils.hasText(String.valueOf(value));
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

    private record SapCusBusinessKey(Long customerId, Long sapInfoId) {}

    private record HttpMappedPayload(String body, Map<String, String> queryParams, Map<String, String> headers) {}

    private Long currentUserId() {
        return userContext.getCurrentUserId().orElse(1L);
    }
}
