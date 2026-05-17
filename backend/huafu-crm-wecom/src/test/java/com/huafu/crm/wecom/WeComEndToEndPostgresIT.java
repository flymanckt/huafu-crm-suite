package com.huafu.crm.wecom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
class WeComEndToEndPostgresIT {
    private final String runToken = "ROUND3-" + UUID.randomUUID();

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanOwnRows() {
        jdbcTemplate.update("DELETE FROM crm_wecom_push_outbox WHERE payload LIKE ?", "%" + runToken + "%");
        jdbcTemplate.update("DELETE FROM crm_ai_parse_log WHERE request_payload LIKE ? OR response_payload LIKE ?", "%" + runToken + "%", "%" + runToken + "%");
        jdbcTemplate.update("DELETE FROM crm_lost_order WHERE reason LIKE ?", "%" + runToken + "%");
        jdbcTemplate.update("DELETE FROM crm_opportunity WHERE name LIKE ?", "%" + runToken + "%");
        jdbcTemplate.update("DELETE FROM crm_lead WHERE title LIKE ? OR description LIKE ?", "%" + runToken + "%", "%" + runToken + "%");
        jdbcTemplate.update("DELETE FROM crm_daily_report WHERE raw_content LIKE ?", "%" + runToken + "%");
        jdbcTemplate.update("DELETE FROM crm_wecom_message_log WHERE raw_xml LIKE ?", "%" + runToken + "%");
    }

    @Test
    void postWeComDailyReportShouldPersistMessageParseAiAndBusinessRowsThenAckOutbox() {
        String content = "日报 " + runToken + "\n商机：新疆针织面料报价推进 " + runToken + "\n商情：客户关注棉价 " + runToken + "\n丢单：某订单因价格流失 " + runToken;
        String xml = "<xml>" +
                "<ToUserName><![CDATA[corp]]></ToUserName>" +
                "<FromUserName><![CDATA[sales-" + runToken + "]]></FromUserName>" +
                "<CreateTime>1710000000</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[" + content + "]]></Content>" +
                "<MsgId>10086</MsgId>" +
                "</xml>";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        long started = System.nanoTime();
        String response = restTemplate.postForObject("http://localhost:" + port + "/wecom/receive", new HttpEntity<>(xml, headers), String.class);
        Duration elapsed = Duration.ofNanos(System.nanoTime() - started);

        assertThat(response).isEqualTo("success");
        assertThat(elapsed).isLessThan(Duration.ofSeconds(2));

        assertThat(count("crm_wecom_message_log", "raw_xml")).isEqualTo(1);
        assertThat(count("crm_daily_report", "raw_content")).isEqualTo(1);
        assertThat(count("crm_ai_parse_log", "request_payload")).isEqualTo(1);
        assertThat(count("crm_lead", "description")).isEqualTo(1);
        assertThat(count("crm_opportunity", "name")).isEqualTo(1);
        assertThat(count("crm_lost_order", "reason")).isEqualTo(1);
        assertThat(count("crm_wecom_push_outbox", "payload")).isEqualTo(1);

        String status = jdbcTemplate.queryForObject(
                "SELECT ai_parse_status FROM crm_daily_report WHERE raw_content LIKE ? ORDER BY id DESC LIMIT 1",
                String.class,
                "%" + runToken + "%");
        String parsedJson = jdbcTemplate.queryForObject(
                "SELECT parsed_json::text FROM crm_daily_report WHERE raw_content LIKE ? ORDER BY id DESC LIMIT 1",
                String.class,
                "%" + runToken + "%");
        assertThat(status).isEqualTo("PARSED");
        assertThat(parsedJson).contains("opportunityCount", "marketIntelligenceCount", "lostOrderCount");
    }

    private int count(String table, String column) {
        Integer value = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + " LIKE ?",
                Integer.class,
                "%" + runToken + "%");
        return value == null ? 0 : value;
    }
}
