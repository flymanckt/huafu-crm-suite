package com.huafu.crm.performance.service.impl;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.performance.dto.DailyReportCreateDTO;
import com.huafu.crm.performance.entity.DailyReport;
import com.huafu.crm.performance.mapper.DailyReportMapper;
import com.huafu.crm.performance.query.DailyReportQuery;
import com.huafu.crm.performance.service.DailyReportService;
import com.huafu.crm.performance.vo.DailyReportVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyReportServiceImpl implements DailyReportService {
    private final DailyReportMapper mapper;
    public DailyReportServiceImpl(DailyReportMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public DailyReportVO create(DailyReportCreateDTO dto) {
        DailyReport e = new DailyReport();
        e.setUserId(dto.userId());
        e.setReportDate(dto.reportDate());
        e.setContentText(dto.contentText());
        e.setParseStatus(0);
        mapper.insert(e);
        return toVO(e);
    }

    @Override @Transactional(readOnly = true)
    public DailyReportVO getById(Long id) {
        DailyReport e = mapper.selectById(id);
        if (e == null) throw new BizException(3001, "日报不存在");
        return toVO(e);
    }

    @Override @Transactional
    public DailyReportVO update(Long id, DailyReportCreateDTO dto) {
        DailyReport e = mapper.selectById(id);
        if (e == null) throw new BizException(3001, "日报不存在");
        e.setUserId(dto.userId());
        e.setReportDate(dto.reportDate());
        e.setContentText(dto.contentText());
        if (e.getVersion() == null) e.setVersion(0);
        mapper.updateById(e);
        DailyReport refreshed = mapper.selectById(id);
        return toVO(refreshed);
    }

    @Override @Transactional
    public Boolean delete(Long id) {
        DailyReport e = mapper.selectById(id);
        if (e == null) throw new BizException(3001, "日报不存在");
        return mapper.deleteById(id) > 0;
    }

    @Override @Transactional(readOnly = true)
    public PageResult<DailyReportVO> page(DailyReportQuery query) {
        Page<DailyReport> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<DailyReport> qw = new LambdaQueryWrapper<>();
        if (query.userId() != null) qw.eq(DailyReport::getUserId, query.userId());
        if (query.parseStatus() != null) qw.eq(DailyReport::getParseStatus, query.parseStatus());
        qw.orderByDesc(DailyReport::getReportDate);
        Page<DailyReport> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    private DailyReportVO toVO(DailyReport e) {
        return new DailyReportVO(e.getId(), e.getReportNo(), e.getUserId(), e.getReportDate(),
            e.getContentText(), e.getParseStatus(), e.getParsedJson(),
            e.getOpportunityCount(), e.getMarketIntelligenceCount(), e.getLostOrderCount(),
            e.getParseError(), e.getParseTime(), e.getWecomMsgId(),
            e.getCreatedBy(), e.getCreatedTime(), e.getVersion());
    }
}
