package com.huafu.crm.target.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TargetAchieveVO(
    Long id, Long targetId, Integer achieveYear, Integer achieveMonth,
    BigDecimal achieveValue, BigDecimal achieveRate, Integer source,
    LocalDate sourceDataDate, OffsetDateTime syncedTime, String remark,
    String createdBy, OffsetDateTime createdTime, Integer version
) {}
