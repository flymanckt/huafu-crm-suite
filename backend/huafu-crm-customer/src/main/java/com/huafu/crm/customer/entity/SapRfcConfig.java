package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sap_rfc_config")
public class SapRfcConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String configCode;
    private String configName;
    private Short enabled;
    private String appServerHost;
    private String systemNumber;
    private String client;
    private String userName;
    private String passwordCipher;
    private String language;
    private Integer poolCapacity;
    private Integer peakLimit;
    private Integer connectionTimeout;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdTime;
    private Long updatedBy;
    private LocalDateTime updatedTime;
    private Short deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConfigCode() { return configCode; }
    public void setConfigCode(String configCode) { this.configCode = configCode; }
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    public Short getEnabled() { return enabled; }
    public void setEnabled(Short enabled) { this.enabled = enabled; }
    public String getAppServerHost() { return appServerHost; }
    public void setAppServerHost(String appServerHost) { this.appServerHost = appServerHost; }
    public String getSystemNumber() { return systemNumber; }
    public void setSystemNumber(String systemNumber) { this.systemNumber = systemNumber; }
    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPasswordCipher() { return passwordCipher; }
    public void setPasswordCipher(String passwordCipher) { this.passwordCipher = passwordCipher; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Integer getPoolCapacity() { return poolCapacity; }
    public void setPoolCapacity(Integer poolCapacity) { this.poolCapacity = poolCapacity; }
    public Integer getPeakLimit() { return peakLimit; }
    public void setPeakLimit(Integer peakLimit) { this.peakLimit = peakLimit; }
    public Integer getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(Integer connectionTimeout) { this.connectionTimeout = connectionTimeout; }
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

