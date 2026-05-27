package com.huafu.crm.target.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("crm_target")
public class Target {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String targetNo;
    private Integer targetYear;
    private Integer targetMonth;
    private Long deptId;
    private Long userId;
    private String productCategory;
    private Integer metricType;
    private BigDecimal targetValue;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getTargetNo() { return targetNo; } public void setTargetNo(String v) { this.targetNo = v; }
    public Integer getTargetYear() { return targetYear; } public void setTargetYear(Integer v) { this.targetYear = v; }
    public Integer getTargetMonth() { return targetMonth; } public void setTargetMonth(Integer v) { this.targetMonth = v; }
    public Long getDeptId() { return deptId; } public void setDeptId(Long v) { this.deptId = v; }
    public Long getUserId() { return userId; } public void setUserId(Long v) { this.userId = v; }
    public String getProductCategory() { return productCategory; } public void setProductCategory(String v) { this.productCategory = v; }
    public Integer getMetricType() { return metricType; } public void setMetricType(Integer v) { this.metricType = v; }
    public BigDecimal getTargetValue() { return targetValue; } public void setTargetValue(BigDecimal v) { this.targetValue = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}