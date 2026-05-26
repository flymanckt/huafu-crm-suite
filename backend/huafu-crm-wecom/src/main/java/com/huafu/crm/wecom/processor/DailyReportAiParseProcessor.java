package com.huafu.crm.wecom.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class DailyReportAiParseProcessor {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RestClient aiRestClient;

    public DailyReportAiParseProcessor(JdbcTemplate jdbcTemplate,
                                       ObjectMapper objectMapper,
                                       RestClient.Builder restClientBuilder,
                                       @Value("${huafu.internal.ai-base-url:http://localhost:8085}") String aiBaseUrl,
                                       @Value("${huafu.internal.opportunity-base-url:http://localhost:8082}") String opportunityBaseUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.aiRestClient = restClientBuilder.baseUrl(aiBaseUrl).build();
    }

    public void parse(Long dailyReportId) {
        if (dailyReportId == null) {
            return;
        }
        Map<String, Object> report = jdbcTemplate.queryForMap(
                "SELECT id, user_id, COALESCE(content_text, '') AS content_text FROM crm_daily_report WHERE id = ?",
                dailyReportId);
        String userId = String.valueOf(report.get("user_id"));
        String rawContent = String.valueOf(report.get("content_text"));
        Long logId = insertAiLog(dailyReportId, rawContent);
        try {
            DailyReportAiResult result = requestAiParse(dailyReportId, rawContent);
            String responseJson = result.parsedJson();
            jdbcTemplate.update("""
                    UPDATE crm_daily_report
                    SET parsed_json = ?::jsonb,
                        opportunity_count = ?,
                        market_intelligence_count = ?,
                        lost_order_count = ?,
                        parse_status = 2,
                        parse_error = NULL,
                        parse_time = CURRENT_TIMESTAMP,
                        updated_time = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, responseJson, result.opportunityCount(), result.marketIntelligenceCount(), result.lostOrderCount(), dailyReportId);
            jdbcTemplate.update("""
                    UPDATE crm_ai_parse_log
                    SET response_payload = ?, status = 'SUCCESS', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, responseJson, logId);
            writeAckOutbox(userId, dailyReportId, result, rawContent);
        } catch (Exception ex) {
            jdbcTemplate.update("""
                    UPDATE crm_daily_report
                    SET parse_status = 3,
                        parse_error = ?,
                        updated_time = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, abbreviate(ex.getMessage(), 480), dailyReportId);
            jdbcTemplate.update("""
                    UPDATE crm_ai_parse_log SET status = 'FAILED', error_message = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?
                    """, abbreviate(ex.getMessage(), 480), logId);
            throw new IllegalStateException("daily report AI parse failed", ex);
        }
    }

    private Long insertAiLog(Long reportId, String rawContent) {
        jdbcTemplate.update("""
                INSERT INTO crm_ai_parse_log(source_type, source_id, provider, request_payload, status)
                VALUES ('DAILY_REPORT', ?, ?, ?, 'PENDING')
                """, reportId, configValue("ai.provider", "CONFIGURED_AI"), rawContent);
        return jdbcTemplate.queryForObject("SELECT currval(pg_get_serial_sequence('crm_ai_parse_log','id'))", Long.class);
    }

    private DailyReportAiResult requestAiParse(Long dailyReportId, String rawContent) throws JsonProcessingException {
        String response = aiRestClient.post()
                .uri("/api/ai/parse/daily-report")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "text", rawContent == null ? "" : rawContent,
                        "dailyReportId", dailyReportId
                ))
                .retrieve()
                .body(String.class);
        JsonNode root = objectMapper.readTree(response);
        int code = root.path("code").asInt(200);
        if (code != 200) {
            throw new IllegalStateException(root.path("message").asText("AI服务返回失败"));
        }
        JsonNode data = root.path("data");
        if (!data.isObject()) {
            throw new IllegalStateException("AI服务没有返回解析数据");
        }
        JsonNode parsedJson = data.path("parsedJson");
        String parsedJsonText = parsedJson.isObject()
                ? objectMapper.writeValueAsString(parsedJson)
                : objectMapper.writeValueAsString(Map.of(
                        "opportunityCount", data.path("opportunityCount").asInt(),
                        "marketIntelligenceCount", data.path("marketIntelligenceCount").asInt(),
                        "lostOrderCount", data.path("lostOrderCount").asInt()
                ));
        return new DailyReportAiResult(
                data.path("opportunityCount").asInt(),
                data.path("marketIntelligenceCount").asInt(),
                data.path("lostOrderCount").asInt(),
                parsedJsonText);
    }

    private void writeAckOutbox(String userId, Long reportId, DailyReportAiResult result, String rawContent) throws JsonProcessingException {
        Map<String, Object> payload = Map.of(
                "type", "DAILY_REPORT_PARSE_ACK",
                "dailyReportId", reportId,
                "toUserId", userId,
                "source", rawContent,
                "summary", "日报解析完成：商机" + result.opportunityCount() + "，商情" + result.marketIntelligenceCount() + "，丢单" + result.lostOrderCount());
        jdbcTemplate.update("""
                INSERT INTO crm_wecom_push_outbox(to_user_id, payload, status)
                VALUES (?, ?, 'MOCK_SENT')
                """, userId, objectMapper.writeValueAsString(payload));
    }

    private String abbreviate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    private String configValue(String key, String defaultValue) {
        try {
            String value = jdbcTemplate.queryForObject(
                    "SELECT config_value FROM sys_config WHERE config_key = ? AND deleted = 0 LIMIT 1",
                    String.class,
                    key);
            return value == null || value.isBlank() ? defaultValue : value;
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private record DailyReportAiResult(int opportunityCount, int marketIntelligenceCount, int lostOrderCount, String parsedJson) {}
}
