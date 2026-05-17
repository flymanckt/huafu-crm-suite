package com.huafu.crm.performance.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record VisitRecordCreateDTO(
    @NotNull Long userId, Long customerId, String customerName,
    Long contactId, String contactName, @NotNull LocalDate visitDate,
    @NotNull Integer visitType, String visitPurpose, String visitContent,
    LocalDate nextVisitPlan, Integer isNewCustomer,
    BigDecimal longitude, BigDecimal latitude, String locationName,
    Long customerAddressId, BigDecimal checkinLongitude, BigDecimal checkinLatitude,
    String checkinAddress, OffsetDateTime checkinTime, String remark
) {}
