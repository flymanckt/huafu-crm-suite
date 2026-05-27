package com.huafu.crm.target.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TargetVO(
    Long id, String targetNo, Integer targetYear, Integer targetMonth,
    Long deptId, Long userId, String productCategory, Integer metricType,
    BigDecimal targetValue, String createdBy, OffsetDateTime createdTime, Integer version
) {}
