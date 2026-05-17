package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String configValue;
    private String configName;
    private String configGroup;
    private String description;
    private String type;
    private Boolean editable;
    private Boolean visible;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    @TableLogic
    private Short deleted;

    // Manual getters (Lombok @Data not available in this module)
    public Long getId() { return id; }
    public String getConfigKey() { return configKey; }
    public String getConfigValue() { return configValue; }
    public String getConfigName() { return configName; }
    public String getConfigGroup() { return configGroup; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public Boolean getEditable() { return editable; }
    public Boolean getVisible() { return visible; }
    public Long getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public Long getUpdatedBy() { return updatedBy; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public Short getDeleted() { return deleted; }

    // Manual setters
    public void setId(Long id) { this.id = id; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public void setConfigName(String configName) { this.configName = configName; }
    public void setConfigGroup(String configGroup) { this.configGroup = configGroup; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setEditable(Boolean editable) { this.editable = editable; }
    public void setVisible(Boolean visible) { this.visible = visible; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    public void setDeleted(Short deleted) { this.deleted = deleted; }
}
