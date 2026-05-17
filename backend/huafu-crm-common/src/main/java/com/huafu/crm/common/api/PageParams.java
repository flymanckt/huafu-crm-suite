package com.huafu.crm.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageParams(@Min(1) long pageNo, @Min(1) @Max(200) long pageSize) {
    public PageParams { if (pageNo == 0) pageNo = 1; if (pageSize == 0) pageSize = 20; }
}
