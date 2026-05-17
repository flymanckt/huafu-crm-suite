package com.huafu.crm.opportunity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record OpportunityCreateDTO(
    @NotBlank String opportunityName, @NotNull Long customerId, @NotNull Long handlerUserId,
    @NotNull Integer stage, String productName, BigDecimal quantity, String unit,
    BigDecimal estimatedAmount, LocalDate expectedCloseDate, Integer winProbability,
    String remark, Long sourceLeadId
) {}
