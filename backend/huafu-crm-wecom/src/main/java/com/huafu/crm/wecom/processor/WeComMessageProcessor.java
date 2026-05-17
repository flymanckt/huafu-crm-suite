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
        if (isDailyReport(message)) {
            Long reportId = insertDailyReport(messageId, message);
            aiParseDispatcher.dispatch(reportId);
        }
    }

    private Long insertMessageLog(String rawXml, WeComXmlParser.ParsedMessage message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO crm_wecom_message_log(from_user_id, to_user_id, msg_type, msg_id, raw_xml, content, status)
                    VALUES (?, ?, ?, ?, ?, ?, 'RECEIVED')
                    """, new String[] {"id"});
            ps.setString(1, message.fromUser());
            ps.setString(2, message.toUser());
            ps.setString(3, StringUtils.hasText(message.msgType()) ? message.msgType() : "text");
            ps.setString(4, message.msgId());
            ps.setString(5, rawXml == null ? "" : rawXml);
            ps.setString(6, message.content());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    private Long insertDailyReport(Long messageId, WeComXmlParser.ParsedMessage message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO crm_daily_report(wecom_message_id, user_id, report_date, raw_content, ai_parse_status)
                    VALUES (?, ?, ?, ?, 'PENDING')
                    """, new String[] {"id"});
            if (messageId == null) {
                ps.setObject(1, null);
            } else {
                ps.setLong(1, messageId);
            }
            ps.setString(2, StringUtils.hasText(message.fromUser()) ? message.fromUser() : "unknown");
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            ps.setString(4, message.content());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key == null ? null : key.longValue();
    }

    private boolean isDailyReport(WeComXmlParser.ParsedMessage message) {
        String content = message.content() == null ? "" : message.content();
        return StringUtils.hasText(content) && (content.contains("日报") || content.contains("商机") || content.contains("商情") || content.contains("丢单"));
    }
}
