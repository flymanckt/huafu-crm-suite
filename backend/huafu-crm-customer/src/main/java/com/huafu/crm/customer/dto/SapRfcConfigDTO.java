package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SapRfcConfigDTO(
    Long id,
    @NotBlank @Size(max = 64) String configCode,
    @NotBlank @Size(max = 128) String configName,
    Short enabled,
    @Size(max = 255) String appServerHost,
    @Size(max = 16) String systemNumber,
    @Size(max = 16) String client,
    @Size(max = 128) String userName,
    String passwordCipher,
    @Size(max = 8) String language,
    Integer poolCapacity,
    Integer peakLimit,
    Integer connectionTimeout,
    @Size(max = 500) String remark
) {}

