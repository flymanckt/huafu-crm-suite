package com.huafu.crm.opportunity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StageUpdateDTO(
    Integer stage,
    BigDecimal actualAmount,
    Integer lostType,
    String lostReason,
    String competitorName
) {}
