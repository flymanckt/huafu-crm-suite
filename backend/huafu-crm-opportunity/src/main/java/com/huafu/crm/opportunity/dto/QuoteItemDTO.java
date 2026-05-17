package com.huafu.crm.opportunity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record QuoteItemDTO(
    @NotBlank String productName,
    String productCode,
    @NotNull BigDecimal quantity,
    String unit,
    @NotNull BigDecimal unitPrice,
    String leadTime,
    String remark
) {}
