package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_customer_transfer")
public class CustomerTransfer {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long customerId;
    private Long fromUserId;
    private Long toUserId;
    private Long supervisorUserId;
    private LocalDate transferDate;
    private String reason;
    private String businessSummary;
    private String contactInfoSnapshot;
    private String receivableInfo;
    private String cooperationIssues;
    private String futureOpportunities;
    private String handoverAttachments;
    private Integer status;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public Long getFromUserId() { return fromUserId; } public void setFromUserId(Long v) { this.fromUserId = v; }
    public Long getToUserId() { return toUserId; } public void setToUserId(Long v) { this.toUserId = v; }
    public Long getSupervisorUserId() { return supervisorUserId; } public void setSupervisorUserId(Long v) { this.supervisorUserId = v; }
    public LocalDate getTransferDate() { return transferDate; } public void setTransferDate(LocalDate v) { this.transferDate = v; }
    public String getReason() { return reason; } public void setReason(String v) { this.reason = v; }
    public String getBusinessSummary() { return businessSummary; } public void setBusinessSummary(String v) { this.businessSummary = v; }
    public String getContactInfoSnapshot() { return contactInfoSnapshot; } public void setContactInfoSnapshot(String v) { this.contactInfoSnapshot = v; }
    public String getReceivableInfo() { return receivableInfo; } public void setReceivableInfo(String v) { this.receivableInfo = v; }
    public String getCooperationIssues() { return cooperationIssues; } public void setCooperationIssues(String v) { this.cooperationIssues = v; }
    public String getFutureOpportunities() { return futureOpportunities; } public void setFutureOpportunities(String v) { this.futureOpportunities = v; }
    public String getHandoverAttachments() { return handoverAttachments; } public void setHandoverAttachments(String v) { this.handoverAttachments = v; }
    public Integer getStatus() { return status; } public void setStatus(Integer v) { this.status = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
