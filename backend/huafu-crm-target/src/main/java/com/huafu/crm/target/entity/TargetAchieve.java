package com.huafu.crm.target.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_target_achieve")
public class TargetAchieve {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long targetId;
    private Integer achieveYear;
    private Integer achieveMonth;
    private BigDecimal achieveValue;
    private BigDecimal achieveRate;
    private Integer source;
    private LocalDate sourceDataDate;
    private OffsetDateTime syncedTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getTargetId() { return targetId; } public void setTargetId(Long v) { this.targetId = v; }
    public Integer getAchieveYear() { return achieveYear; } public void setAchieveYear(Integer v) { this.achieveYear = v; }
    public Integer getAchieveMonth() { return achieveMonth; } public void setAchieveMonth(Integer v) { this.achieveMonth = v; }
    public BigDecimal getAchieveValue() { return achieveValue; } public void setAchieveValue(BigDecimal v) { this.achieveValue = v; }
    public BigDecimal getAchieveRate() { return achieveRate; } public void setAchieveRate(BigDecimal v) { this.achieveRate = v; }
    public Integer getSource() { return source; } public void setSource(Integer v) { this.source = v; }
    public LocalDate getSourceDataDate() { return sourceDataDate; } public void setSourceDataDate(LocalDate v) { this.sourceDataDate = v; }
    public OffsetDateTime getSyncedTime() { return syncedTime; } public void setSyncedTime(OffsetDateTime v) { this.syncedTime = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}