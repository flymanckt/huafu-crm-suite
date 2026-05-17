package com.huafu.crm.opportunity.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_quote")
public class Quote {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String quoteNo;
    private Long customerId;
    private Long opportunityId;
    private Long contactId;
    private String contactName;
    private String contactPhone;
    private Long salesUserId;
    private LocalDate quoteDate;
    private LocalDate validDate;
    private BigDecimal totalAmount;
    private BigDecimal discountRate;
    private BigDecimal finalAmount;
    private String paymentTerms;
    private String deliveryTerms;
    private Integer status;
    private OffsetDateTime sentTime;
    private OffsetDateTime confirmedTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getQuoteNo() { return quoteNo; } public void setQuoteNo(String v) { this.quoteNo = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public Long getOpportunityId() { return opportunityId; } public void setOpportunityId(Long v) { this.opportunityId = v; }
    public Long getContactId() { return contactId; } public void setContactId(Long v) { this.contactId = v; }
    public String getContactName() { return contactName; } public void setContactName(String v) { this.contactName = v; }
    public String getContactPhone() { return contactPhone; } public void setContactPhone(String v) { this.contactPhone = v; }
    public Long getSalesUserId() { return salesUserId; } public void setSalesUserId(Long v) { this.salesUserId = v; }
    public LocalDate getQuoteDate() { return quoteDate; } public void setQuoteDate(LocalDate v) { this.quoteDate = v; }
    public LocalDate getValidDate() { return validDate; } public void setValidDate(LocalDate v) { this.validDate = v; }
    public BigDecimal getTotalAmount() { return totalAmount; } public void setTotalAmount(BigDecimal v) { this.totalAmount = v; }
    public BigDecimal getDiscountRate() { return discountRate; } public void setDiscountRate(BigDecimal v) { this.discountRate = v; }
    public BigDecimal getFinalAmount() { return finalAmount; } public void setFinalAmount(BigDecimal v) { this.finalAmount = v; }
    public String getPaymentTerms() { return paymentTerms; } public void setPaymentTerms(String v) { this.paymentTerms = v; }
    public String getDeliveryTerms() { return deliveryTerms; } public void setDeliveryTerms(String v) { this.deliveryTerms = v; }
    public Integer getStatus() { return status; } public void setStatus(Integer v) { this.status = v; }
    public OffsetDateTime getSentTime() { return sentTime; } public void setSentTime(OffsetDateTime v) { this.sentTime = v; }
    public OffsetDateTime getConfirmedTime() { return confirmedTime; } public void setConfirmedTime(OffsetDateTime v) { this.confirmedTime = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}