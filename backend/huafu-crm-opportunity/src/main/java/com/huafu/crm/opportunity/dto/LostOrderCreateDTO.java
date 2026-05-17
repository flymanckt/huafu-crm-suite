package com.huafu.crm.opportunity.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LostOrderCreateDTO(
    @NotNull Long opportunityId, @NotNull Long customerId, @NotNull Integer lostType,
    String competitorName, BigDecimal competitorPrice, BigDecimal ourPrice,
    BigDecimal marginDiff, BigDecimal competitorDiscount, BigDecimal ourDiscount,
    String reasonDetail, Integer recoveryPossible, LocalDate followUpDate, Long handlerUserId
) {}
