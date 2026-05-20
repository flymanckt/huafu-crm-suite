package com.huafu.crm.customer.service.sap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.customer.entity.IntegrationFieldMapping;
import com.huafu.crm.customer.entity.IntegrationInterface;
import com.huafu.crm.customer.entity.IntegrationLog;
import com.huafu.crm.customer.entity.SapRfcConfig;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SapJcoReflectionService implements SapJcoService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> TABLE_FIELD_LIST_TYPE = new TypeReference<>() {};
    private static final Map<String, Properties> JCO_DESTINATION_PROPS = new ConcurrentHashMap<>();
    private static volatile boolean jcoProviderRegistered = false;

    @Override
    public boolean isAvailable() {
        try {
            Class.forName("com.sap.conn.jco.JCoDestinationManager");
            Class.forName("com.sap.conn.jco.ext.DestinationDataProvider");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    @Override
    public String availabilityMessage() {
        if (isAvailable()) {
            return "SAP JCo运行库已加载";
        }
        return "当前运行环境未加载SAP JCo运行库。请将sapjco3.jar和本机库放入backend/lib，或通过SAP_JCO_LIB_DIR指定目录后重启customer服务。";
    }

    @Override
    public String testConnection(SapRfcConfig config) {
        if (!isAvailable()) {
            throw new IllegalStateException(availabilityMessage());
        }
        try {
            Object destination = getSapDestination(config);
            destination.getClass().getMethod("ping").invoke(destination);
            return "SAP JCo连接成功";
        } catch (Exception ex) {
            throw new IllegalStateException("SAP JCo连接失败：" + rootMessage(ex), ex);
        }
    }

    @Override
    public SapJcoResult execute(
        SapRfcConfig config,
        IntegrationInterface integrationInterface,
        IntegrationLog log,
        List<IntegrationFieldMapping> mappings
    ) {
        if (!isAvailable()) {
            return SapJcoResult.failed(availabilityMessage(), false);
        }
        if (!StringUtils.hasText(integrationInterface.getSapFunctionName())) {
            return SapJcoResult.failed("接口定义缺少SAP RFC/BAPI函数名", false);
        }
        try {
            Object function = prepareSapFunction(config, integrationInterface);
            applySapMappings(function, log, mappings);
            Object destination = getSapDestination(config);
            function.getClass()
                .getMethod("execute", Class.forName("com.sap.conn.jco.JCoDestination"))
                .invoke(function, destination);
            Object response = invokeNoArg(function, "toXML");
            return SapJcoResult.success(response == null ? "SAP RFC执行成功" : String.valueOf(response));
        } catch (Exception ex) {
            return SapJcoResult.failed("SAP RFC执行异常：" + rootMessage(ex), true);
        }
    }

    private Object prepareSapFunction(SapRfcConfig config, IntegrationInterface integrationInterface) throws Exception {
        Object destination = getSapDestination(config);
        Object repository = invokeNoArg(destination, "getRepository");
        Object function = repository.getClass().getMethod("getFunction", String.class)
            .invoke(repository, integrationInterface.getSapFunctionName());
        if (function == null) {
            throw new IllegalStateException("SAP函数不存在：" + integrationInterface.getSapFunctionName());
        }
        return function;
    }

    private Object getSapDestination(SapRfcConfig config) throws Exception {
        registerJcoProviderIfNecessary();
        String destinationName = "HUAFU_CRM_" + config.getConfigCode();
        JCO_DESTINATION_PROPS.put(destinationName, buildJcoProperties(config));
        Class<?> managerClass = Class.forName("com.sap.conn.jco.JCoDestinationManager");
        return managerClass.getMethod("getDestination", String.class).invoke(null, destinationName);
    }

    private void registerJcoProviderIfNecessary() throws Exception {
        if (jcoProviderRegistered) {
            return;
        }
        synchronized (SapJcoReflectionService.class) {
            if (jcoProviderRegistered) {
                return;
            }
            Class<?> providerInterface = Class.forName("com.sap.conn.jco.ext.DestinationDataProvider");
            Object provider = Proxy.newProxyInstance(
                providerInterface.getClassLoader(),
                new Class<?>[] { providerInterface },
                new JcoDestinationProviderInvocationHandler()
            );
            Class<?> environmentClass = Class.forName("com.sap.conn.jco.ext.Environment");
            Boolean registered = (Boolean) environmentClass.getMethod("isDestinationDataProviderRegistered").invoke(null);
            if (!registered) {
                environmentClass.getMethod("registerDestinationDataProvider", providerInterface).invoke(null, provider);
            }
            jcoProviderRegistered = true;
        }
    }

    private Properties buildJcoProperties(SapRfcConfig config) {
        Properties props = new Properties();
        props.setProperty("jco.client.ashost", config.getAppServerHost());
        props.setProperty("jco.client.sysnr", config.getSystemNumber());
        props.setProperty("jco.client.client", config.getClient());
        props.setProperty("jco.client.user", config.getUserName());
        props.setProperty("jco.client.passwd", config.getPasswordCipher());
        props.setProperty("jco.client.lang", StringUtils.hasText(config.getLanguage()) ? config.getLanguage() : "ZH");
        props.setProperty("jco.destination.pool_capacity", String.valueOf(config.getPoolCapacity() == null ? 5 : config.getPoolCapacity()));
        props.setProperty("jco.destination.peak_limit", String.valueOf(config.getPeakLimit() == null ? 10 : config.getPeakLimit()));
        return props;
    }

    private void applySapMappings(Object function, IntegrationLog log, List<IntegrationFieldMapping> mappings) throws Exception {
        JsonNode root = StringUtils.hasText(log.getRequestPayload())
            ? OBJECT_MAPPER.readTree(log.getRequestPayload())
            : OBJECT_MAPPER.createObjectNode();
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
        if (!StringUtils.hasText(mapping.getParameterGroup()) || !StringUtils.hasText(mapping.getTableFieldMappings())) {
            return;
        }
        Object tableList = invokeNoArg(function, "getTableParameterList");
        if (tableList == null) {
            throw new IllegalStateException("SAP函数没有TABLE参数列表：" + mapping.getParameterGroup());
        }
        Object table = tableList.getClass().getMethod("getTable", String.class).invoke(tableList, mapping.getParameterGroup());
        if (table == null) {
            throw new IllegalStateException("SAP TABLE参数不存在：" + mapping.getParameterGroup());
        }
        List<Map<String, Object>> rows = OBJECT_MAPPER.readValue(mapping.getTableFieldMappings(), TABLE_FIELD_LIST_TYPE);
        int rowCount = Math.max(1, inferTableRowCount(root, rows));
        for (int i = 0; i < rowCount; i++) {
            table.getClass().getMethod("appendRow").invoke(table);
            for (Map<String, Object> row : rows) {
                String targetField = stringValue(row.get("targetField"));
                if (!StringUtils.hasText(targetField)) {
                    continue;
                }
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
            if (arrayMarker <= 0) {
                continue;
            }
            JsonNode array = root.path(path.substring(0, arrayMarker));
            if (array.isArray()) {
                count = Math.max(count, array.size());
            }
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
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
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

    private Object invokeNoArg(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private String rootMessage(Exception ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
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
}
