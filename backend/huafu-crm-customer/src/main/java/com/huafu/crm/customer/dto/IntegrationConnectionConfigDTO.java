package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IntegrationConnectionConfigDTO(
    Long id,
    @NotBlank @Size(max = 64) String connectionCode,
    @NotBlank @Size(max = 128) String connectionName,
    @NotBlank @Size(max = 32) String connectionType,
    Short enabled,
    @Size(max = 500) String baseUrl,
    @Size(max = 32) String authType,
    String authConfig,
    String headerConfig,
    Integer timeoutMs,
    @Size(max = 500) String remark
) {}

