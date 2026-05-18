package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("integration_field_mapping")
public class IntegrationFieldMapping {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long interfaceId;
    private String parameterMode;
    private String parameterGroup;
    private String mappingDirection;
    private String sourceModule;
    private String sourceField;
    private String sourceFieldLabel;
    private String targetField;
    private String targetFieldLabel;
    private String fieldType;
    private Short required;
    private String defaultValue;
    private String transformRule;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Short deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInterfaceId() { return interfaceId; }
    public void setInterfaceId(Long interfaceId) { this.interfaceId = interfaceId; }
    public String getParameterMode() { return parameterMode; }
    public void setParameterMode(String parameterMode) { this.parameterMode = parameterMode; }
    public String getParameterGroup() { return parameterGroup; }
    public void setParameterGroup(String parameterGroup) { this.parameterGroup = parameterGroup; }
    public String getMappingDirection() { return mappingDirection; }
    public void setMappingDirection(String mappingDirection) { this.mappingDirection = mappingDirection; }
    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }
    public String getSourceField() { return sourceField; }
    public void setSourceField(String sourceField) { this.sourceField = sourceField; }
    public String getSourceFieldLabel() { return sourceFieldLabel; }
    public void setSourceFieldLabel(String sourceFieldLabel) { this.sourceFieldLabel = sourceFieldLabel; }
    public String getTargetField() { return targetField; }
    public void setTargetField(String targetField) { this.targetField = targetField; }
    public String getTargetFieldLabel() { return targetFieldLabel; }
    public void setTargetFieldLabel(String targetFieldLabel) { this.targetFieldLabel = targetFieldLabel; }
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    public Short getRequired() { return required; }
    public void setRequired(Short required) { this.required = required; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public String getTransformRule() { return transformRule; }
    public void setTransformRule(String transformRule) { this.transformRule = transformRule; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    public Short getDeleted() { return deleted; }
    public void setDeleted(Short deleted) { this.deleted = deleted; }
}
