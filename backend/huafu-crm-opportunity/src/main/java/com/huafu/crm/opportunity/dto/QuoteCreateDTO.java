package com.huafu.crm.opportunity.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteCreateDTO(
    Long customerId,
    Long opportunityId,
    Long contactId,
    String contactName,
    String contactPhone,
    Long salesUserId,
    LocalDate quoteDate,
    LocalDate validDate,
    BigDecimal discountRate,
    String paymentTerms,
    String deliveryTerms,
    String remark
) {}
