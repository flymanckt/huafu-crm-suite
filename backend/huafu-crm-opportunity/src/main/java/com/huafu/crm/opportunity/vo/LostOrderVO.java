package com.huafu.crm.opportunity.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LostOrderVO(
    Long id, String lostNo, Long opportunityId, Long customerId, Integer lostType,
    String competitorName, BigDecimal competitorPrice, BigDecimal ourPrice,
    BigDecimal marginDiff, BigDecimal competitorDiscount, BigDecimal ourDiscount,
    String reasonDetail, Integer recoveryPossible, LocalDate followUpDate,
    Long handlerUserId, String createdBy, OffsetDateTime createdTime, Integer version
) {}
