package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IntegrationInterfaceDTO(
    Long id,
    @NotBlank @Size(max = 64) String interfaceCode,
    @NotBlank @Size(max = 128) String interfaceName,
    @NotBlank @Size(max = 64) String systemCode,
    @Size(max = 64) String connectionCode,
    @NotBlank @Size(max = 32) String protocol,
    @NotBlank @Size(max = 32) String direction,
    @Size(max = 64) String businessModule,
    @Size(max = 128) String sapFunctionName,
    @Size(max = 16) String httpMethod,
    @Size(max = 500) String endpointPath,
    @Size(max = 64) String contentType,
    Short enabled,
    Integer retryLimit,
    @Size(max = 500) String description
) {}
