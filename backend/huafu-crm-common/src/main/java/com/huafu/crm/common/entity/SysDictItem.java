package com.huafu.crm.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_dict_item")
public class SysDictItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dictId;
    private String itemCode;
    private String itemName;
    private String itemValue;
    private Integer sortOrder;
    private Integer status;
    private Integer defaultFlag;
    private Integer showCode;
    private String description;
    private String color;
    private String cssClass;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private OffsetDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    @Version
    private Integer version;
}
