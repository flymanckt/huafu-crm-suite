package com.huafu.crm.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_dict_type")
public class SysDictType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String dictCode;
    private String dictName;
    private String dictType;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private OffsetDateTime updatedAt;
    @TableLogic
    private Integer deleted;
    @Version
    private Integer version;
}
