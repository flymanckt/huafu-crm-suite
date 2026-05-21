package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@TableName("crm_customer")
public class Customer {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String customerCode;
    private String customerName;
    private String customerShortName;
    private Integer type;
    private Integer level;
    private Integer status;
    private String province;
    private String city;
    private String district;
    private String address;
    private String mainContactName;
    private String mainContactPhone;
    private String mainContactRole;
    private BigDecimal annualRevenue;
    private BigDecimal creditLimit;
    private BigDecimal taxRate;
    private Integer paymentDays;
    private String sapCustomerCode;
    private String remark;
    private OffsetDateTime lastVisitTime;
    private Long ownerUserId;
    private OffsetDateTime publicPoolTime;
    // === P0 扩展字段 ===
    private String customerCategory;
    private String customerSegment;
    private Integer businessType;
    // 国家区域，例如中国、香港、海外。
    private String countryRegion;
    private String mainBrand;
    private BigDecimal annualYarnVolume;
    private Integer machineCount;
    private String productionCapacity;
    private String industryPosition;
    private String mainCustomerGroup;
    private String bundleCustomerName;
    private String bundleBrand;
    private Long bundleCustomerId;
    private String bundleCustomerSapCode;
    private Long ownerDeptId;
    private String salesMerchandiser;
    private BigDecimal locationLat;
    private BigDecimal locationLng;
    private String unifiedSocialCreditCode;
    private String englishName;
    private String assetType;
    private String customerSource;
    private Integer customerStage;
    private String competitorShareJson;
    private String cooperationBrandJson;
    private Integer blacklist;
    private Integer riskLevel;
    private String taxId;
    private String bankName;
    private String bankAccount;
    private String invoiceTitle;
    private String companyCode;
    private String salesGroup;
    private String priceList;
    private String currency;
    private String deliveryFactory;
    private String accountAssignmentGroup;
    private String taxClassification;
    private String shipToParty;
    private String soldToParty;
    private String payerParty;
    private String countryCode;
    // 历史兼容字段，前台不再使用；国家区域统一使用 countryRegion。
    private String region;
    // === 审计字段 ===
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedTime;
    @TableLogic
    private Integer deleted;
    private Long tenantId;
    @Version
    private Integer version;

