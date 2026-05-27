package com.huafu.crm.target.service.impl;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.target.dto.TargetCreateDTO;
import com.huafu.crm.target.entity.Target;
import com.huafu.crm.target.entity.TargetAchieve;
import com.huafu.crm.target.mapper.TargetMapper;
import com.huafu.crm.target.mapper.TargetAchieveMapper;
import com.huafu.crm.target.query.TargetQuery;
import com.huafu.crm.target.service.TargetService;
import com.huafu.crm.target.vo.TargetVO;
import com.huafu.crm.target.vo.TargetAchieveVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TargetServiceImpl implements TargetService {
    private final TargetMapper mapper;
    private final TargetAchieveMapper achieveMapper;
    public TargetServiceImpl(TargetMapper mapper, TargetAchieveMapper achieveMapper) {
        this.mapper = mapper; this.achieveMapper = achieveMapper;
    }

    @Override @Transactional
    public TargetVO create(TargetCreateDTO dto) {
        Target e = new Target();
        e.setTargetYear(dto.targetYear());
        e.setTargetMonth(dto.targetMonth());
        e.setDeptId(dto.deptId());
        e.setUserId(dto.userId());
        e.setProductCategory(dto.productCategory());
        e.setMetricType(dto.metricType());
        e.setTargetValue(dto.targetValue());
        mapper.insert(e);
        return toVO(e);
    }

    @Override @Transactional(readOnly = true)
    public TargetVO getById(Long id) {
        Target e = mapper.selectById(id);
        if (e == null) throw new BizException(5001, "目标不存在");
        return toVO(e);
    }

    @Override @Transactional
    public TargetVO update(Long id, TargetCreateDTO dto) {
        Target e = mapper.selectById(id);
        if (e == null) throw new BizException(5001, "目标不存在");
        e.setTargetYear(dto.targetYear());
        e.setTargetMonth(dto.targetMonth());
        e.setDeptId(dto.deptId());
        e.setUserId(dto.userId());
        e.setProductCategory(dto.productCategory());
        e.setMetricType(dto.metricType());
        e.setTargetValue(dto.targetValue());
        if (e.getVersion() == null) e.setVersion(0);
        mapper.updateById(e);
        Target refreshed = mapper.selectById(id);
        return toVO(refreshed);
    }

    @Override @Transactional
    public Boolean delete(Long id) {
        Target e = mapper.selectById(id);
        if (e == null) throw new BizException(5001, "目标不存在");
        return mapper.deleteById(id) > 0;
    }

    @Override @Transactional(readOnly = true)
    public PageResult<TargetVO> page(TargetQuery query) {
        Page<Target> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<Target> qw = new LambdaQueryWrapper<>();
        if (query.targetYear() != null) qw.eq(Target::getTargetYear, query.targetYear());
        if (query.targetMonth() != null) qw.eq(Target::getTargetMonth, query.targetMonth());
        qw.orderByDesc(Target::getCreatedTime);
        Page<Target> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    @Override @Transactional(readOnly = true)
    public PageResult<TargetAchieveVO> achievePage(Long targetId, Integer current, Integer size) {
        Page<TargetAchieve> page = new Page<>(current, size);
        LambdaQueryWrapper<TargetAchieve> qw = new LambdaQueryWrapper<TargetAchieve>()
            .eq(TargetAchieve::getTargetId, targetId)
            .orderByDesc(TargetAchieve::getAchieveYear);
        Page<TargetAchieve> result = achieveMapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toAchieveVO).toList());
    }

    private TargetVO toVO(Target e) {
        return new TargetVO(e.getId(), e.getTargetNo(), e.getTargetYear(), e.getTargetMonth(),
            e.getDeptId(), e.getUserId(), e.getProductCategory(), e.getMetricType(),
            e.getTargetValue(), e.getCreatedBy(), e.getCreatedTime(), e.getVersion());
    }

    private TargetAchieveVO toAchieveVO(TargetAchieve e) {
        return new TargetAchieveVO(e.getId(), e.getTargetId(), e.getAchieveYear(), e.getAchieveMonth(),
            e.getAchieveValue(), e.getAchieveRate(), e.getSource(),
            e.getSourceDataDate(), e.getSyncedTime(), e.getRemark(),
            e.getCreatedBy(), e.getCreatedTime(), e.getVersion());
    }
}
