package com.huafu.crm.opportunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.opportunity.dto.QuoteCreateDTO;
import com.huafu.crm.opportunity.dto.QuoteItemDTO;
import com.huafu.crm.opportunity.dto.QuoteUpdateDTO;
import com.huafu.crm.opportunity.entity.Quote;
import com.huafu.crm.opportunity.entity.QuoteItem;
import com.huafu.crm.opportunity.mapper.QuoteMapper;
import com.huafu.crm.opportunity.mapper.QuoteItemMapper;
import com.huafu.crm.opportunity.service.QuoteService;
import com.huafu.crm.opportunity.vo.QuoteItemVO;
import com.huafu.crm.opportunity.vo.QuoteVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class QuoteServiceImpl implements QuoteService {
    private final QuoteMapper quoteMapper;
    private final QuoteItemMapper quoteItemMapper;

    public QuoteServiceImpl(QuoteMapper quoteMapper, QuoteItemMapper quoteItemMapper) {
        this.quoteMapper = quoteMapper;
        this.quoteItemMapper = quoteItemMapper;
    }

    @Override
    @Transactional
    public QuoteVO create(QuoteCreateDTO dto) {
        Quote q = new Quote();
        q.setQuoteNo("QT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        q.setCustomerId(dto.customerId());
        q.setOpportunityId(dto.opportunityId());
        q.setContactId(dto.contactId());
        q.setContactName(dto.contactName());
        q.setContactPhone(dto.contactPhone());
        q.setSalesUserId(dto.salesUserId());
        q.setQuoteDate(dto.quoteDate());
        q.setValidDate(dto.validDate());
        q.setDiscountRate(dto.discountRate() != null ? dto.discountRate() : BigDecimal.ZERO);
        q.setPaymentTerms(dto.paymentTerms());
        q.setDeliveryTerms(dto.deliveryTerms());
        q.setRemark(dto.remark());
        q.setStatus(1); // 草稿
        if (q.getTenantId() == null) q.setTenantId(1L);
        quoteMapper.insert(q);
        return toVO(q);
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteVO getById(Long id) {
        Quote q = quoteMapper.selectById(id);
        if (q == null) throw new BizException(2002, "报价单不存在");
        return toVO(q);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<QuoteVO> page(Long customerId, Long salesUserId, Integer status, long current, long size) {
        Page<Quote> p = new Page<>(current, size);
        LambdaQueryWrapper<Quote> qw = new LambdaQueryWrapper<>();
        if (customerId != null) qw.eq(Quote::getCustomerId, customerId);
        if (salesUserId != null) qw.eq(Quote::getSalesUserId, salesUserId);
        if (status != null) qw.eq(Quote::getStatus, status);
        qw.orderByDesc(Quote::getCreatedTime);
        Page<Quote> result = quoteMapper.selectPage(p, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(),
            result.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    @Transactional
    public QuoteVO update(Long id, QuoteUpdateDTO dto) {
        Quote q = quoteMapper.selectById(id);
        if (q == null) throw new BizException(2002, "报价单不存在");
        if (q.getStatus() != 1) throw new BizException(2002, "只有草稿状态可编辑");
        if (dto.customerId() != null) q.setCustomerId(dto.customerId());
        if (dto.contactId() != null) q.setContactId(dto.contactId());
        if (dto.contactName() != null) q.setContactName(dto.contactName());
        if (dto.contactPhone() != null) q.setContactPhone(dto.contactPhone());
        if (dto.quoteDate() != null) q.setQuoteDate(dto.quoteDate());
        if (dto.validDate() != null) q.setValidDate(dto.validDate());
        if (dto.discountRate() != null) q.setDiscountRate(dto.discountRate());
        if (dto.paymentTerms() != null) q.setPaymentTerms(dto.paymentTerms());
        if (dto.deliveryTerms() != null) q.setDeliveryTerms(dto.deliveryTerms());
        if (dto.remark() != null) q.setRemark(dto.remark());
        quoteMapper.updateById(q);
        return toVO(quoteMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Quote q = quoteMapper.selectById(id);
        if (q == null) throw new BizException(2002, "报价单不存在");
        if (q.getStatus() != 1) throw new BizException(2002, "只有草稿状态可删除");
        quoteMapper.deleteById(id);
    }

    @Override
    @Transactional
    public QuoteVO send(Long id) {
        Quote q = quoteMapper.selectById(id);
        if (q == null) throw new BizException(2002, "报价单不存在");
        if (q.getStatus() != 1) throw new BizException(2002, "只能发送草稿状态报价单");
        q.setStatus(2); // 已发送
        q.setSentTime(OffsetDateTime.now());
        recalcAmount(q);
        quoteMapper.updateById(q);
        return toVO(quoteMapper.selectById(id));
    }

    @Override
    @Transactional
    public QuoteVO confirm(Long id) {
        Quote q = quoteMapper.selectById(id);
        if (q == null) throw new BizException(2002, "报价单不存在");
        if (q.getStatus() != 2) throw new BizException(2002, "只能确认已发送的报价单");
        q.setStatus(3); // 客户确认
        q.setConfirmedTime(OffsetDateTime.now());
        quoteMapper.updateById(q);
        return toVO(quoteMapper.selectById(id));
    }

    @Override
    @Transactional
    public QuoteItemVO addItem(Long quoteId, QuoteItemDTO dto) {
        Quote q = quoteMapper.selectById(quoteId);
        if (q == null) throw new BizException(2002, "报价单不存在");
        if (q.getStatus() != 1) throw new BizException(2002, "只有草稿状态可添加明细");
        QuoteItem item = new QuoteItem();
        item.setQuoteId(quoteId);
        item.setProductName(dto.productName());
        item.setProductCode(dto.productCode());
        item.setQuantity(dto.quantity());
        item.setUnit(dto.unit());
        item.setUnitPrice(dto.unitPrice());
        item.setAmount(dto.quantity().multiply(dto.unitPrice()));
        item.setLeadTime(dto.leadTime());
        item.setRemark(dto.remark());
        if (item.getTenantId() == null) item.setTenantId(1L);
        quoteItemMapper.insert(item);
        recalcAmount(q);
        return toItemVO(item);
    }

    @Override
    @Transactional
    public QuoteItemVO updateItem(Long itemId, QuoteItemDTO dto) {
        QuoteItem item = quoteItemMapper.selectById(itemId);
        if (item == null) throw new BizException(2002, "明细不存在");
        Quote q = quoteMapper.selectById(item.getQuoteId());
        if (q.getStatus() != 1) throw new BizException(2002, "只有草稿状态可编辑明细");
        item.setProductName(dto.productName());
        item.setProductCode(dto.productCode());
        item.setQuantity(dto.quantity());
        item.setUnit(dto.unit());
        item.setUnitPrice(dto.unitPrice());
        item.setAmount(dto.quantity().multiply(dto.unitPrice()));
        item.setLeadTime(dto.leadTime());
        item.setRemark(dto.remark());
        quoteItemMapper.updateById(item);
        recalcAmount(q);
        return toItemVO(quoteItemMapper.selectById(itemId));
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        QuoteItem item = quoteItemMapper.selectById(itemId);
        if (item == null) throw new BizException(2002, "明细不存在");
        Quote q = quoteMapper.selectById(item.getQuoteId());
        if (q.getStatus() != 1) throw new BizException(2002, "只有草稿状态可删除明细");
        quoteItemMapper.deleteById(itemId);
        recalcAmount(q);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteItemVO> getItems(Long quoteId) {
        List<QuoteItem> items = quoteItemMapper.selectList(
            new LambdaQueryWrapper<QuoteItem>().eq(QuoteItem::getQuoteId, quoteId));
        return items.stream().map(this::toItemVO).toList();
    }

    private void recalcAmount(Quote q) {
        List<QuoteItem> items = quoteItemMapper.selectList(
            new LambdaQueryWrapper<QuoteItem>().eq(QuoteItem::getQuoteId, q.getId()));
        BigDecimal total = items.stream().map(QuoteItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        q.setTotalAmount(total);
        BigDecimal discount = q.getDiscountRate() != null ? q.getDiscountRate() : BigDecimal.ZERO;
        q.setFinalAmount(total.multiply(BigDecimal.ONE.subtract(discount)));
        quoteMapper.updateById(q);
    }

    private QuoteVO toVO(Quote q) {
        List<QuoteItem> items = quoteItemMapper.selectList(
            new LambdaQueryWrapper<QuoteItem>().eq(QuoteItem::getQuoteId, q.getId()));
        List<QuoteItemVO> itemVOs = items.stream().map(this::toItemVO).toList();
        return new QuoteVO(q.getId(), q.getQuoteNo(), q.getCustomerId(), q.getOpportunityId(),
            q.getContactId(), q.getContactName(), q.getContactPhone(), q.getSalesUserId(),
            q.getQuoteDate(), q.getValidDate(), q.getTotalAmount(), q.getDiscountRate(),
            q.getFinalAmount(), q.getPaymentTerms(), q.getDeliveryTerms(), q.getStatus(),
            q.getSentTime(), q.getConfirmedTime(), q.getRemark(), itemVOs, q.getCreatedTime());
    }

    private QuoteItemVO toItemVO(QuoteItem item) {
        return new QuoteItemVO(item.getId(), item.getQuoteId(), item.getProductName(),
            item.getProductCode(), item.getQuantity(), item.getUnit(), item.getUnitPrice(),
            item.getAmount(), item.getLeadTime(), item.getRemark());
    }
}
