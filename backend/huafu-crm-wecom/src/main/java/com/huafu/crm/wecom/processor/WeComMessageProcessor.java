package com.huafu.crm.wecom.processor;

import com.huafu.crm.wecom.dispatcher.AiParseDispatcher;
import com.huafu.crm.wecom.parser.WeComXmlParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class WeComMessageProcessor {
    private final WeComXmlParser parser;
    private final JdbcTemplate jdbcTemplate;
    private final AiParseDispatcher aiParseDispatcher;

    public WeComMessageProcessor(WeComXmlParser parser, JdbcTemplate jdbcTemplate, AiParseDispatcher aiParseDispatcher) {
        this.parser = parser;
        this.jdbcTemplate = jdbcTemplate;
        this.aiParseDispatcher = aiParseDispatcher;
    }

    public void process(String rawXml) {
        WeComXmlParser.ParsedMessage message = parser.parse(rawXml);
        Long messageId = insertMessageLog(rawXml, message);
        if (shouldWriteDailyReport(message)) {
            Long reportId = insertDailyReport(messageId, message);
            markMessageParsed(messageId, reportId);
            try {
                aiParseDispatcher.dispatch(reportId);
            } catch (Exception ex) {
                markDailyReportParseFailed(reportId, ex);
                markMessageParseFailed(messageId, ex);
            }
        }
    }

    private Long insertMessageLog(String rawXml, WeComXmlParser.ParsedMessage message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO crm_wecom_message_log(
                        from_user_id, to_user_id, from_user, to_user, msg_type, msg_id,
                        raw_xml, content, status, parse_status
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'RECEIVED', 0)
                    """, new String[] {"id"});
            ps.setString(1, message.fromUser());
            ps.setString(2, message.toUser());
            ps.setString(3, message.fromUser());
            ps.setString(4, message.toUser());
            ps.setString(5, StringUtils.hasText(message.msgType()) ? message.msgType() : "text");
            ps.setString(6, message.msgId());
            ps.setString(7, rawXml == null ? "" : rawXml);
            ps.setString(8, message.content());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    private Long insertDailyReport(Long messageId, WeComXmlParser.ParsedMessage message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO crm_daily_report(user_id, report_date, content_text, parse_status, wecom_msg_id, created_by, updated_by)
                    VALUES (?, ?, ?, 0, ?, 'WECOM', 'WECOM')
                    """, new String[] {"id"});
            ps.setLong(1, numericUserId(message.fromUser()));
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setString(3, message.content());
            ps.setString(4, StringUtils.hasText(message.msgId()) ? message.msgId() : String.valueOf(messageId));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    private void markMessageParsed(Long messageId, Long reportId) {
        if (messageId == null) {
            return;
        }
        jdbcTemplate.update("""
                UPDATE crm_wecom_message_log
                SET parse_status = 1,
                    status = 'PARSED',
                    related_report_id = ?,
                    updated_time = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, reportId, messageId);
    }

    private void markDailyReportParseFailed(Long reportId, Exception ex) {
        if (reportId == null) {
            return;
        }
        jdbcTemplate.update("""
                UPDATE crm_daily_report
                SET parse_status = 3,
                    parse_error = ?,
                    updated_time = CURRENT_TIMESTAMP
                WHERE id = ?
                """, abbreviate(ex.getMessage(), 480), reportId);
    }

    private void markMessageParseFailed(Long messageId, Exception ex) {
        if (messageId == null) {
            return;
        }
        jdbcTemplate.update("""
                UPDATE crm_wecom_message_log
                SET status = 'AI_PARSE_FAILED',
                    error_message = ?,
                    updated_time = CURRENT_TIMESTAMP,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """, abbreviate(ex.getMessage(), 480), messageId);
    }

    private boolean shouldWriteDailyReport(WeComXmlParser.ParsedMessage message) {
        if (!receiveEnabled()) {
            return false;
        }
        String mode = configValue("wecom.receive_write_mode", "KEYWORD_DAILY_REPORT");
        if ("ALL_DAILY_REPORT".equalsIgnoreCase(mode)) {
            return StringUtils.hasText(message.content());
        }
        return isDailyReport(message);
    }

    private boolean isDailyReport(WeComXmlParser.ParsedMessage message) {
        String content = message.content() == null ? "" : message.content();
        if (!StringUtils.hasText(content)) {
            return false;
        }
        return receiveKeywords().stream().anyMatch(content::contains);
    }

    private boolean receiveEnabled() {
        return Boolean.parseBoolean(configValue("wecom.receive_enabled", "true"));
    }

    private List<String> receiveKeywords() {
        String configured = configValue("wecom.receive_keywords", "日报,商机,商情,丢单,拜访,客户");
        return Arrays.stream(configured.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .toList();
    }

    private String configValue(String key, String defaultValue) {
        try {
            String value = jdbcTemplate.queryForObject(
                "SELECT config_value FROM sys_config WHERE config_key = ? LIMIT 1",
                String.class,
                key
            );
            return StringUtils.hasText(value) ? value : defaultValue;
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private long numericUserId(String value) {
        try {
            if (StringUtils.hasText(value)) {
                return Long.parseLong(value.trim());
            }
        } catch (Exception ignored) {
            // External WeCom user ids are often non-numeric; keep them in the message log and use 0 for the CRM daily report owner.
        }
        return 0L;
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
