package com.huafu.crm.opportunity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LeadCreateDTO(
    @NotNull Integer leadType, Long customerId, String customerName,
    String contactName, String contactPhone, String province, String city,
    String productName, String competitorName, BigDecimal competitorPrice,
    BigDecimal competitorDiscount, BigDecimal marginRate, Integer source,
    @NotNull Long creatorUserId, Long handlerUserId, String remark
) {}
