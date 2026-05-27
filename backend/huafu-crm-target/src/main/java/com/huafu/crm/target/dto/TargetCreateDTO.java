package com.huafu.crm.target.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TargetCreateDTO(
    @NotNull Integer targetYear, @NotNull Integer targetMonth,
    Long deptId, Long userId, String productCategory,
    @NotNull Integer metricType, @NotNull BigDecimal targetValue
) {}