    // Getters and Setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCustomerCode() { return customerCode; } public void setCustomerCode(String v) { this.customerCode = v; }
    public String getCustomerName() { return customerName; } public void setCustomerName(String v) { this.customerName = v; }
    public String getCustomerShortName() { return customerShortName; } public void setCustomerShortName(String v) { this.customerShortName = v; }
    public Integer getType() { return type; } public void setType(Integer v) { this.type = v; }
    public Integer getLevel() { return level; } public void setLevel(Integer v) { this.level = v; }
    public Integer getStatus() { return status; } public void setStatus(Integer v) { this.status = v; }
    public String getProvince() { return province; } public void setProvince(String v) { this.province = v; }
    public String getCity() { return city; } public void setCity(String v) { this.city = v; }
    public String getDistrict() { return district; } public void setDistrict(String v) { this.district = v; }
    public String getAddress() { return address; } public void setAddress(String v) { this.address = v; }
    public String getMainContactName() { return mainContactName; } public void setMainContactName(String v) { this.mainContactName = v; }
    public String getMainContactPhone() { return mainContactPhone; } public void setMainContactPhone(String v) { this.mainContactPhone = v; }
    public String getMainContactRole() { return mainContactRole; } public void setMainContactRole(String v) { this.mainContactRole = v; }
    public BigDecimal getAnnualRevenue() { return annualRevenue; } public void setAnnualRevenue(BigDecimal v) { this.annualRevenue = v; }
    public BigDecimal getCreditLimit() { return creditLimit; } public void setCreditLimit(BigDecimal v) { this.creditLimit = v; }
    public BigDecimal getTaxRate() { return taxRate; } public void setTaxRate(BigDecimal v) { this.taxRate = v; }
    public Integer getPaymentDays() { return paymentDays; } public void setPaymentDays(Integer v) { this.paymentDays = v; }
    public String getSapCustomerCode() { return sapCustomerCode; } public void setSapCustomerCode(String v) { this.sapCustomerCode = v; }
    public String getRemark() { return remark; } public void setRemark(String v) { this.remark = v; }
    public OffsetDateTime getLastVisitTime() { return lastVisitTime; } public void setLastVisitTime(OffsetDateTime v) { this.lastVisitTime = v; }
    public Long getOwnerUserId() { return ownerUserId; } public void setOwnerUserId(Long v) { this.ownerUserId = v; }
    public OffsetDateTime getPublicPoolTime() { return publicPoolTime; } public void setPublicPoolTime(OffsetDateTime v) { this.publicPoolTime = v; }
    public String getCustomerCategory() { return customerCategory; } public void setCustomerCategory(String v) { this.customerCategory = v; }
    public String getCustomerSegment() { return customerSegment; } public void setCustomerSegment(String v) { this.customerSegment = v; }
    public Integer getBusinessType() { return businessType; } public void setBusinessType(Integer v) { this.businessType = v; }
    public String getCountryRegion() { return countryRegion; } public void setCountryRegion(String v) { this.countryRegion = v; }
    public String getMainBrand() { return mainBrand; } public void setMainBrand(String v) { this.mainBrand = v; }
    public BigDecimal getAnnualYarnVolume() { return annualYarnVolume; } public void setAnnualYarnVolume(BigDecimal v) { this.annualYarnVolume = v; }
    public Integer getMachineCount() { return machineCount; } public void setMachineCount(Integer v) { this.machineCount = v; }
    public String getProductionCapacity() { return productionCapacity; } public void setProductionCapacity(String v) { this.productionCapacity = v; }
    public String getIndustryPosition() { return industryPosition; } public void setIndustryPosition(String v) { this.industryPosition = v; }
    public String getMainCustomerGroup() { return mainCustomerGroup; } public void setMainCustomerGroup(String v) { this.mainCustomerGroup = v; }
    public String getBundleCustomerName() { return bundleCustomerName; } public void setBundleCustomerName(String v) { this.bundleCustomerName = v; }
    public String getBundleBrand() { return bundleBrand; } public void setBundleBrand(String v) { this.bundleBrand = v; }
    public Long getBundleCustomerId() { return bundleCustomerId; } public void setBundleCustomerId(Long v) { this.bundleCustomerId = v; }
    public String getBundleCustomerSapCode() { return bundleCustomerSapCode; } public void setBundleCustomerSapCode(String v) { this.bundleCustomerSapCode = v; }
    public Long getOwnerDeptId() { return ownerDeptId; } public void setOwnerDeptId(Long v) { this.ownerDeptId = v; }
    public String getSalesMerchandiser() { return salesMerchandiser; } public void setSalesMerchandiser(String v) { this.salesMerchandiser = v; }
    public BigDecimal getLocationLat() { return locationLat; } public void setLocationLat(BigDecimal v) { this.locationLat = v; }
    public BigDecimal getLocationLng() { return locationLng; } public void setLocationLng(BigDecimal v) { this.locationLng = v; }
    public String getUnifiedSocialCreditCode() { return unifiedSocialCreditCode; } public void setUnifiedSocialCreditCode(String v) { this.unifiedSocialCreditCode = v; }
    public String getEnglishName() { return englishName; } public void setEnglishName(String v) { this.englishName = v; }
    public String getAssetType() { return assetType; } public void setAssetType(String v) { this.assetType = v; }
    public String getCustomerSource() { return customerSource; } public void setCustomerSource(String v) { this.customerSource = v; }
    public Integer getCustomerStage() { return customerStage; } public void setCustomerStage(Integer v) { this.customerStage = v; }
    public String getCompetitorShareJson() { return competitorShareJson; } public void setCompetitorShareJson(String v) { this.competitorShareJson = v; }
    public String getCooperationBrandJson() { return cooperationBrandJson; } public void setCooperationBrandJson(String v) { this.cooperationBrandJson = v; }
    public Integer getBlacklist() { return blacklist; } public void setBlacklist(Integer v) { this.blacklist = v; }
    public Integer getRiskLevel() { return riskLevel; } public void setRiskLevel(Integer v) { this.riskLevel = v; }
    public String getTaxId() { return taxId; } public void setTaxId(String v) { this.taxId = v; }
    public String getBankName() { return bankName; } public void setBankName(String v) { this.bankName = v; }
    public String getBankAccount() { return bankAccount; } public void setBankAccount(String v) { this.bankAccount = v; }
    public String getInvoiceTitle() { return invoiceTitle; } public void setInvoiceTitle(String v) { this.invoiceTitle = v; }
    public String getCompanyCode() { return companyCode; } public void setCompanyCode(String v) { this.companyCode = v; }
    public String getSalesGroup() { return salesGroup; } public void setSalesGroup(String v) { this.salesGroup = v; }
    public String getPriceList() { return priceList; } public void setPriceList(String v) { this.priceList = v; }
    public String getCurrency() { return currency; } public void setCurrency(String v) { this.currency = v; }
    public String getDeliveryFactory() { return deliveryFactory; } public void setDeliveryFactory(String v) { this.deliveryFactory = v; }
    public String getAccountAssignmentGroup() { return accountAssignmentGroup; } public void setAccountAssignmentGroup(String v) { this.accountAssignmentGroup = v; }
    public String getTaxClassification() { return taxClassification; } public void setTaxClassification(String v) { this.taxClassification = v; }
    public String getShipToParty() { return shipToParty; } public void setShipToParty(String v) { this.shipToParty = v; }
    public String getSoldToParty() { return soldToParty; } public void setSoldToParty(String v) { this.soldToParty = v; }
    public String getPayerParty() { return payerParty; } public void setPayerParty(String v) { this.payerParty = v; }
    public String getCountryCode() { return countryCode; } public void setCountryCode(String v) { this.countryCode = v; }
    public String getRegion() { return region; } public void setRegion(String v) { this.region = v; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String v) { this.createdBy = v; }
    public OffsetDateTime getCreatedTime() { return createdTime; } public void setCreatedTime(OffsetDateTime v) { this.createdTime = v; }
    public String getUpdatedBy() { return updatedBy; } public void setUpdatedBy(String v) { this.updatedBy = v; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; } public void setUpdatedTime(OffsetDateTime v) { this.updatedTime = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
    public Long getTenantId() { return tenantId; } public void setTenantId(Long v) { this.tenantId = v; }
    public Integer getVersion() { return version; } public void setVersion(Integer v) { this.version = v; }
}
