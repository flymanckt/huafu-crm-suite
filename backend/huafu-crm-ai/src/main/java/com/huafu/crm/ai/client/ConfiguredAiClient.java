package com.huafu.crm.ai.client;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.ai.model.DailyReportAiResult;
import com.huafu.crm.common.exception.BizException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ConfiguredAiClient implements AiClient {
    private static final int DEFAULT_AI_TIMEOUT_MS = 120_000;
    private static final int DEFAULT_AI_CONNECT_TIMEOUT_MS = 30_000;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

    public ConfiguredAiClient(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    @Transactional
    public DailyReportAiResult parseDailyReport(String text) {
        return parseDailyReport(text, null);
    }

    @Override
    @Transactional
    public DailyReportAiResult parseDailyReport(String text, Long dailyReportId) {
        String apiKey = config("ai.api_key");
        String baseUrl = config("ai.base_url");
        String model = config("ai.model");
        String protocol = config("ai.protocol");
        int timeoutMs = intConfig("ai.timeout_ms", DEFAULT_AI_TIMEOUT_MS);
        if (blank(apiKey) || blank(baseUrl) || blank(model)) {
            throw new BizException(1001, "请先在系统管理-外围系统配置-AI服务中配置 ai.api_key、ai.base_url、ai.model");
        }

        String content = text == null ? "" : text.trim();
        if (content.isEmpty()) {
            throw new BizException(1001, "请输入需要解析的日报内容");
        }

        String answer = "ANTHROPIC_MESSAGES".equalsIgnoreCase(protocol)
                ? requestAnthropicMessage(messagesUrl(baseUrl), apiKey, model, content, timeoutMs)
                : requestChatCompletion(chatCompletionUrl(baseUrl), apiKey, model, content, timeoutMs);
        Map<String, Object> parsed = parseAnswer(answer);
        enrichWithMasterData(parsed, content);
        persistParsedDailyReport(content, parsed, dailyReportId);
        return toResult(content, parsed);
    }

    private String requestChatCompletion(String url, String apiKey, String model, String text, int timeoutMs) {
        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "temperature", 0.1,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt()),
                            Map.of("role", "user", "content", text)
                    )
            );
            String raw = restClient(timeoutMs)
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(String.class);
            JsonNode response = objectMapper.readTree(raw);
            String answer = response.path("choices").path(0).path("message").path("content").asText("");
            if (answer.isBlank()) {
                throw new IllegalStateException("模型未返回文本内容");
            }
            return answer;
        } catch (Exception ex) {
            throw new BizException(1001, "AI解析调用失败：" + aiCallErrorMessage(ex, timeoutMs));
        }
    }

    private String requestAnthropicMessage(String url, String apiKey, String model, String text, int timeoutMs) {
        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "max_tokens", 4096,
                    "temperature", 0.1,
                    "system", systemPrompt(),
                    "messages", List.of(Map.of("role", "user", "content", text))
            );
            String raw = restClient(timeoutMs)
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .body(request)
                    .retrieve()
                    .body(String.class);
            JsonNode response = objectMapper.readTree(raw);
            String answer = response.path("content").path(0).path("text").asText("");
            if (answer.isBlank()) {
                throw new IllegalStateException("模型未返回文本内容");
            }
            return answer;
        } catch (Exception ex) {
            throw new BizException(1001, "AI解析调用失败：" + aiCallErrorMessage(ex, timeoutMs));
        }
    }

    private RestClient restClient(int timeoutMs) {
        int readTimeout = positiveOrDefault(timeoutMs, DEFAULT_AI_TIMEOUT_MS);
        int connectTimeout = Math.min(readTimeout, DEFAULT_AI_CONNECT_TIMEOUT_MS);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return restClientBuilder.clone()
                .requestFactory(factory)
                .build();
    }

    private String aiCallErrorMessage(Exception ex, int timeoutMs) {
        Throwable root = rootCause(ex);
        if (root instanceof SocketTimeoutException || String.valueOf(root.getMessage()).toLowerCase().contains("timed out")) {
            return "调用AI服务超时，当前超时时间为 " + timeoutMs + "ms，可在外围系统配置中调整 ai.timeout_ms";
        }
        return ex.getMessage();
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private Map<String, Object> parseAnswer(String answer) {
        String json = extractJson(answer);
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new BizException(1001, "AI返回内容不是有效JSON，请检查模型配置或重试");
        }
    }

    private void enrichWithMasterData(Map<String, Object> parsed, String originalText) {
        List<CustomerCandidate> customers = loadCustomers();

        forEachRow(parsed.get("商机列表"), row -> {
            enrichCustomer(row, customers, originalText, "客户名称", "客户", "客户/品牌");
            normalizeProductText(row);
        });
        forEachRow(parsed.get("商情列表"), row -> {
            enrichCustomer(row, customers, originalText, "客户名称", "客户", "客户/品牌");
            normalizeProductText(row);
        });
        forEachRow(parsed.get("丢单记录"), row -> {
            enrichCustomer(row, customers, originalText, "客户名称", "客户", "客户/品牌");
            normalizeProductText(row);
        });
        withMap(parsed.get("今日拜访"), row -> {
            if (hasAnyText(row, "客户名称", "客户", "拜访客户", "拜访类型", "拜访成效")) {
                enrichCustomer(row, customers, originalText, "客户名称", "客户", "拜访客户");
            }
        });
        withMap(parsed.get("明日计划"), row -> {
            if (hasAnyText(row, "计划拜访客户", "客户名称", "客户", "计划事项")) {
                enrichCustomer(row, customers, originalText, "计划拜访客户", "客户名称", "客户");
            }
        });
    }

    private void persistParsedDailyReport(String originalText, Map<String, Object> parsed, Long existingReportId) {
        List<?> opportunities = list(parsed.get("商机列表"));
        List<?> intelligence = list(parsed.get("商情列表"));
        List<?> lostOrders = list(parsed.get("丢单记录"));

        Long reportId = existingReportId == null
                ? insertDailyReport(originalText, toJson(parsed), opportunities.size(), intelligence.size(), lostOrders.size())
                : updateExistingDailyReport(existingReportId, originalText, toJson(parsed), opportunities.size(), intelligence.size(), lostOrders.size());
        List<String> opportunityIds = new ArrayList<>();
        forEachRow(parsed.get("商机列表"), row -> opportunityIds.add(String.valueOf(insertOpportunity(originalText, row))));

        List<String> leadIds = new ArrayList<>();
        forEachRow(parsed.get("商情列表"), row -> leadIds.add(String.valueOf(insertMarketLead(originalText, row))));

        List<String> lostOrderIds = new ArrayList<>();
        forEachRow(parsed.get("丢单记录"), row -> lostOrderIds.add(String.valueOf(insertLostOrder(originalText, row))));

        List<String> visitIds = new ArrayList<>();
        withMap(parsed.get("今日拜访"), row -> {
            if (hasAnyText(row, "客户名称", "拜访类型", "拜访成效")) {
                visitIds.add(String.valueOf(insertVisitRecord(row, LocalDate.now(), "今日拜访")));
            }
        });
        withMap(parsed.get("明日计划"), row -> {
            if (hasAnyText(row, "计划拜访客户", "计划事项")) {
                visitIds.add(String.valueOf(insertPlanVisitRecord(row, LocalDate.now().plusDays(1))));
            }
        });

        Map<String, Object> writeBack = new LinkedHashMap<>();
        writeBack.put("日报ID", String.valueOf(reportId));
        writeBack.put("商机ID列表", opportunityIds);
        writeBack.put("商情ID列表", leadIds);
        writeBack.put("丢单ID列表", lostOrderIds);
        writeBack.put("拜访记录ID列表", visitIds);
        parsed.put("写入结果", writeBack);
        updateDailyReportParsedJson(reportId, parsed);
    }

    private Long updateExistingDailyReport(Long reportId, String originalText, String parsedJson, int opportunityCount, int intelligenceCount, int lostOrderCount) {
        OffsetDateTime now = OffsetDateTime.now();
        int updated = jdbcTemplate.update("""
                UPDATE crm_daily_report
                SET content_text = COALESCE(NULLIF(?, ''), content_text),
                    parsed_json = CAST(? AS jsonb),
                    opportunity_count = ?,
                    market_intelligence_count = ?,
                    lost_order_count = ?,
                    parse_status = 2,
                    parse_error = NULL,
                    parse_time = ?,
                    updated_by = 'AI',
                    updated_time = ?
                WHERE id = ?
                """,
                originalText, parsedJson, opportunityCount, intelligenceCount, lostOrderCount, now, now, reportId);
        if (updated == 0) {
            throw new BizException(1001, "日报记录不存在，无法回写AI解析结果：" + reportId);
        }
        return reportId;
    }

    private Long insertDailyReport(String originalText, String parsedJson, int opportunityCount, int intelligenceCount, int lostOrderCount) {
        OffsetDateTime now = OffsetDateTime.now();
        return jdbcTemplate.queryForObject("""
                INSERT INTO crm_daily_report
                (id, report_no, user_id, report_date, content_text, parse_status, parsed_json,
                 opportunity_count, market_intelligence_count, lost_order_count, parse_time,
                 created_by, created_time, updated_by, updated_time, deleted, tenant_id, version)
                VALUES (?, ?, ?, ?, ?, 2, CAST(? AS jsonb), ?, ?, ?, ?, 'AI', ?, 'AI', ?, 0, 1, 0)
                RETURNING id
                """,
                Long.class,
                nextId(), generatedNo("DR"), defaultUserId(), LocalDate.now(), originalText, parsedJson,
                opportunityCount, intelligenceCount, lostOrderCount, now, now, now);
    }

    private void updateDailyReportParsedJson(Long reportId, Map<String, Object> parsed) {
        jdbcTemplate.update(
                "UPDATE crm_daily_report SET parsed_json = CAST(? AS jsonb), updated_time = ? WHERE id = ?",
                toJson(parsed), OffsetDateTime.now(), reportId);
    }

    private Long insertOpportunity(String originalText, Map<String, Object> row) {
        Long id = nextId();
        OffsetDateTime now = OffsetDateTime.now();
        String customerName = firstText(row, "客户匹配名称", "客户名称", "客户");
        String productName = productText(row);
        String name = nonBlank(customerName, "未知客户") + " - " + nonBlank(productName, "产品需求") + "商机";
        jdbcTemplate.update("""
                INSERT INTO crm_opportunity
                (id, opp_no, opportunity_name, customer_id, handler_user_id, stage, stage_update_time,
                 product_name, estimated_amount, win_probability, remark, raw_text, parsed_json,
                 created_by, created_time, updated_by, updated_time, deleted, tenant_id, version)
                VALUES (?, ?, ?, ?, ?, 1, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), 'AI', ?, 'AI', ?, 0, 1, 0)
                """,
                id, generatedNo("OPP"), name, longValue(row.get("客户主数据ID")), defaultUserId(),
                now, productName, decimalValue(row.get("预估金额(万元)")), probability(firstText(row, "意向度")),
                firstText(row, "跟进要点"), originalText, toJson(row), now, now);
        row.put("商机ID", String.valueOf(id));
        return id;
    }

    private Long insertMarketLead(String originalText, Map<String, Object> row) {
        Long id = nextId();
        OffsetDateTime now = OffsetDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO crm_lead
                (id, lead_no, lead_type, customer_id, customer_name, product_name, competitor_name,
                 competitor_price, competitor_discount, margin_rate, source, creator_user_id, handler_user_id,
                 status, remark, raw_text, parsed_json, created_by, created_time, updated_by, updated_time,
                 deleted, tenant_id, version)
                VALUES (?, ?, 1, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, 1, ?, ?, CAST(? AS jsonb),
                        'AI', ?, 'AI', ?, 0, 1, 0)
                """,
                id, generatedNo("LEAD"), longValue(row.get("客户主数据ID")),
                firstText(row, "客户匹配名称", "客户名称", "客户"),
                productText(row),
                firstText(row, "竞品名称"), decimalValue(row.get("竞品价格")),
                decimalValue(row.get("折扣")), decimalValue(row.get("我方毛利影响")),
                defaultUserId(), defaultUserId(), firstText(row, "说明"), originalText, toJson(row), now, now);
        row.put("商情ID", String.valueOf(id));
        return id;
    }

    private Long insertLostOrder(String originalText, Map<String, Object> row) {
        Long id = nextId();
        OffsetDateTime now = OffsetDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO crm_lost_order
                (id, lost_no, opportunity_id, customer_id, lost_type, competitor_name, competitor_price,
                 our_price, reason_detail, recovery_possible, handler_user_id, raw_text, parsed_json,
                 created_by, created_time, updated_by, updated_time, deleted, tenant_id, version)
                VALUES (?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb),
                        'AI', ?, 'AI', ?, 0, 1, 0)
                """,
                id, generatedNo("LO"), longValue(row.get("客户主数据ID")),
                lostType(firstText(row, "丢单原因")), firstText(row, "竞品名称"),
                decimalValue(row.get("竞品价格")), decimalValue(row.get("我方报价")),
                firstText(row, "丢单原因", "补救建议"), hasText(firstText(row, "补救建议")) ? 1 : null,
                defaultUserId(), originalText, toJson(row), now, now);
        row.put("丢单ID", String.valueOf(id));
        return id;
    }

    private Long insertVisitRecord(Map<String, Object> row, LocalDate visitDate, String sourceLabel) {
        Long id = nextId();
        OffsetDateTime now = OffsetDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO crm_visit_record
                (id, visit_no, user_id, customer_id, customer_name, visit_date, visit_type, visit_purpose,
                 visit_content, is_new_customer, remark, created_by, created_time, updated_by, updated_time,
                 deleted, tenant_id, version)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, 'AI', ?, 'AI', ?, 0, 1, 0)
                """,
                id, generatedNo("VISIT"), defaultUserId(), longValue(row.get("客户主数据ID")),
                firstText(row, "客户匹配名称", "客户名称", "客户", "计划拜访客户"),
                visitDate, visitType(firstText(row, "拜访类型")), sourceLabel,
                firstText(row, "拜访成效", "计划事项"), toJson(row), now, now);
        row.put("拜访记录ID", String.valueOf(id));
        return id;
    }

    private Long insertPlanVisitRecord(Map<String, Object> row, LocalDate planDate) {
        row.putIfAbsent("客户名称", firstText(row, "计划拜访客户"));
        row.putIfAbsent("拜访类型", "计划");
        row.putIfAbsent("拜访成效", firstText(row, "计划事项"));
        return insertVisitRecord(row, planDate, "明日计划");
    }

    private DailyReportAiResult toResult(String originalText, Map<String, Object> parsed) {
        List<?> opportunities = list(parsed.get("商机列表"));
        List<?> intelligence = list(parsed.get("商情列表"));
        List<?> lostOrders = list(parsed.get("丢单记录"));
        List<String> highlights = new ArrayList<>();
        collectHighlight(highlights, opportunities, "商机");
        collectHighlight(highlights, intelligence, "商情");
        collectHighlight(highlights, lostOrders, "丢单");
        if (highlights.isEmpty()) {
            Object summary = parsed.get("摘要");
            if (summary != null && !String.valueOf(summary).isBlank()) highlights.add(String.valueOf(summary));
        }
        return new DailyReportAiResult(originalText, opportunities.size(), intelligence.size(), lostOrders.size(), highlights, parsed);
    }

    private void collectHighlight(List<String> highlights, List<?> rows, String label) {
        for (Object row : rows) {
            if (highlights.size() >= 5) return;
            highlights.add(label + "：" + String.valueOf(row));
        }
    }

    private List<?> list(Object value) {
        return value instanceof List<?> rows ? rows : List.of();
    }

    private List<CustomerCandidate> loadCustomers() {
        return jdbcTemplate.query("""
                SELECT id, customer_code, customer_name, customer_short_name, english_name, sap_customer_code
                FROM crm_customer
                WHERE COALESCE(deleted, 0) = 0 AND COALESCE(customer_name, '') <> ''
                ORDER BY updated_time DESC NULLS LAST, created_time DESC NULLS LAST
                LIMIT 5000
                """,
                (rs, rowNum) -> new CustomerCandidate(
                        rs.getLong("id"),
                        rs.getString("customer_code"),
                        rs.getString("customer_name"),
                        rs.getString("customer_short_name"),
                        rs.getString("english_name"),
                        rs.getString("sap_customer_code")
                ));
    }

    private void enrichCustomer(Map<String, Object> row, List<CustomerCandidate> customers, String originalText, String... keys) {
        String keyword = firstText(row, keys);
        Match<CustomerCandidate> match = bestCustomer(keyword, row == null ? "" : row.toString(), originalText, customers);
        if (match == null) {
            if (hasText(keyword)) row.put("客户匹配状态", "未匹配");
            return;
        }
        CustomerCandidate customer = match.value();
        row.put("客户主数据ID", String.valueOf(customer.id()));
        row.put("客户编码", nonBlank(customer.code(), ""));
        row.put("客户匹配名称", nonBlank(customer.name(), ""));
        row.put("客户匹配状态", "已匹配");
        row.put("客户匹配分数", match.score());
    }

    private void normalizeProductText(Map<String, Object> row) {
        String product = productText(row);
        if (hasText(product)) {
            row.put("产品需求", product);
        }
        row.remove("产品档案ID");
        row.remove("产品编码");
        row.remove("产品匹配名称");
        row.remove("产品类型");
        row.remove("产品匹配状态");
        row.remove("产品匹配分数");
    }

    private String productText(Map<String, Object> row) {
        return firstText(row, "产品需求", "产品名称", "产品", "意向产品", "规格型号");
    }

    private Match<CustomerCandidate> bestCustomer(String keyword, String rowText, String originalText, List<CustomerCandidate> customers) {
        Match<CustomerCandidate> best = bestCustomerByText(keyword, customers);
        if (best != null) {
            return best;
        }
        best = bestCustomerByText(rowText, customers);
        if (best != null) {
            return best;
        }
        List<Match<CustomerCandidate>> reportMatches = new ArrayList<>();
        for (CustomerCandidate customer : customers) {
            int score = maxCustomerScore(originalText, customer.name(), customer.shortName(), customer.englishName(), customer.code(), customer.sapCode());
            if (score >= 80) {
                reportMatches.add(new Match<>(customer, score));
            }
        }
        return reportMatches.size() == 1 ? reportMatches.get(0) : null;
    }

    private Match<CustomerCandidate> bestCustomerByText(String text, List<CustomerCandidate> customers) {
        Match<CustomerCandidate> best = null;
        for (CustomerCandidate customer : customers) {
            int score = maxCustomerScore(text, customer.name(), customer.shortName(), customer.englishName(), customer.code(), customer.sapCode());
            if (score >= 60 && (best == null || score > best.score())) best = new Match<>(customer, score);
        }
        return best;
    }

    private int maxCustomerScore(String keyword, String... aliases) {
        int max = 0;
        for (String alias : aliases) max = Math.max(max, customerScore(keyword, alias));
        return max;
    }

    private int customerScore(String raw, String alias) {
        String left = normalize(raw);
        String right = normalize(alias);
        if (left.isEmpty() || right.isEmpty()) return 0;
        if (left.equals(right)) return 100;
        if (left.contains(right) || right.contains(left)) return Math.min(left.length(), right.length()) >= 3 ? 88 : 70;

        String leftCore = normalizeCustomerCore(raw);
        String rightCore = normalizeCustomerCore(alias);
        if (leftCore.isEmpty() || rightCore.isEmpty()) return 0;
        if (leftCore.equals(rightCore)) return 96;
        if (leftCore.contains(rightCore) || rightCore.contains(leftCore)) return Math.min(leftCore.length(), rightCore.length()) >= 2 ? 86 : 0;
        return score(leftCore, rightCore);
    }

    private int score(String raw, String alias) {
        String left = normalize(raw);
        String right = normalize(alias);
        if (left.isEmpty() || right.isEmpty()) return 0;
        if (left.equals(right)) return 100;
        if (left.contains(right) || right.contains(left)) return Math.min(left.length(), right.length()) >= 3 ? 88 : 70;
        int common = 0;
        String rest = right;
        for (int i = 0; i < left.length(); i++) {
            String ch = left.substring(i, i + 1);
            int idx = rest.indexOf(ch);
            if (idx >= 0) {
                common++;
                rest = rest.substring(0, idx) + rest.substring(idx + 1);
            }
        }
        return (int) Math.round(common * 100.0 / Math.max(left.length(), right.length()));
    }

    private String normalizeCustomerCore(String value) {
        return normalize(value)
                .replace("股份有限公司", "")
                .replace("有限责任公司", "")
                .replace("有限公司", "")
                .replace("集团公司", "")
                .replace("集团", "")
                .replace("公司", "")
                .replace("纺织", "")
                .replace("服饰", "")
                .replace("服装", "")
                .replace("贸易", "")
                .replace("科技", "")
                .replace("实业", "")
                .replace("国际", "");
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.toLowerCase()
                .replaceAll("[\\s\\p{Punct}，。、“”‘’（）()【】\\[\\]{}《》<>：:；;！!？?、/\\\\|_-]+", "");
    }

    private void forEachRow(Object value, RowConsumer consumer) {
        if (!(value instanceof List<?> rows)) return;
        for (Object row : rows) withMap(row, consumer);
    }

    @SuppressWarnings("unchecked")
    private void withMap(Object value, RowConsumer consumer) {
        if (value instanceof Map<?, ?>) {
            consumer.accept((Map<String, Object>) value);
        }
    }

    private boolean hasAnyText(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (hasText(firstText(row, key))) return true;
        }
        return false;
    }

    private String firstText(Map<String, Object> row, String... keys) {
        if (row == null) return "";
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null && hasText(String.valueOf(value))) return String.valueOf(value).trim();
        }
        return "";
    }

    private String nonBlank(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Long longValue(Object value) {
        if (value == null) return null;
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null) return null;
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) return null;
        String cleaned = raw.replaceAll("[^0-9.\\-]", "");
        if (cleaned.isEmpty() || Objects.equals(cleaned, "-")) return null;
        try {
            return new BigDecimal(cleaned);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer probability(String intention) {
        if ("高".equals(intention)) return 80;
        if ("中".equals(intention)) return 50;
        if ("低".equals(intention)) return 25;
        return null;
    }

    private Integer lostType(String reason) {
        if (reason == null) return 5;
        if (reason.contains("价格") || reason.contains("报价")) return 1;
        if (reason.contains("交期") || reason.contains("货期")) return 2;
        if (reason.contains("质量") || reason.contains("品质")) return 3;
        if (reason.contains("现货") || reason.contains("库存")) return 4;
        return 5;
    }

    private Integer visitType(String value) {
        if (value == null) return 2;
        if (value.contains("上门") || value.contains("现场") || value.contains("拜访")) return 1;
        if (value.contains("微信")) return 3;
        if (value.contains("展会")) return 4;
        return 2;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BizException(1001, "AI解析结果序列化失败：" + ex.getMessage());
        }
    }

    private Long nextId() {
        return IdWorker.getId();
    }

    private Long defaultUserId() {
        return 1L;
    }

    private String generatedNo(String prefix) {
        return prefix + "-" + DateTimeFormatter.ofPattern("yyMMddHHmmssSSS").format(LocalDateTime.now())
                + "-" + Math.abs(nextId() % 10000);
    }

    private String config(String key) {
        List<String> values = jdbcTemplate.query(
                "SELECT config_value FROM sys_config WHERE config_key = ? AND deleted = 0 LIMIT 1",
                (rs, rowNum) -> rs.getString(1),
                key);
        return values.isEmpty() ? "" : values.get(0);
    }

    private int intConfig(String key, int fallback) {
        String value = config(key);
        if (blank(value)) return fallback;
        try {
            return positiveOrDefault(Integer.parseInt(value.trim()), fallback);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int positiveOrDefault(int value, int fallback) {
        return value > 0 ? value : fallback;
    }

    private String chatCompletionUrl(String baseUrl) {
        String trimmed = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
        if (trimmed.endsWith("/chat/completions")) return trimmed;
        return trimmed + "/chat/completions";
    }

    private String messagesUrl(String baseUrl) {
        String trimmed = baseUrl == null ? "" : baseUrl.trim().replaceAll("/+$", "");
        if (trimmed.endsWith("/messages")) return trimmed;
        return trimmed + "/messages";
    }

    private String extractJson(String answer) {
        if (answer == null) return "";
        String text = answer.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        return start >= 0 && end > start ? text.substring(start, end + 1) : text;
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String systemPrompt() {
        return """
                你是华孚时尚CRM的销售日报解析助手。请只输出一个JSON对象，不要输出Markdown或解释。
                JSON字段固定为：
                {
                  "摘要": "一句话总结",
                  "商机列表": [
                    {"客户名称":"","产品需求":"","预估金额(万元)":null,"意向度":"高/中/低","跟进要点":""}
                  ],
                  "商情列表": [
                    {"客户名称":"","产品需求":"","竞品名称":"","竞品价格":"","折扣":"","我方毛利影响":"","说明":""}
                  ],
                  "丢单记录": [
                    {"客户名称":"","产品需求":"","丢单原因":"","竞品名称":"","竞品价格":"","我方报价":"","补救建议":""}
                  ],
                  "今日拜访": {"客户名称":"","拜访类型":"","拜访成效":""},
                  "明日计划": {"计划拜访客户":"","计划事项":""}
                }
                客户名称必须从原文中识别客户或品牌，优先输出客户主数据中的正式客户名称；产品需求只从原文中截取产品、品类、规格或需求描述，不要猜测产品档案编码。
                没有对应内容时，列表返回空数组，对象字段返回空字符串。
                """;
    }

    private record CustomerCandidate(Long id, String code, String name, String shortName, String englishName, String sapCode) {}

    private record Match<T>(T value, int score) {}

    @FunctionalInterface
    private interface RowConsumer {
        void accept(Map<String, Object> row);
    }
}
