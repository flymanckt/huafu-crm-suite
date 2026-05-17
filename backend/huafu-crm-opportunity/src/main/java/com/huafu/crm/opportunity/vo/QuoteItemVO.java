package com.huafu.crm.opportunity.vo;

import java.math.BigDecimal;

public record QuoteItemVO(
    Long id,
    Long quoteId,
    String productName,
    String productCode,
    BigDecimal quantity,
    String unit,
    BigDecimal unitPrice,
    BigDecimal amount,
    String leadTime,
    String remark
) {}
