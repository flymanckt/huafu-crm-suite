package com.huafu.crm.opportunity.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OpportunityVO(
    Long id, String oppNo, String opportunityName, Long customerId, Long handlerUserId,
    Integer stage, OffsetDateTime stageUpdateTime, String productName,
    BigDecimal quantity, String unit, BigDecimal estimatedAmount,
    LocalDate expectedCloseDate, LocalDate actualCloseDate, Integer winProbability,
    String lostReason, Integer lostType, String competitorName, Long quoteId,
    String orderNo, String remark, Long sourceLeadId,
    String createdBy, OffsetDateTime createdTime, Integer version
) {}
