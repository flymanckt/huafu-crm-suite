package com.huafu.crm.customer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IntegrationFieldMappingDTO(
    Long id,
    @NotNull Long interfaceId,
    @Size(max = 16) String parameterMode,
    @Size(max = 128) String parameterGroup,
    @Size(max = 16) String mappingDirection,
    @Size(max = 64) String sourceModule,
    @Size(max = 128) String sourceField,
    @Size(max = 128) String sourceFieldLabel,
    @Size(max = 128) String targetField,
    @Size(max = 128) String targetFieldLabel,
    String tableFieldMappings,
    @Size(max = 32) String fieldType,
    Short required,
    @Size(max = 500) String defaultValue,
    String transformRule,
    Integer sortOrder,
    @Size(max = 500) String remark
) {}
