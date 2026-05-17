package com.huafu.crm.performance.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("crm_performance")
public class Performance {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Integer statYear;
    private Integer statMonth;
    private Integer visitCount;
    private Integer visitTarget;
    private BigDecimal visitRate;
    private Integer reportCount;
    private Integer reportTarget;
    private BigDecimal reportRate;
    private Integer newCustomerCount;
    private Integer newCustomerTarget;
    private BigDecimal newCustomerRate;
    private BigDecimal compositeScore;
    private Integer grade;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long v) { this.userId = v; }
    public Integer getStatYear() { return statYear; } public void setStatYear(Integer v) { this.statYear = v; }
    public Integer getStatMonth() { return statMonth; } public void setStatMonth(Integer v) { this.statMonth = v; }
    public Integer getVisitCount() { return visitCount; } public void setVisitCount(Integer v) { this.visitCount = v; }
    public Integer getVisitTarget() { return visitTarget; } public void setVisitTarget(Integer v) { this.visitTarget = v; }
    public BigDecimal getVisitRate() { return visitRate; } public void setVisitRate(BigDecimal v) { this.visitRate = v; }
    public Integer getReportCount() { return reportCount; } public void setReportCount(Integer v) { this.reportCount = v; }
    public Integer getReportTarget() { return reportTarget; } public void setReportTarget(Integer v) { this.reportTarget = v; }
    public BigDecimal getReportRate() { return reportRate; } public void setReportRate(BigDecimal v) { this.reportRate = v; }
    public Integer getNewCustomerCount() { return newCustomerCount; } public void setNewCustomerCount(Integer v) { this.newCustomerCount = v; }
    public Integer getNewCustomerTarget() { return newCustomerTarget; } public void setNewCustomerTarget(Integer v) { this.newCustomerTarget = v; }
    public BigDecimal getNewCustomerRate() { return newCustomerRate; } public void setNewCustomerRate(BigDecimal v) { this.newCustomerRate = v; }
    public BigDecimal getCompositeScore() { return compositeScore; } public void setCompositeScore(BigDecimal v) { this.compositeScore = v; }
    public Integer getGrade() { return grade; } public void setGrade(Integer v) { this.grade = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}