package com.huafu.crm.performance.service;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.performance.dto.DailyReportCreateDTO;
import com.huafu.crm.performance.query.DailyReportQuery;
import com.huafu.crm.performance.vo.DailyReportVO;

public interface DailyReportService {
    DailyReportVO create(DailyReportCreateDTO dto);
    DailyReportVO getById(Long id);
    DailyReportVO update(Long id, DailyReportCreateDTO dto);
    Boolean delete(Long id);
    PageResult<DailyReportVO> page(DailyReportQuery query);
}
