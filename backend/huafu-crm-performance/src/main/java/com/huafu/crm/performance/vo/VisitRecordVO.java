package com.huafu.crm.performance.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record VisitRecordVO(
    Long id, String visitNo, Long userId, Long customerId, String customerName,
    Long contactId, String contactName, LocalDate visitDate, Integer visitType,
    String visitPurpose, String visitContent, LocalDate nextVisitPlan,
    Integer isNewCustomer, BigDecimal longitude, BigDecimal latitude,
    String locationName, Long customerAddressId, BigDecimal checkinLongitude,
    BigDecimal checkinLatitude, String checkinAddress, OffsetDateTime checkinTime,
    BigDecimal checkinDistanceMeters, Integer checkinRadiusMeters,
    Integer checkinMatched, String checkinResult, String remark,
    String createdBy, OffsetDateTime createdTime, Integer version
) {}
