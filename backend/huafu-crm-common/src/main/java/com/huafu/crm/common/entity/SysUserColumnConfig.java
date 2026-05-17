package com.huafu.crm.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_user_column_config")
public class SysUserColumnConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String pageCode;
    private String columnConfigs;
    private Integer isDefault;
    private String configName;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private OffsetDateTime updatedAt;
    @Version
    private Integer version;
}
