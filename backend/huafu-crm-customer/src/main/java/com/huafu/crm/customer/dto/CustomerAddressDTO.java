package com.huafu.crm.customer.dto;

import java.math.BigDecimal;

public record CustomerAddressDTO(
    Long id,
    Long customerId,
    Integer addressType,
    String contactName,
    String phone,
    String country,
    String province,
    String city,
    String district,
    String addressDetail,
    String fullAddress,
    BigDecimal longitude,
    BigDecimal latitude,
    String amapPoiId,
    String amapPoiName,
    String amapAdcode,
    String amapLevel,
    String locationSource,
    Integer locationVerified,
    Integer checkinRadiusMeters,
    String addressRemark,
    Integer isDefault
) {}
