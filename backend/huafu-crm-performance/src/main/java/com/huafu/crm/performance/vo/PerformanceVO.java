package com.huafu.crm.performance.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PerformanceVO(
    Long id, Long userId, Integer statYear, Integer statMonth,
    Integer visitCount, Integer visitTarget, BigDecimal visitRate,
    Integer reportCount, Integer reportTarget, BigDecimal reportRate,
    Integer newCustomerCount, Integer newCustomerTarget, BigDecimal newCustomerRate,
    BigDecimal compositeScore, Integer grade,
    String createdBy, OffsetDateTime createdTime, Integer version
) {}
