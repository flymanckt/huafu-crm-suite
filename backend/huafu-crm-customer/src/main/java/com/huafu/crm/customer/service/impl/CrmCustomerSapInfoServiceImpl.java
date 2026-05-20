package com.huafu.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.context.UserContext;
import com.huafu.crm.common.entity.CrmCustomerSapInfo;
import com.huafu.crm.common.entity.CrmCustomerSapOrg;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.util.InputSanitizer;
import com.huafu.crm.customer.mapper.CrmCustomerSapInfoMapper;
import com.huafu.crm.customer.entity.Customer;
import com.huafu.crm.customer.entity.IntegrationInterface;
import com.huafu.crm.customer.entity.IntegrationLog;
import com.huafu.crm.customer.mapper.CrmCustomerSapOrgMapper;
import com.huafu.crm.customer.mapper.CustomerMapper;
import com.huafu.crm.customer.mapper.IntegrationInterfaceMapper;
import com.huafu.crm.customer.mapper.IntegrationLogMapper;
import com.huafu.crm.customer.service.CrmCustomerSapInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CrmCustomerSapInfoServiceImpl implements CrmCustomerSapInfoService {

    private static final String SAP_CUS_INTERFACE_CODE = "SAP_CUS";

    private final CrmCustomerSapInfoMapper mapper;
    private final CustomerMapper customerMapper;
    private final CrmCustomerSapOrgMapper sapOrgMapper;
    private final IntegrationInterfaceMapper interfaceMapper;
    private final IntegrationLogMapper logMapper;
    private final UserContext userContext;
    private final ObjectMapper objectMapper;

    public CrmCustomerSapInfoServiceImpl(
            CrmCustomerSapInfoMapper mapper,
            CustomerMapper customerMapper,
            CrmCustomerSapOrgMapper sapOrgMapper,
            IntegrationInterfaceMapper interfaceMapper,
            IntegrationLogMapper logMapper,
            UserContext userContext,
            ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.customerMapper = customerMapper;
        this.sapOrgMapper = sapOrgMapper;
        this.interfaceMapper = interfaceMapper;
        this.logMapper = logMapper;
        this.userContext = userContext;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCustomerSapInfo> getByCustomerId(Long customerId) {
        return mapper.selectList(new LambdaQueryWrapper<CrmCustomerSapInfo>()
                .eq(CrmCustomerSapInfo::getCustomerId, customerId)
                .eq(CrmCustomerSapInfo::getDeleted, 0)
                .orderByDesc(CrmCustomerSapInfo::getIsDefault)
                .orderByDesc(CrmCustomerSapInfo::getCreatedTime));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CrmCustomerSapInfo> pageByCustomerId(Long customerId, long current, long size) {
        Page<CrmCustomerSapInfo> page = new Page<>(current, size);
        Page<CrmCustomerSapInfo> result = mapper.selectPage(page, new LambdaQueryWrapper<CrmCustomerSapInfo>()
                .eq(CrmCustomerSapInfo::getCustomerId, customerId)
                .eq(CrmCustomerSapInfo::getDeleted, 0)
                .orderByDesc(CrmCustomerSapInfo::getIsDefault)
                .orderByDesc(CrmCustomerSapInfo::getCreatedTime));
        return new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords());
    }

    @Override
    @Transactional
    public CrmCustomerSapInfo create(Long customerId, CrmCustomerSapInfo info) {
        normalize(customerId, info);
        if (Integer.valueOf(1).equals(info.getIsDefault())) clearDefault(customerId, null);
        mapper.insert(info);
        CrmCustomerSapInfo saved = mapper.selectById(info.getId());
        syncCustomerSapCode(saved);
        queueSapCusPush(saved, "CREATE");
        return saved;
    }

    @Override
    @Transactional
    public CrmCustomerSapInfo update(Long id, CrmCustomerSapInfo info) {
        CrmCustomerSapInfo existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "SAP信息不存在");
        normalize(existing.getCustomerId(), info);
        if (Integer.valueOf(1).equals(info.getIsDefault())) clearDefault(existing.getCustomerId(), id);
        info.setId(id);
        info.setCustomerId(existing.getCustomerId());
        info.setVersion(existing.getVersion());
        mapper.updateById(info);
        CrmCustomerSapInfo saved = mapper.selectById(id);
        syncCustomerSapCode(saved);
        queueSapCusPush(saved, "UPDATE");
        return saved;
    }

    @Override
    @Transactional
    public CrmCustomerSapInfo applySapResponse(Long customerId, Long id, CrmCustomerSapInfo info) {
        CrmCustomerSapInfo existing = mapper.selectById(id);
        if (existing == null) throw new BizException(1001, "SAP信息不存在");
        if (!existing.getCustomerId().equals(customerId)) {
            throw new BizException(1001, "SAP信息不属于当前客户");
        }
        if (info == null) return existing;
        boolean changed = false;
        if (StringUtils.hasText(info.getSapCode())) {
            existing.setSapCode(safeNullable(info.getSapCode()));
            changed = true;
        }
        if (StringUtils.hasText(info.getAccountGroup())) {
            existing.setAccountGroup(safeNullable(info.getAccountGroup()));
            changed = true;
        }
        if (StringUtils.hasText(info.getCountryCode())) {
            existing.setCountryCode(safeNullable(info.getCountryCode()));
            changed = true;
        }
        if (StringUtils.hasText(info.getCompanyCode())) {
            existing.setCompanyCode(safeNullable(info.getCompanyCode()));
            changed = true;
        }
        if (StringUtils.hasText(info.getSalesOrg())) {
            existing.setSalesOrg(safeNullable(info.getSalesOrg()));
            changed = true;
        }
        if (StringUtils.hasText(info.getDistributionChannel())) {
            existing.setDistributionChannel(safeNullable(info.getDistributionChannel()));
            changed = true;
        }
        if (StringUtils.hasText(info.getDivision())) {
            existing.setDivision(safeNullable(info.getDivision()));
            changed = true;
        }
        if (StringUtils.hasText(info.getDescription())) {
            existing.setDescription(safeNullable(info.getDescription()));
            changed = true;
        }
        if (changed) {
            mapper.updateById(existing);
        }
        CrmCustomerSapInfo saved = mapper.selectById(id);
        syncCustomerSapCode(saved);
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    private void normalize(Long customerId, CrmCustomerSapInfo info) {
        info.setCustomerId(customerId);
        if (info.getIsDefault() == null) info.setIsDefault(0);
        info.setSapCode(safeNullable(info.getSapCode()));
        info.setAccountGroup(safeNullable(info.getAccountGroup()));
        info.setCountryCode(safeNullable(info.getCountryCode()));
        info.setCompanyCode(safeNullable(info.getCompanyCode()));
        info.setSalesOrg(safeNullable(info.getSalesOrg()));
        info.setDistributionChannel(safeNullable(info.getDistributionChannel()));
        info.setDivision(safeNullable(info.getDivision()));
        info.setDescription(safeNullable(info.getDescription()));
    }

    private String safeNullable(String value) {
        String safe = InputSanitizer.safeText(value);
        return StringUtils.hasText(safe) ? safe.trim() : null;
    }

    private void clearDefault(Long customerId, Long exceptId) {
        LambdaUpdateWrapper<CrmCustomerSapInfo> uw = new LambdaUpdateWrapper<CrmCustomerSapInfo>()
                .eq(CrmCustomerSapInfo::getCustomerId, customerId)
                .eq(CrmCustomerSapInfo::getIsDefault, 1)
                .set(CrmCustomerSapInfo::getIsDefault, 0);
        if (exceptId != null) uw.ne(CrmCustomerSapInfo::getId, exceptId);
        mapper.update(null, uw);
    }

    private void queueSapCusPush(CrmCustomerSapInfo info, String action) {
        if (info == null || info.getCustomerId() == null || info.getId() == null) return;
        IntegrationInterface iface = interfaceMapper.selectOne(new LambdaQueryWrapper<IntegrationInterface>()
                .eq(IntegrationInterface::getInterfaceCode, SAP_CUS_INTERFACE_CODE)
                .eq(IntegrationInterface::getDeleted, (short) 0)
                .last("LIMIT 1"));
        boolean ready = iface != null && Short.valueOf((short) 1).equals(iface.getEnabled());

        IntegrationLog log = new IntegrationLog();
        log.setInterfaceCode(SAP_CUS_INTERFACE_CODE);
        log.setInterfaceName(iface != null && StringUtils.hasText(iface.getInterfaceName())
                ? iface.getInterfaceName()
                : "SAP客户主数据维护");
        log.setDirection("OUTBOUND");
        log.setBusinessKey("CUSTOMER_SAP_INFO:" + info.getCustomerId() + ":" + info.getId());
        log.setStatus(ready ? "PENDING" : "FAILED");
        log.setRequestPayload(toJson(buildSapCusPayload(info, action, iface)));
        log.setErrorMessage(ready ? null : "接口定义SAP_CUS不存在或未启用");
        log.setRetryCount(0);
        log.setCreatedBy(currentUserId());
        log.setCreatedTime(LocalDateTime.now());
        log.setUpdatedTime(LocalDateTime.now());
        log.setDeleted((short) 0);
        logMapper.insert(log);
    }

    private Map<String, Object> buildSapCusPayload(CrmCustomerSapInfo info, String action, IntegrationInterface iface) {
        Long customerId = info.getCustomerId();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", action);
        payload.put("interfaceCode", SAP_CUS_INTERFACE_CODE);
        payload.put("protocol", iface == null ? "SAP_RFC" : iface.getProtocol());
        payload.put("sapFunctionName", iface == null ? null : iface.getSapFunctionName());
        payload.put("customerId", customerId);
        payload.put("sapInfoId", info.getId());
        payload.put("sapResponsePath", "/crm/v1/customers/" + customerId + "/sap-infos/" + info.getId() + "/sap-response");
        payload.put("customer", customerMapper.selectById(customerId));
        payload.put("sapInfo", info);
        payload.put("sapInfos", getByCustomerId(customerId));
        payload.put("sapOrgs", sapOrgMapper.selectList(new LambdaQueryWrapper<CrmCustomerSapOrg>()
                .eq(CrmCustomerSapOrg::getCustomerId, customerId)
                .eq(CrmCustomerSapOrg::getDeleted, 0)
                .orderByDesc(CrmCustomerSapOrg::getCreatedTime)));
        return payload;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(1001, "SAP_CUS推送报文生成失败");
        }
    }

    private void syncCustomerSapCode(CrmCustomerSapInfo info) {
        if (info == null || !Integer.valueOf(1).equals(info.getIsDefault()) || !StringUtils.hasText(info.getSapCode())) {
            return;
        }
        Customer customer = customerMapper.selectById(info.getCustomerId());
        if (customer == null) return;
        customer.setSapCustomerCode(info.getSapCode());
        customerMapper.updateById(customer);
    }

    private Long currentUserId() {
        return userContext.getCurrentUserId().orElse(null);
    }
}
