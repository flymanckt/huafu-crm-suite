package com.huafu.crm.opportunity.service.impl;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.opportunity.dto.LeadCreateDTO;
import com.huafu.crm.opportunity.entity.Lead;
import com.huafu.crm.opportunity.mapper.LeadMapper;
import com.huafu.crm.opportunity.query.LeadQuery;
import com.huafu.crm.opportunity.service.LeadService;
import com.huafu.crm.opportunity.vo.LeadVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeadServiceImpl implements LeadService {
    private final LeadMapper mapper;
    public LeadServiceImpl(LeadMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public LeadVO create(LeadCreateDTO dto) {
        Lead e = new Lead();
        apply(e, dto);
        e.setStatus(1);
        mapper.insert(e);
        return toVO(e);
    }

    @Override @Transactional(readOnly = true)
    public LeadVO getById(Long id) {
        Lead e = mapper.selectById(id);
        if (e == null) throw new BizException(2001, "线索不存在");
        return toVO(e);
    }

    @Override @Transactional
    public LeadVO update(Long id, LeadCreateDTO dto) {
        Lead e = mapper.selectById(id);
        if (e == null) throw new BizException(2001, "线索不存在");
        apply(e, dto);
        mapper.updateById(e);
        return toVO(e);
    }

    @Override @Transactional
    public boolean delete(Long id) {
        Lead e = mapper.selectById(id);
        if (e == null) throw new BizException(2001, "线索不存在");
        return mapper.deleteById(id) > 0;
    }

    @Override @Transactional(readOnly = true)
    public PageResult<LeadVO> page(LeadQuery query) {
        Page<Lead> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<Lead> qw = new LambdaQueryWrapper<>();
        if (query.leadType() != null) qw.eq(Lead::getLeadType, query.leadType());
        if (query.status() != null) qw.eq(Lead::getStatus, query.status());
        qw.orderByDesc(Lead::getCreatedTime);
        Page<Lead> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    private LeadVO toVO(Lead e) {
        return new LeadVO(e.getId(), e.getLeadNo(), e.getLeadType(), e.getCustomerId(),
            e.getCustomerName(), e.getContactName(), e.getContactPhone(),
            e.getProvince(), e.getCity(), e.getProductName(), e.getCompetitorName(),
            e.getCompetitorPrice(), e.getCompetitorDiscount(), e.getMarginRate(),
            e.getSource(), e.getCreatorUserId(), e.getHandlerUserId(), e.getStatus(),
            e.getConvertTime(), e.getConvertedOppId(), e.getRemark(),
            e.getCreatedBy(), e.getCreatedTime(), e.getVersion());
    }

    private void apply(Lead e, LeadCreateDTO dto) {
        e.setLeadType(dto.leadType());
        e.setCustomerId(dto.customerId());
        e.setCustomerName(dto.customerName());
        e.setContactName(dto.contactName());
        e.setContactPhone(dto.contactPhone());
        e.setProvince(dto.province());
        e.setCity(dto.city());
        e.setProductName(dto.productName());
        e.setCompetitorName(dto.competitorName());
        e.setCompetitorPrice(dto.competitorPrice());
        e.setCompetitorDiscount(dto.competitorDiscount());
        e.setMarginRate(dto.marginRate());
        e.setSource(dto.source());
        e.setCreatorUserId(dto.creatorUserId());
        e.setHandlerUserId(dto.handlerUserId());
        e.setRemark(dto.remark());
    }
}
