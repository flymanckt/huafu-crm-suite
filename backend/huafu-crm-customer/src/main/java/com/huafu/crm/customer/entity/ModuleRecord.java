package com.huafu.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@TableName("crm_module_record")
public class ModuleRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String moduleKey;
    private String recordNo;
    private String title;
    private String status;
    private String ownerName;
    private LocalDate recordDate;
    private String payloadJson;
    private String remark;
    private OffsetDateTime createdTime;
    private OffsetDateTime updatedTime;
    private Integer deleted;
    private Long tenantId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }
    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public OffsetDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(OffsetDateTime createdTime) { this.createdTime = createdTime; }
    public OffsetDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(OffsetDateTime updatedTime) { this.updatedTime = updatedTime; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
