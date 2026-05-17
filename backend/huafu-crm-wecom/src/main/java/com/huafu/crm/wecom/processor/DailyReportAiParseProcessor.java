package com.huafu.crm.wecom.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class DailyReportAiParseProcessor {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RestClient aiRestClient;
    private final RestClient opportunityRestClient;

    public DailyReportAiParseProcessor(JdbcTemplate jdbcTemplate,
                                       ObjectMapper objectMapper,
                                       RestClient.Builder restClientBuilder,
                                       @Value("${huafu.internal.ai-base-url:http://localhost:8085}") String aiBaseUrl,
                                       @Value("${huafu.internal.opportunity-base-url:http://localhost:8082}") String opportunityBaseUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.aiRestClient = restClientBuilder.baseUrl(aiBaseUrl).build();
        this.opportunityRestClient = restClientBuilder.baseUrl(opportunityBaseUrl).build();
    }

    public void parse(Long dailyReportId) {
        if (dailyReportId == null) {
            return;
        }
        Map<String, Object> report = jdbcTemplate.queryForMap(
                "SELECT id, user_id, raw_content FROM crm_daily_report WHERE id = ?",
                dailyReportId);
        String userId = String.valueOf(report.get("user_id"));
        String rawContent = String.valueOf(report.get("raw_content"));
        Long logId = insertAiLog(dailyReportId, rawContent);
        try {
            DailyReportAiResult result = requestAiParse(rawContent);
            String responseJson = objectMapper.writeValueAsString(result);
            jdbcTemplate.update("""
                    UPDATE crm_daily_report
                    SET parsed_json = ?::jsonb,
                        opportunity_count = ?,
                        market_intelligence_count = ?,
                        lost_order_count = ?,
                        ai_parse_status = 'PARSED',
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, responseJson, result.opportunityCount(), result.marketIntelligenceCount(), result.lostOrderCount(), dailyReportId);
            writeBusinessRows(rawContent, userId);
            jdbcTemplate.update("""
                    UPDATE crm_ai_parse_log
                    SET response_payload = ?, status = 'SUCCESS', updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    """, responseJson, logId);
            writeAckOutbox(userId, dailyReportId, result, rawContent);
        } catch (Exception ex) {
            jdbcTemplate.update("""
                    UPDATE crm_daily_report SET ai_parse_status = 'FAILED', updated_at = CURRENT_TIMESTAMP WHERE id = ?
                    """, dailyReportId);
            jdbcTemplate.update("""
                    UPDATE crm_ai_parse_log SET status = 'FAILED', error_message = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?
                    """, ex.getMessage(), logId);
            throw new IllegalStateException("daily report AI parse failed", ex);
        }
    }

    private Long insertAiLog(Long reportId, String rawContent) {
        jdbcTemplate.update("""
                INSERT INTO crm_ai_parse_log(source_type, source_id, request_payload, status)
                VALUES ('DAILY_REPORT', ?, ?, 'PENDING')
                """, reportId, rawContent);
        return jdbcTemplate.queryForObject("SELECT currval(pg_get_serial_sequence('crm_ai_parse_log','id'))", Long.class);
    }

    private DailyReportAiResult requestAiParse(String rawContent) throws JsonProcessingException {
        String response = aiRestClient.post()
                .uri("/api/ai/parse/daily-report")
                .body(rawContent == null ? "" : rawContent)
                .retrieve()
                .body(String.class);
        JsonNode data = objectMapper.readTree(response).path("data");
        return new DailyReportAiResult(
                data.path("opportunityCount").asInt(),
                data.path("marketIntelligenceCount").asInt(),
                data.path("lostOrderCount").asInt());
    }

    private void writeBusinessRows(String rawContent, String userId) {
        Long handlerUserId = parseLongOrDefault(userId, 0L);
        for (String line : lines(rawContent)) {
            if (line.contains("商情")) {
                postOpportunityDomain("/lead", Map.of(
                        "leadType", 1,
                        "source", 1,
                        "creatorUserId", handlerUserId,
                        "handlerUserId", handlerUserId,
                        "remark", abbreviate(line, 480)));
            }
            if (line.contains("商机") || line.contains("机会")) {
                postOpportunityDomain("/opportunity", Map.of(
                        "opportunityName", abbreviate(line, 180),
                        "customerId", 0L,
                        "handlerUserId", handlerUserId,
                        "stage", 1,
                        "remark", rawContent == null ? "" : rawContent));
            }
            if (line.contains("丢单") || line.contains("流失")) {
                postOpportunityDomain("/lost-order", Map.of(
                        "opportunityId", 0L,
                        "customerId", 0L,
                        "lostType", 5,
                        "reasonDetail", abbreviate(line, 480),
                        "handlerUserId", handlerUserId));
            }
        }
    }

    private void postOpportunityDomain(String path, Map<String, Object> payload) {
        opportunityRestClient.post().uri(path).body(payload).retrieve().toBodilessEntity();
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

    private List<String> lines(String rawContent) {
        return rawContent == null ? List.of() : rawContent.lines().map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private String abbreviate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }

    private Long parseLongOrDefault(String value, Long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private record DailyReportAiResult(int opportunityCount, int marketIntelligenceCount, int lostOrderCount) {}
}
