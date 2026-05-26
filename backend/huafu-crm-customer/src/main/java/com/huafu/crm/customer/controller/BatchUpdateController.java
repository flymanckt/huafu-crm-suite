package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.dto.BatchUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crm/v1/batch-update")
@Tag(name = "批量修改", description = "列表批量字段修改")
public class BatchUpdateController {
    private static final Pattern SAFE_PAYLOAD_KEY = Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,63}");
    private final JdbcTemplate jdbcTemplate;

    public BatchUpdateController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PutMapping("/{resource}")
    @Transactional
    @Operation(summary = "批量修改业务列表")
    public Result<Integer> update(@PathVariable String resource, @RequestBody BatchUpdateDTO dto) {
        ResourceDef def = resources().get(resource);
        if (def == null) throw new BizException(1001, "不支持批量修改该列表");
        return Result.ok(updateTable(def, ids(dto), fields(dto)));
    }

    @PutMapping("/module-records/{moduleKey}")
    @Transactional
    @Operation(summary = "批量修改配置化模块台账")
    public Result<Integer> updateModuleRecords(@PathVariable String moduleKey, @RequestBody BatchUpdateDTO dto) {
        return Result.ok(updateModuleTable(moduleKey, ids(dto), fields(dto)));
    }

    private int updateTable(ResourceDef def, List<Long> ids, Map<String, Object> fields) {
        List<String> sets = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            FieldDef field = def.fields().get(entry.getKey());
            if (field == null) throw new BizException(1001, "字段不允许批量修改：" + entry.getKey());
            sets.add(field.column() + " = ?");
            args.add(convert(entry.getValue(), field.type()));
        }
        if (def.updatedColumn() != null && !def.updatedColumn().isBlank()) {
            sets.add(def.updatedColumn() + " = CURRENT_TIMESTAMP");
        }
        String placeholders = placeholders(ids.size());
        args.addAll(ids);
        String sql = "UPDATE " + def.table() + " SET " + String.join(", ", sets)
            + " WHERE id IN (" + placeholders + ")";
        if (def.deletedColumn() != null && !def.deletedColumn().isBlank()) {
            sql += " AND COALESCE(" + def.deletedColumn() + ", 0) = 0";
        }
        return jdbcTemplate.update(sql, args.toArray());
    }

    private int updateModuleTable(String moduleKey, List<Long> ids, Map<String, Object> fields) {
        List<String> sets = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        Map<String, FieldDef> baseFields = Map.of(
            "recordNo", new FieldDef("record_no", FieldType.TEXT),
            "title", new FieldDef("title", FieldType.TEXT),
            "status", new FieldDef("status", FieldType.TEXT),
            "ownerName", new FieldDef("owner_name", FieldType.TEXT),
            "recordDate", new FieldDef("record_date", FieldType.DATE),
            "remark", new FieldDef("remark", FieldType.TEXT)
        );
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            FieldDef base = baseFields.get(key);
            if (base != null) {
                sets.add(base.column() + " = ?");
                args.add(convert(entry.getValue(), base.type()));
            } else {
                String payloadKey = key.startsWith("payload.") ? key.substring("payload.".length()) : key;
                if (!SAFE_PAYLOAD_KEY.matcher(payloadKey).matches()) {
                    throw new BizException(1001, "字段不允许批量修改：" + key);
                }
                sets.add("payload_json = jsonb_set(COALESCE(payload_json::jsonb, '{}'::jsonb), ARRAY[?], to_jsonb(?::text), true)::text");
                args.add(payloadKey);
                args.add(entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            }
        }
        sets.add("updated_time = CURRENT_TIMESTAMP");
        String placeholders = placeholders(ids.size());
        args.add(moduleKey);
        args.addAll(ids);
        String sql = "UPDATE crm_module_record SET " + String.join(", ", sets)
            + " WHERE module_key = ? AND id IN (" + placeholders + ") AND COALESCE(deleted, 0) = 0";
        return jdbcTemplate.update(sql, args.toArray());
    }

    private List<Long> ids(BatchUpdateDTO dto) {
        if (dto == null || dto.ids() == null || dto.ids().isEmpty()) {
            throw new BizException(1001, "请选择需要修改的数据");
        }
        return dto.ids().stream().map(value -> {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (Exception ex) {
                throw new BizException(1001, "数据ID不正确");
            }
        }).toList();
    }

    private Map<String, Object> fields(BatchUpdateDTO dto) {
        if (dto == null || dto.fields() == null || dto.fields().isEmpty()) {
            throw new BizException(1001, "请选择需要修改的字段");
        }
        if (dto.fields().size() > 10) {
            throw new BizException(1001, "一次最多批量修改10个字段");
        }
        return dto.fields();
    }

    private String placeholders(int size) {
        return String.join(",", java.util.Collections.nCopies(size, "?"));
    }

    private Object convert(Object value, FieldType type) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        String text = String.valueOf(value).trim();
        try {
            return switch (type) {
                case TEXT -> text;
                case INT -> Integer.valueOf(text);
                case LONG -> Long.valueOf(text);
                case DECIMAL -> new BigDecimal(text);
                case DATE -> Date.valueOf(LocalDate.parse(text));
            };
        } catch (Exception ex) {
            throw new BizException(1001, "字段值格式不正确：" + text);
        }
    }

    private Map<String, ResourceDef> resources() {
        Map<String, ResourceDef> map = new LinkedHashMap<>();
        map.put("customer", resource("crm_customer", Map.ofEntries(
            text("customerName", "customer_name"), text("customerShortName", "customer_short_name"),
            integer("type", "type"), integer("level", "level"), integer("status", "status"),
            text("province", "province"), text("city", "city"), text("district", "district"),
            text("customerCategory", "customer_category"), text("customerSegment", "customer_segment"),
            integer("businessType", "business_type"), text("customerSource", "customer_source"),
            text("mainCustomerGroup", "main_customer_group"),
            integer("customerStage", "customer_stage"), integer("riskLevel", "risk_level"),
            text("salesMerchandiser", "sales_merchandiser"),
            longField("ownerUserId", "owner_user_id"), longField("ownerDeptId", "owner_dept_id"),
            text("remark", "remark")
        )));
        map.put("lead", resource("crm_lead", Map.ofEntries(
            integer("leadType", "lead_type"), integer("status", "status"), integer("source", "source"),
            longField("handlerUserId", "handler_user_id"), text("customerName", "customer_name"),
            text("productName", "product_name"), text("competitorName", "competitor_name"), text("remark", "remark")
        )));
        map.put("opportunity", resource("crm_opportunity", Map.ofEntries(
            integer("stage", "stage"), longField("handlerUserId", "handler_user_id"),
            text("productName", "product_name"), decimal("estimatedAmount", "estimated_amount"),
            integer("winProbability", "win_probability"), date("expectedCloseDate", "expected_close_date"),
            text("remark", "remark")
        )));
        map.put("lost-order", resource("crm_lost_order", Map.ofEntries(
            integer("lostType", "lost_type"), integer("recoveryPossible", "recovery_possible"),
            longField("handlerUserId", "handler_user_id"), date("followUpDate", "follow_up_date"),
            text("reasonDetail", "reason_detail"), text("competitorName", "competitor_name")
        )));
        map.put("visit-record", resource("crm_visit_record", Map.ofEntries(
            integer("visitType", "visit_type"), text("visitPurpose", "visit_purpose"),
            text("visitContent", "visit_content"), date("nextVisitPlan", "next_visit_plan"),
            integer("isNewCustomer", "is_new_customer"), text("remark", "remark")
        )));
        map.put("daily-report", resource("crm_daily_report", Map.ofEntries(
            integer("parseStatus", "parse_status"), date("reportDate", "report_date"),
            text("parseError", "parse_error")
        )));
        map.put("performance", resource("crm_performance", Map.ofEntries(
            integer("visitTarget", "visit_target"), integer("reportTarget", "report_target"),
            integer("newCustomerTarget", "new_customer_target"), integer("grade", "grade")
        )));
        map.put("target", resource("crm_target", Map.ofEntries(
            integer("targetYear", "target_year"), integer("targetMonth", "target_month"),
            longField("deptId", "dept_id"), longField("userId", "user_id"),
            text("productCategory", "product_category"), integer("metricType", "metric_type"),
            decimal("targetValue", "target_value")
        )));
        map.put("quote", resource("crm_quote", Map.ofEntries(
            integer("status", "status"), date("quoteDate", "quote_date"), date("validDate", "valid_date"),
            decimal("discountRate", "discount_rate"), text("paymentTerms", "payment_terms"),
            text("deliveryTerms", "delivery_terms"), text("remark", "remark")
        )));
        map.put("user", resource("crm_user", Map.ofEntries(
            integer("status", "status"), longField("deptId", "dept_id"),
            text("post", "post"), text("phone", "phone"), text("email", "email")
        )));
        map.put("role", resource("crm_role", Map.ofEntries(
            text("description", "description")
        )));
        map.put("system-config", resource("sys_config", Map.ofEntries(
            text("configGroup", "config_group"),
            text("type", "type"), text("description", "description")
        )));
        map.put("dict-type", resource("sys_dict_type", "updated_at", Map.ofEntries(
            text("dictName", "dict_name"), text("dictType", "dict_type"),
            text("description", "description"), integer("sortOrder", "sort_order"),
            integer("status", "status")
        )));
        map.put("dict-item", resource("sys_dict_item", "updated_at", Map.ofEntries(
            text("itemName", "item_name"), text("itemValue", "item_value"),
            integer("showCode", "show_code"), text("color", "color"), integer("sortOrder", "sort_order"),
            integer("status", "status"), text("description", "description")
        )));
        map.put("wecom-message-log", resource("crm_wecom_message_log", null, Map.ofEntries(
            integer("parseStatus", "parse_status"), text("content", "content")
        )));
        return map;
    }

    private ResourceDef resource(String table, Map<String, FieldDef> fields) {
        return new ResourceDef(table, "updated_time", "deleted", fields);
    }

    private ResourceDef resource(String table, String updatedColumn, Map<String, FieldDef> fields) {
        return new ResourceDef(table, updatedColumn, "deleted", fields);
    }

    private Map.Entry<String, FieldDef> text(String key, String column) {
        return Map.entry(key, new FieldDef(column, FieldType.TEXT));
    }

    private Map.Entry<String, FieldDef> integer(String key, String column) {
        return Map.entry(key, new FieldDef(column, FieldType.INT));
    }

    private Map.Entry<String, FieldDef> longField(String key, String column) {
        return Map.entry(key, new FieldDef(column, FieldType.LONG));
    }

    private Map.Entry<String, FieldDef> decimal(String key, String column) {
        return Map.entry(key, new FieldDef(column, FieldType.DECIMAL));
    }

    private Map.Entry<String, FieldDef> date(String key, String column) {
        return Map.entry(key, new FieldDef(column, FieldType.DATE));
    }

    private record ResourceDef(String table, String updatedColumn, String deletedColumn, Map<String, FieldDef> fields) {}
    private record FieldDef(String column, FieldType type) {}
    private enum FieldType { TEXT, INT, LONG, DECIMAL, DATE }
}
