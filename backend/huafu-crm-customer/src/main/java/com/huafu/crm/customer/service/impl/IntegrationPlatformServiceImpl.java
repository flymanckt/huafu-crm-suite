package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class IntegrationPlatformServiceImpl implements IntegrationPlatformService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> TABLE_FIELD_LIST_TYPE = new TypeReference<>() {};
    private static final Set<String> SAP_PROTOCOLS = Set.of("SAP_RFC", "SAP_ODATA", "SAP_IDOC");
    private static final Set<String> GENERIC_PROTOCOLS = Set.of("REST", "SOAP", "WEBHOOK", "SFTP", "FTP", "DATABASE", "KAFKA", "RABBITMQ", "CUSTOM");
    private static final Set<String> PARAMETER_MODES = Set.of("SINGLE", "TABLE");
    private static final Set<String> MAPPING_DIRECTIONS = Set.of("OUTBOUND", "INBOUND", "BIDIRECTIONAL");
    private static final Set<String> HTTP_PROTOCOLS = Set.of("REST", "SOAP", "WEBHOOK", "SAP_ODATA");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private static final Map<String, Properties> JCO_DESTINATION_PROPS = new ConcurrentHashMap<>();
    private static volatile boolean jcoProviderRegistered = false;

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
        String protocol = iface.getProtocol();
        if (HTTP_PROTOCOLS.contains(protocol)) {
            return executeHttp(iface, log);
        }
        if ("SAP_RFC".equals(protocol)) {
            return executeSapRfc(iface, log);
        }
        return ExecuteResult.failed("接口协议暂未接入执行器：" + protocol, false);
    }

    private ExecuteResult executeHttp(IntegrationInterface iface, IntegrationLog log) {
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
            String url = joinUrl(connection.getBaseUrl(), iface.getEndpointPath());
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(connection.getTimeoutMs() == null ? 30000 : connection.getTimeoutMs()))
                .header("Content-Type", contentType);
            applyHeaders(builder, connection.getHeaderConfig());
            applyAuth(builder, connection);
            if ("GET".equals(method)) {
                builder.GET();
            } else {
                builder.method(method, HttpRequest.BodyPublishers.ofString(log.getRequestPayload() == null ? "" : log.getRequestPayload(), StandardCharsets.UTF_8));
            }
            HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connection.getTimeoutMs() == null ? 30000 : connection.getTimeoutMs()))
                .build()
                .send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            boolean ok = response.statusCode() >= 200 && response.statusCode() < 300;
            String responsePayload = toJson(Map.of("statusCode", response.statusCode(), "body", response.body()));
            return ok
                ? ExecuteResult.success(responsePayload)
                : ExecuteResult.failed("HTTP调用失败，状态码：" + response.statusCode(), responsePayload, response.statusCode() >= 500);
        } catch (Exception ex) {
            return ExecuteResult.failed("HTTP调用异常：" + ex.getMessage(), true);
        }
    }

    private ExecuteResult executeSapRfc(IntegrationInterface iface, IntegrationLog log) {
        SapRfcConfig cfg = findSapConfig(iface.getConnectionCode());
        if (cfg == null) {
            return ExecuteResult.failed("SAP RFC配置不存在或未启用：" + iface.getConnectionCode(), false);
        }
        if (!StringUtils.hasText(iface.getSapFunctionName())) {
            return ExecuteResult.failed("接口定义缺少SAP RFC/BAPI函数名", false);
        }
        try {
            Class.forName("com.sap.conn.jco.JCoDestinationManager");
        } catch (ClassNotFoundException ex) {
            return ExecuteResult.failed("当前运行环境未安装SAP JCo执行器，无法真实推送SAP RFC。请将sapjco3.jar和本机库加入customer服务运行时classpath，或改用SAP OData/HTTP网关接口。", false);
        }
        try {
            Object function = prepareSapFunction(cfg, iface);
            applySapMappings(function, iface, log);
            Object destination = getSapDestination(cfg);
            function.getClass().getMethod("execute", Class.forName("com.sap.conn.jco.JCoDestination")).invoke(function, destination);
            Object response = invokeNoArg(function, "toXML");
            return ExecuteResult.success(response == null ? "SAP RFC执行成功" : String.valueOf(response));
        } catch (Exception ex) {
            return ExecuteResult.failed("SAP RFC执行异常：" + rootMessage(ex), true);
        }
    }

    private Object prepareSapFunction(SapRfcConfig cfg, IntegrationInterface iface) throws Exception {
        Object destination = getSapDestination(cfg);
        Object repository = invokeNoArg(destination, "getRepository");
        Object function = repository.getClass().getMethod("getFunction", String.class).invoke(repository, iface.getSapFunctionName());
        if (function == null) {
            throw new IllegalStateException("SAP函数不存在：" + iface.getSapFunctionName());
        }
        return function;
    }

    private Object getSapDestination(SapRfcConfig cfg) throws Exception {
        registerJcoProviderIfNecessary();
        String destinationName = "HUAFU_CRM_" + cfg.getConfigCode();
        JCO_DESTINATION_PROPS.put(destinationName, buildJcoProperties(cfg));
        Class<?> managerClass = Class.forName("com.sap.conn.jco.JCoDestinationManager");
        return managerClass.getMethod("getDestination", String.class).invoke(null, destinationName);
    }

    private void registerJcoProviderIfNecessary() throws Exception {
        if (jcoProviderRegistered) return;
        synchronized (IntegrationPlatformServiceImpl.class) {
            if (jcoProviderRegistered) return;
            Class<?> providerInterface = Class.forName("com.sap.conn.jco.ext.DestinationDataProvider");
            Object provider = Proxy.newProxyInstance(
                providerInterface.getClassLoader(),
                new Class<?>[] { providerInterface },
                new JcoDestinationProviderInvocationHandler());
            Class<?> environmentClass = Class.forName("com.sap.conn.jco.ext.Environment");
            Boolean registered = (Boolean) environmentClass.getMethod("isDestinationDataProviderRegistered").invoke(null);
            if (!registered) {
                environmentClass.getMethod("registerDestinationDataProvider", providerInterface).invoke(null, provider);
            }
            jcoProviderRegistered = true;
        }
    }

    private Properties buildJcoProperties(SapRfcConfig cfg) {
        Properties props = new Properties();
        props.setProperty("jco.client.ashost", cfg.getAppServerHost());
        props.setProperty("jco.client.sysnr", cfg.getSystemNumber());
        props.setProperty("jco.client.client", cfg.getClient());
        props.setProperty("jco.client.user", cfg.getUserName());
        props.setProperty("jco.client.passwd", cfg.getPasswordCipher());
        props.setProperty("jco.client.lang", StringUtils.hasText(cfg.getLanguage()) ? cfg.getLanguage() : "ZH");
        props.setProperty("jco.destination.pool_capacity", String.valueOf(cfg.getPoolCapacity() == null ? 5 : cfg.getPoolCapacity()));
        props.setProperty("jco.destination.peak_limit", String.valueOf(cfg.getPeakLimit() == null ? 10 : cfg.getPeakLimit()));
        return props;
    }

    private void applySapMappings(Object function, IntegrationInterface iface, IntegrationLog log) throws Exception {
        JsonNode root = StringUtils.hasText(log.getRequestPayload()) ? OBJECT_MAPPER.readTree(log.getRequestPayload()) : OBJECT_MAPPER.createObjectNode();
        List<IntegrationFieldMapping> mappings = listMappings(iface.getId()).stream()
            .filter(mapping -> "OUTBOUND".equals(mapping.getMappingDirection()) || "BIDIRECTIONAL".equals(mapping.getMappingDirection()))
            .toList();
        for (IntegrationFieldMapping mapping : mappings) {
            if ("TABLE".equals(mapping.getParameterMode())) {
                applySapTableMapping(function, mapping, root);
            } else {
                Object value = resolveMappingValue(root, mapping.getSourceField(), mapping.getDefaultValue(), 0);
                if (value != null && StringUtils.hasText(mapping.getTargetField())) {
                    setSapParameter(function, mapping.getTargetField(), value);
                }
            }
        }
    }

    private void applySapTableMapping(Object function, IntegrationFieldMapping mapping, JsonNode root) throws Exception {
        if (!StringUtils.hasText(mapping.getParameterGroup()) || !StringUtils.hasText(mapping.getTableFieldMappings())) return;
        Object tableList = invokeNoArg(function, "getTableParameterList");
        if (tableList == null) throw new IllegalStateException("SAP函数没有TABLE参数列表：" + mapping.getParameterGroup());
        Object table = tableList.getClass().getMethod("getTable", String.class).invoke(tableList, mapping.getParameterGroup());
        if (table == null) throw new IllegalStateException("SAP TABLE参数不存在：" + mapping.getParameterGroup());
        List<Map<String, Object>> rows = OBJECT_MAPPER.readValue(mapping.getTableFieldMappings(), TABLE_FIELD_LIST_TYPE);
        int rowCount = Math.max(1, inferTableRowCount(root, rows));
        for (int i = 0; i < rowCount; i++) {
            table.getClass().getMethod("appendRow").invoke(table);
            for (Map<String, Object> row : rows) {
                String targetField = stringValue(row.get("targetField"));
                if (!StringUtils.hasText(targetField)) continue;
                Object value = resolveMappingValue(root, stringValue(row.get("sourceField")), stringValue(row.get("defaultValue")), i);
                if (value != null) {
                    table.getClass().getMethod("setValue", String.class, Object.class).invoke(table, targetField, value);
                }
            }
        }
    }

    private int inferTableRowCount(JsonNode root, List<Map<String, Object>> rows) {
        int count = 0;
        for (Map<String, Object> row : rows) {
            String path = stringValue(row.get("sourceField"));
            int arrayMarker = path == null ? -1 : path.indexOf("[].");
            if (arrayMarker <= 0) continue;
            JsonNode array = root.path(path.substring(0, arrayMarker));
            if (array.isArray()) count = Math.max(count, array.size());
        }
        return count;
    }

    private void setSapParameter(Object function, String targetField, Object value) throws Exception {
        Object importList = invokeNoArg(function, "getImportParameterList");
        if (importList != null) {
            importList.getClass().getMethod("setValue", String.class, Object.class).invoke(importList, targetField, value);
            return;
        }
        Object changingList = invokeNoArg(function, "getChangingParameterList");
        if (changingList != null) {
            changingList.getClass().getMethod("setValue", String.class, Object.class).invoke(changingList, targetField, value);
        }
    }

    private Object resolveMappingValue(JsonNode root, String sourceField, String defaultValue, int rowIndex) {
        if (!StringUtils.hasText(sourceField)) {
            return StringUtils.hasText(defaultValue) ? defaultValue : null;
        }
        JsonNode node = resolveJsonPath(root, sourceField, rowIndex);
        if (node == null || node.isMissingNode() || node.isNull()) {
            return StringUtils.hasText(defaultValue) ? defaultValue : null;
        }
        if (node.isNumber()) return node.numberValue();
        if (node.isBoolean()) return node.booleanValue();
        return node.asText();
    }

    private JsonNode resolveJsonPath(JsonNode root, String sourceField, int rowIndex) {
        String normalized = sourceField;
        if (sourceField.startsWith("customerSapInfo.") || sourceField.startsWith("sapInfo.")) {
            normalized = "sapInfo." + sourceField.substring(sourceField.indexOf('.') + 1);
        } else if (sourceField.startsWith("customerSapOrg.")) {
            normalized = "sapOrgs[]." + sourceField.substring(sourceField.indexOf('.') + 1);
        }
        JsonNode node = root;
        for (String part : normalized.split("\\.")) {
            if (!StringUtils.hasText(part)) continue;
            if (part.endsWith("[]")) {
                String arrayName = part.substring(0, part.length() - 2);
                node = node.path(arrayName);
                if (!node.isArray()) return null;
                node = node.path(Math.min(rowIndex, Math.max(0, node.size() - 1)));
            } else {
                node = node.path(part);
            }
        }
        return node;
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

    private Object invokeNoArg(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private String rootMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
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

    private static class JcoDestinationProviderInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getDestinationProperties" -> JCO_DESTINATION_PROPS.get(String.valueOf(args[0]));
                case "supportsEvents" -> false;
                case "setDestinationDataEventListener" -> null;
                default -> null;
            };
        }
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

    private Long currentUserId() {
        return userContext.getCurrentUserId().orElse(1L);
    }
}
