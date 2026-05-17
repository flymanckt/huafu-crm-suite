package com.huafu.crm.performance.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.performance.query.PerformanceQuery;
import com.huafu.crm.performance.vo.PerformanceVO;

public interface PerformanceService {
    PageResult<PerformanceVO> page(PerformanceQuery query);
}
