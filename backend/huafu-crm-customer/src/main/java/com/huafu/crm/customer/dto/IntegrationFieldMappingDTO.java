package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IntegrationFieldMappingDTO(
    Long id,
    @NotNull Long interfaceId,
    @NotBlank @Size(max = 128) String sourceField,
    @NotBlank @Size(max = 128) String targetField,
    @Size(max = 32) String fieldType,
    Short required,
    @Size(max = 500) String defaultValue,
    String transformRule,
    Integer sortOrder,
    @Size(max = 500) String remark
) {}

