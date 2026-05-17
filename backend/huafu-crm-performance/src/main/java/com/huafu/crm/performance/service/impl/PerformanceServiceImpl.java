package com.huafu.crm.performance.service.impl;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.performance.entity.Performance;
import com.huafu.crm.performance.mapper.PerformanceMapper;
import com.huafu.crm.performance.query.PerformanceQuery;
import com.huafu.crm.performance.service.PerformanceService;
import com.huafu.crm.performance.vo.PerformanceVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerformanceServiceImpl implements PerformanceService {
    private final PerformanceMapper mapper;
    public PerformanceServiceImpl(PerformanceMapper mapper) { this.mapper = mapper; }

    @Override @Transactional(readOnly = true)
    public PageResult<PerformanceVO> page(PerformanceQuery query) {
        Page<Performance> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<Performance> qw = new LambdaQueryWrapper<>();
        if (query.statYear() != null) qw.eq(Performance::getStatYear, query.statYear());
        if (query.statMonth() != null) qw.eq(Performance::getStatMonth, query.statMonth());
        qw.orderByAsc(Performance::getCompositeScore);
        Page<Performance> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    private PerformanceVO toVO(Performance e) {
        return new PerformanceVO(e.getId(), e.getUserId(), e.getStatYear(), e.getStatMonth(),
            e.getVisitCount(), e.getVisitTarget(), e.getVisitRate(),
            e.getReportCount(), e.getReportTarget(), e.getReportRate(),
            e.getNewCustomerCount(), e.getNewCustomerTarget(), e.getNewCustomerRate(),
            e.getCompositeScore(), e.getGrade(),
            e.getCreatedBy(), e.getCreatedTime(), e.getVersion());
    }
}
