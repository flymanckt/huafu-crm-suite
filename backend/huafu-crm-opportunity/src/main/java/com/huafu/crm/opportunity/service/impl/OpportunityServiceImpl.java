package com.huafu.crm.opportunity.service.impl;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.opportunity.dto.OpportunityCreateDTO;
import com.huafu.crm.opportunity.dto.StageUpdateDTO;
import com.huafu.crm.opportunity.entity.LostOrder;
import com.huafu.crm.opportunity.entity.Opportunity;
import com.huafu.crm.opportunity.mapper.LostOrderMapper;
import com.huafu.crm.opportunity.mapper.OpportunityMapper;
import com.huafu.crm.opportunity.query.OpportunityQuery;
import com.huafu.crm.opportunity.service.OpportunityService;
import com.huafu.crm.opportunity.vo.OpportunityVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class OpportunityServiceImpl implements OpportunityService {
    private final OpportunityMapper mapper;
    private final LostOrderMapper lostOrderMapper;
    private final JdbcTemplate jdbcTemplate;
    public OpportunityServiceImpl(OpportunityMapper mapper, LostOrderMapper lostOrderMapper, JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.lostOrderMapper = lostOrderMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override @Transactional
    public OpportunityVO create(OpportunityCreateDTO dto) {
        Opportunity e = new Opportunity();
        e.setOpportunityName(dto.opportunityName());
        e.setCustomerId(dto.customerId());
        e.setHandlerUserId(dto.handlerUserId());
        e.setStage(dto.stage());
        e.setStageUpdateTime(OffsetDateTime.now());
        e.setProductName(dto.productName());
        e.setQuantity(dto.quantity());
        e.setUnit(dto.unit());
        e.setEstimatedAmount(dto.estimatedAmount());
        e.setExpectedCloseDate(dto.expectedCloseDate());
        e.setWinProbability(dto.winProbability());
        e.setRemark(dto.remark());
        e.setSourceLeadId(dto.sourceLeadId());
        mapper.insert(e);
        return toVO(e);
    }

    @Override @Transactional(readOnly = true)
    public OpportunityVO getById(Long id) {
        Opportunity e = mapper.selectById(id);
        if (e == null) throw new BizException(2001, "商机不存在");
        return toVO(e);
    }

    @Override @Transactional
    public OpportunityVO update(Long id, OpportunityCreateDTO dto) {
        Opportunity e = mapper.selectById(id);
        if (e == null) throw new BizException(2001, "商机不存在");
        e.setOpportunityName(dto.opportunityName());
        e.setCustomerId(dto.customerId());
        e.setHandlerUserId(dto.handlerUserId());
        if (dto.stage() != null && !dto.stage().equals(e.getStage())) {
            e.setStage(dto.stage());
            e.setStageUpdateTime(OffsetDateTime.now());
        }
        e.setProductName(dto.productName());
        e.setQuantity(dto.quantity());
        e.setUnit(dto.unit());
        e.setEstimatedAmount(dto.estimatedAmount());
        e.setExpectedCloseDate(dto.expectedCloseDate());
        e.setWinProbability(dto.winProbability());
        e.setRemark(dto.remark());
        e.setSourceLeadId(dto.sourceLeadId());
        if (e.getVersion() == null) e.setVersion(0);
        mapper.updateById(e);
        Opportunity refreshed = mapper.selectById(id);
        return toVO(refreshed);
    }

    @Override @Transactional
    public Boolean delete(Long id) {
        Opportunity e = mapper.selectById(id);
        if (e == null) throw new BizException(2001, "商机不存在");
        return mapper.deleteById(id) > 0;
    }

    @Override @Transactional(readOnly = true)
    public PageResult<OpportunityVO> page(OpportunityQuery query) {
        Page<Opportunity> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<Opportunity> qw = new LambdaQueryWrapper<>();
        if (query.stage() != null) qw.eq(Opportunity::getStage, query.stage());
        if (query.customerId() != null) qw.eq(Opportunity::getCustomerId, query.customerId());
        qw.orderByDesc(Opportunity::getCreatedTime);
        Page<Opportunity> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    @Transactional
    public OpportunityVO updateStage(Long id, Integer stage, StageUpdateDTO dto) {
        Opportunity e = mapper.selectById(id);
        if (e == null) throw new BizException(2001, "商机不存在");
        // 阶段推进校验：不允许跳过中间阶段（只能顺序推进或直接到0丢单）
        // stage: 0=丢单, 1=初步接触, 2=需求确认, 3=方案报价, 4=合同谈判, 5=成交
        if (stage != 0 && stage > e.getStage() + 1) {
            throw new BizException(2001, "不允许跳过中间阶段，请按顺序推进");
        }
        e.setStage(stage);
        e.setStageUpdateTime(OffsetDateTime.now());
        if (e.getVersion() == null) e.setVersion(0);
        // stage=5 成交：设置成交日期和实际金额
        if (stage == 5) {
            e.setActualCloseDate(LocalDate.now());
            if (dto != null && dto.actualAmount() != null) {
                // actualAmount 可用于后续回写，但当前表没有该字段，仅记录于remark
            }
        }
        // stage=0 丢单：自动创建丢单记录
        if (stage == 0) {
            LostOrder lo = new LostOrder();
            lo.setLostNo("LO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            lo.setOpportunityId(e.getId());
            lo.setCustomerId(e.getCustomerId());
            lo.setHandlerUserId(e.getHandlerUserId());
            lo.setCompetitorName(dto != null ? dto.competitorName() : null);
            if (dto != null && dto.lostType() != null) lo.setLostType(dto.lostType());
            if (dto != null && dto.lostReason() != null) lo.setReasonDetail(dto.lostReason());
            lo.setOurPrice(e.getEstimatedAmount());
            if (lo.getTenantId() == null) lo.setTenantId(1L);
            lostOrderMapper.insert(lo);
        }
        mapper.updateById(e);
        Opportunity refreshed = mapper.selectById(id);
        return toVO(refreshed);
    }

    @Override
    @Transactional
    public OpportunityVO updateStageSimple(Long id, Integer stage) {
        return updateStage(id, stage, null);
    }

    private OpportunityVO toVO(Opportunity e) {
        return new OpportunityVO(e.getId(), e.getOppNo(), e.getOpportunityName(),
            e.getCustomerId(), e.getHandlerUserId(), e.getStage(), e.getStageUpdateTime(),
            e.getProductName(), e.getQuantity(), e.getUnit(), e.getEstimatedAmount(),
            e.getExpectedCloseDate(), e.getActualCloseDate(), e.getWinProbability(),
            e.getLostReason(), e.getLostType(), e.getCompetitorName(), e.getQuoteId(),
            e.getOrderNo(), e.getRemark(), e.getSourceLeadId(),
            e.getCreatedBy(), e.getCreatedTime(), e.getVersion(), customerName(e.getCustomerId()));
    }

    private String customerName(Long customerId) {
        if (customerId == null) {
            return null;
        }
        try {
            return jdbcTemplate.query("""
                    SELECT customer_name
                    FROM crm_customer
                    WHERE id = ? AND COALESCE(deleted, 0) = 0
                    LIMIT 1
                    """, rs -> rs.next() ? rs.getString("customer_name") : null, customerId);
        } catch (Exception ignored) {
            return null;
        }
    }
}
