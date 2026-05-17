package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.customer.entity.CustomerContact;
import com.huafu.crm.customer.mapper.CustomerContactMapper;
import com.huafu.crm.customer.service.CrmCustomerContactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrmCustomerContactServiceImpl implements CrmCustomerContactService {

    private final CustomerContactMapper mapper;

    public CrmCustomerContactServiceImpl(CustomerContactMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerContact> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CustomerContact>()
                .eq(CustomerContact::getCustomerId, customerId)
                .eq(CustomerContact::getDeleted, 0)
                .orderByDesc(CustomerContact::getIsMain)
                .orderByAsc(CustomerContact::getContactName));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CustomerContact> pageByCustomerId(Long customerId, long current, long size) {
        Page<CustomerContact> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerContact> qw = new LambdaQueryWrapper<>();
        qw.eq(CustomerContact::getCustomerId, customerId);
        qw.eq(CustomerContact::getDeleted, 0);
        qw.orderByDesc(CustomerContact::getIsMain);
        qw.orderByAsc(CustomerContact::getContactName);
        Page<CustomerContact> result = mapper.selectPage(page, qw);
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CustomerContact create(Long customerId, CustomerContact contact) {
        contact.setCustomerId(customerId);
        mapper.insert(contact);
        return contact;
    }

    @Override
    @Transactional
    public CustomerContact update(Long id, CustomerContact contact) {
        CustomerContact existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "联系人不存在");
        contact.setId(id);
        contact.setCustomerId(existing.getCustomerId());
        contact.setVersion(existing.getVersion());
        mapper.updateById(contact);
        return mapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerContact> getTreeByCustomerId(Long customerId) {
        List<CustomerContact> all = getByCustomerId(customerId);
        Map<Long, List<CustomerContact>> byParent = all.stream()
                .collect(Collectors.groupingBy(c -> c.getParentContactId() == null ? 0L : c.getParentContactId()));

        List<CustomerContact> roots = byParent.getOrDefault(0L, new ArrayList<>());
        return buildTree(roots, byParent);
    }

    private List<CustomerContact> buildTree(List<CustomerContact> nodes, Map<Long, List<CustomerContact>> byParent) {
        return nodes.stream().map(node -> {
            List<CustomerContact> children = byParent.getOrDefault(node.getId(), new ArrayList<>());
            if (!children.isEmpty()) {
                // 树形关系通过 parentContactId 建立，不修改 entity 本身
            }
            return node;
        }).collect(Collectors.toList());
    }
}
