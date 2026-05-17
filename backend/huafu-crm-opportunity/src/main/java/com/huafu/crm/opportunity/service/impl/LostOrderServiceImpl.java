package com.huafu.crm.opportunity.service.impl;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.opportunity.dto.LostOrderCreateDTO;
import com.huafu.crm.opportunity.entity.LostOrder;
import com.huafu.crm.opportunity.mapper.LostOrderMapper;
import com.huafu.crm.opportunity.query.LostOrderQuery;
import com.huafu.crm.opportunity.service.LostOrderService;
import com.huafu.crm.opportunity.vo.LostOrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LostOrderServiceImpl implements LostOrderService {
    private final LostOrderMapper mapper;
    public LostOrderServiceImpl(LostOrderMapper mapper) { this.mapper = mapper; }

    @Override @Transactional
    public LostOrderVO create(LostOrderCreateDTO dto) {
        LostOrder e = new LostOrder();
        e.setOpportunityId(dto.opportunityId());
        e.setCustomerId(dto.customerId());
        e.setLostType(dto.lostType());
        e.setCompetitorName(dto.competitorName());
        e.setCompetitorPrice(dto.competitorPrice());
        e.setOurPrice(dto.ourPrice());
        e.setMarginDiff(dto.marginDiff());
        e.setCompetitorDiscount(dto.competitorDiscount());
        e.setOurDiscount(dto.ourDiscount());
        e.setReasonDetail(dto.reasonDetail());
        e.setRecoveryPossible(dto.recoveryPossible());
        e.setFollowUpDate(dto.followUpDate());
        e.setHandlerUserId(dto.handlerUserId());
        mapper.insert(e);
        return toVO(e);
    }

    @Override @Transactional(readOnly = true)
    public PageResult<LostOrderVO> page(LostOrderQuery query) {
        Page<LostOrder> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<LostOrder> qw = new LambdaQueryWrapper<>();
        if (query.lostType() != null) qw.eq(LostOrder::getLostType, query.lostType());
        qw.orderByDesc(LostOrder::getCreatedTime);
        Page<LostOrder> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    private LostOrderVO toVO(LostOrder e) {
        return new LostOrderVO(e.getId(), e.getLostNo(), e.getOpportunityId(),
            e.getCustomerId(), e.getLostType(), e.getCompetitorName(),
            e.getCompetitorPrice(), e.getOurPrice(), e.getMarginDiff(),
            e.getCompetitorDiscount(), e.getOurDiscount(), e.getReasonDetail(),
            e.getRecoveryPossible(), e.getFollowUpDate(), e.getHandlerUserId(),
            e.getCreatedBy(), e.getCreatedTime(), e.getVersion());
    }
}
