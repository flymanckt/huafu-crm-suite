package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IntegrationLogCreateDTO(
    @NotBlank @Size(max = 64) String interfaceCode,
    @Size(max = 128) String interfaceName,
    @Size(max = 32) String direction,
    @Size(max = 128) String businessKey,
    @Size(max = 32) String status,
    String requestPayload,
    String responsePayload,
    String mappingDetail,
    String errorMessage
) {}
