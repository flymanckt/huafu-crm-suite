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
    private String sourceField;
    private String targetField;
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
    public String getSourceField() { return sourceField; }
    public void setSourceField(String sourceField) { this.sourceField = sourceField; }
    public String getTargetField() { return targetField; }
    public void setTargetField(String targetField) { this.targetField = targetField; }
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

