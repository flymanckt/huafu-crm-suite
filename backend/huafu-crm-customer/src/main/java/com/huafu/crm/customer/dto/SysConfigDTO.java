package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SysConfigDTO(
    Long id,
    @NotBlank @Size(max = 128) String configKey,
    String configValue,
    @Size(max = 255) String configName,
    @NotBlank @Size(max = 64) String configGroup,
    @Size(max = 500) String description,
    @NotBlank @Size(max = 32) String type,
    Boolean editable,
    Boolean visible
) {}
