package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("integration_interface")
public class IntegrationInterface {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String interfaceCode;
    private String interfaceName;
    private String systemCode;
    private String connectionCode;
    private String protocol;
    private String direction;
    private String businessModule;
    private String sapFunctionName;
    private String httpMethod;
    private String endpointPath;
    private String contentType;
    private String triggerMode;
    private String triggerResource;
    private String triggerConditionJson;
    private Short enabled;
    private Integer retryLimit;
    private String successRuleType;
    private String successFieldPath;
    private String successExpectedValues;
    private String failureExpectedValues;
    private String successMessagePath;
    private String description;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
    private Short deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInterfaceCode() { return interfaceCode; }
    public void setInterfaceCode(String interfaceCode) { this.interfaceCode = interfaceCode; }
    public String getInterfaceName() { return interfaceName; }
    public void setInterfaceName(String interfaceName) { this.interfaceName = interfaceName; }
    public String getSystemCode() { return systemCode; }
    public void setSystemCode(String systemCode) { this.systemCode = systemCode; }
    public String getConnectionCode() { return connectionCode; }
    public void setConnectionCode(String connectionCode) { this.connectionCode = connectionCode; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getBusinessModule() { return businessModule; }
    public void setBusinessModule(String businessModule) { this.businessModule = businessModule; }
    public String getSapFunctionName() { return sapFunctionName; }
    public void setSapFunctionName(String sapFunctionName) { this.sapFunctionName = sapFunctionName; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getEndpointPath() { return endpointPath; }
    public void setEndpointPath(String endpointPath) { this.endpointPath = endpointPath; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getTriggerMode() { return triggerMode; }
    public void setTriggerMode(String triggerMode) { this.triggerMode = triggerMode; }
    public String getTriggerResource() { return triggerResource; }
    public void setTriggerResource(String triggerResource) { this.triggerResource = triggerResource; }
    public String getTriggerConditionJson() { return triggerConditionJson; }
    public void setTriggerConditionJson(String triggerConditionJson) { this.triggerConditionJson = triggerConditionJson; }
    public Short getEnabled() { return enabled; }
    public void setEnabled(Short enabled) { this.enabled = enabled; }
    public Integer getRetryLimit() { return retryLimit; }
    public void setRetryLimit(Integer retryLimit) { this.retryLimit = retryLimit; }
    public String getSuccessRuleType() { return successRuleType; }
    public void setSuccessRuleType(String successRuleType) { this.successRuleType = successRuleType; }
    public String getSuccessFieldPath() { return successFieldPath; }
    public void setSuccessFieldPath(String successFieldPath) { this.successFieldPath = successFieldPath; }
    public String getSuccessExpectedValues() { return successExpectedValues; }
    public void setSuccessExpectedValues(String successExpectedValues) { this.successExpectedValues = successExpectedValues; }
    public String getFailureExpectedValues() { return failureExpectedValues; }
    public void setFailureExpectedValues(String failureExpectedValues) { this.failureExpectedValues = failureExpectedValues; }
    public String getSuccessMessagePath() { return successMessagePath; }
    public void setSuccessMessagePath(String successMessagePath) { this.successMessagePath = successMessagePath; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    public Short getDeleted() { return deleted; }
    public void setDeleted(Short deleted) { this.deleted = deleted; }
}
