package com.huafu.crm.opportunity.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("crm_lead")
public class Lead {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String leadNo;
    private Integer leadType;
    private Long customerId;
    private String customerName;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String productName;
    private String competitorName;
    private BigDecimal competitorPrice;
    private BigDecimal competitorDiscount;
    private BigDecimal marginRate;
    private Integer source;
    private Long creatorUserId;
    private Long handlerUserId;
    private Integer status;
    private OffsetDateTime convertTime;
    private Long convertedOppId;
    private String remark;
    private String rawText;
    private String parsedJson;
    @TableField(fill = FieldFill.INSERT) private String createdBy;
    @TableField(fill = FieldFill.INSERT) private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private OffsetDateTime updatedTime;
    @TableLogic private Integer deleted;
    private Long tenantId;
    @Version private Integer version;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getLeadNo() { return leadNo; } public void setLeadNo(String v) { this.leadNo = v; }
    public Integer getLeadType() { return leadType; } public void setLeadType(Integer v) { this.leadType = v; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String v) { this.customerName = v; }
    public String getContactName() { return contactName; } public void setContactName(String v) { this.contactName = v; }
    public String getContactPhone() { return contactPhone; } public void setContactPhone(String v) { this.contactPhone = v; }
    public String getProvince() { return province; } public void setProvince(String v) { this.province = v; }
    public String getCity() { return city; } public void setCity(String v) { this.city = v; }
    public String getProductName() { return productName; } public void setProductName(String v) { this.productName = v; }
    public String getCompetitorName() { return competitorName; } public void setCompetitorName(String v) { this.competitorName = v; }
    public BigDecimal getCompetitorPrice() { return competitorPrice; } public void setCompetitorPrice(BigDecimal v) { this.competitorPrice = v; }
    public BigDecimal getCompetitorDiscount() { return competitorDiscount; } public void setCompetitorDiscount(BigDecimal v) { this.competitorDiscount = v; }
    public BigDecimal getMarginRate() { return marginRate; } public void setMarginRate(BigDecimal v) { this.marginRate = v; }
    public Integer getSource() { return source; } public void setSource(Integer v) { this.source = v; }
    public Long getCreatorUserId() { return creatorUserId; } public void setCreatorUserId(Long v) { this.creatorUserId = v; }
    public Long getHandlerUserId() { return handlerUserId; } public void setHandlerUserId(Long v) { this.handlerUserId = v; }
    public Integer getStatus() { return status; } public void setStatus(Integer v) { this.status = v; }
    public OffsetDateTime getConvertTime() { return convertTime; } public void setConvertTime(OffsetDateTime v) { this.convertTime = v; }
    public Long getConvertedOppId() { return convertedOppId; } public void setConvertedOppId(Long v) { this.convertedOppId = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public String getRawText() { return rawText; } public void setRawText(String v) { this.rawText = v; }
    public String getParsedJson() { return parsedJson; } public void setParsedJson(String v) { this.parsedJson = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}