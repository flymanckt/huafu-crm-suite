package com.huafu.crm.performance.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.performance.dto.VisitRecordCreateDTO;
import com.huafu.crm.performance.query.VisitRecordQuery;
import com.huafu.crm.performance.vo.VisitRecordVO;

public interface VisitRecordService {
    VisitRecordVO create(VisitRecordCreateDTO dto);
    PageResult<VisitRecordVO> page(VisitRecordQuery query);
}
