package com.huafu.crm.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_user_filter_config")
public class SysUserFilterConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String pageCode;
    private String filterConfigs;
    private String configName;
    private Integer isDefault;
    private Long createdBy;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private OffsetDateTime updatedAt;
    @Version
    private Integer version;
}
