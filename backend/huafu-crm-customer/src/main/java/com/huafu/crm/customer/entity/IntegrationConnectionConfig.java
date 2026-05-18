package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("integration_connection_config")
public class IntegrationConnectionConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String connectionCode;
    private String connectionName;
    private String connectionType;
    private Short enabled;
    private String baseUrl;
    private String authType;
    private String authConfig;
    private String headerConfig;
    private Integer timeoutMs;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
    private Short deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConnectionCode() { return connectionCode; }
    public void setConnectionCode(String connectionCode) { this.connectionCode = connectionCode; }
    public String getConnectionName() { return connectionName; }
    public void setConnectionName(String connectionName) { this.connectionName = connectionName; }
    public String getConnectionType() { return connectionType; }
    public void setConnectionType(String connectionType) { this.connectionType = connectionType; }
    public Short getEnabled() { return enabled; }
    public void setEnabled(Short enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }
    public String getAuthConfig() { return authConfig; }
    public void setAuthConfig(String authConfig) { this.authConfig = authConfig; }
    public String getHeaderConfig() { return headerConfig; }
    public void setHeaderConfig(String headerConfig) { this.headerConfig = headerConfig; }
    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
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

