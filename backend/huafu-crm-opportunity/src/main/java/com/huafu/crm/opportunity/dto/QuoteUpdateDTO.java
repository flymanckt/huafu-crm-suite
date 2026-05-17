package com.huafu.crm.opportunity.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteUpdateDTO(
    Long customerId,
    Long contactId,
    String contactName,
    String contactPhone,
    LocalDate quoteDate,
    LocalDate validDate,
    BigDecimal discountRate,
    String paymentTerms,
    String deliveryTerms,
    String remark
) {}
