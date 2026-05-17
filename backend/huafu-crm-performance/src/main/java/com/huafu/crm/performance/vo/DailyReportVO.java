package com.huafu.crm.performance.vo;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record DailyReportVO(
    Long id, String reportNo, Long userId, LocalDate reportDate,
    String contentText, Integer parseStatus, String parsedJson,
    Integer opportunityCount, Integer marketIntelligenceCount, Integer lostOrderCount,
    String parseError, OffsetDateTime parseTime, String wecomMsgId,
    String createdBy, OffsetDateTime createdTime, Integer version
) {}
