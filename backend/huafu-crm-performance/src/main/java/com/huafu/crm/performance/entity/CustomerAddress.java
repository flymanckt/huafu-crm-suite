package com.huafu.crm.performance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("crm_customer_address")
public class CustomerAddress {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long customerId;
    private Integer addressType;
    private String fullAddress;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer locationVerified;
    private Integer checkinRadiusMeters;
    @TableLogic private Integer deleted;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; } public void setCustomerId(Long v) { this.customerId = v; }
    public Integer getAddressType() { return addressType; } public void setAddressType(Integer v) { this.addressType = v; }
    public String getFullAddress() { return fullAddress; } public void setFullAddress(String v) { this.fullAddress = v; }
    public BigDecimal getLongitude() { return longitude; } public void setLongitude(BigDecimal v) { this.longitude = v; }
    public BigDecimal getLatitude() { return latitude; } public void setLatitude(BigDecimal v) { this.latitude = v; }
    public Integer getLocationVerified() { return locationVerified; } public void setLocationVerified(Integer v) { this.locationVerified = v; }
    public Integer getCheckinRadiusMeters() { return checkinRadiusMeters; } public void setCheckinRadiusMeters(Integer v) { this.checkinRadiusMeters = v; }
    public Integer getDeleted() { return deleted; } public void setDeleted(Integer v) { this.deleted = v; }
}
