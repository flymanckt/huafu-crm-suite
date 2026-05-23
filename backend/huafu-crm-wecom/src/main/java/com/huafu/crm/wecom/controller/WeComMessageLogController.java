package com.huafu.crm.wecom.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wecom/message-log")
public class WeComMessageLogController {
    private final JdbcTemplate jdbcTemplate;

    public WeComMessageLogController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/page")
    public Result<PageResult<Map<String, Object>>> page(
        @RequestParam(defaultValue = "1") long current,
        @RequestParam(defaultValue = "20") long size,
        @RequestParam(required = false) Integer parseStatus,
        @RequestParam(required = false) String msgType,
        @RequestParam(required = false) String fromUser,
        @RequestParam(required = false) String toUser,
        @RequestParam(required = false) String content
    ) {
        long safeCurrent = Math.max(1, current);
        long safeSize = Math.max(1, Math.min(size, 100));
        List<Object> params = new ArrayList<>();
        String where = buildWhere(parseStatus, msgType, fromUser, toUser, content, params);
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM crm_wecom_message_log WHERE " + where, Long.class, params.toArray());
        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(safeSize);
        pageParams.add((safeCurrent - 1) * safeSize);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
            SELECT
                id,
                msg_id AS "msgId",
                msg_type AS "msgType",
                COALESCE(from_user, from_user_id) AS "fromUser",
                COALESCE(to_user, to_user_id) AS "toUser",
                content,
                raw_xml AS "rawXml",
                COALESCE(parse_status,
                    CASE
                        WHEN status IN ('PARSED', 'SUCCESS') THEN 1
                        WHEN status IN ('FAILED', 'ERROR') THEN 2
                        ELSE 0
                    END::SMALLINT) AS "parseStatus",
                COALESCE(created_time, created_at) AS "createdTime"
            FROM crm_wecom_message_log
            WHERE %s
            ORDER BY COALESCE(created_time, created_at) DESC, id DESC
            LIMIT ? OFFSET ?
            """.formatted(where), pageParams.toArray());
        return Result.ok(PageResult.of(safeCurrent, safeSize, total == null ? 0 : total, rows));
    }

    private String buildWhere(Integer parseStatus, String msgType, String fromUser, String toUser, String content, List<Object> params) {
        List<String> filters = new ArrayList<>();
        filters.add("COALESCE(deleted, 0) = 0");
        if (parseStatus != null) {
            filters.add("""
                COALESCE(parse_status,
                    CASE
                        WHEN status IN ('PARSED', 'SUCCESS') THEN 1
                        WHEN status IN ('FAILED', 'ERROR') THEN 2
                        ELSE 0
                    END::SMALLINT) = ?
                """);
            params.add(parseStatus);
        }
        if (StringUtils.hasText(msgType)) {
            filters.add("msg_type = ?");
            params.add(msgType.trim());
        }
        if (StringUtils.hasText(fromUser)) {
            filters.add("COALESCE(from_user, from_user_id) ILIKE ?");
            params.add("%" + fromUser.trim() + "%");
        }
        if (StringUtils.hasText(toUser)) {
            filters.add("COALESCE(to_user, to_user_id) ILIKE ?");
            params.add("%" + toUser.trim() + "%");
        }
        if (StringUtils.hasText(content)) {
            filters.add("content ILIKE ?");
            params.add("%" + content.trim() + "%");
        }
        return String.join(" AND ", filters);
    }
}
