package com.huafu.crm.customer.service.impl;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.exception.BizException;
import com.huafu.crm.common.util.InputSanitizer;
import com.huafu.crm.customer.dto.CustomerCreateDTO;
import com.huafu.crm.customer.dto.CustomerUpdateDTO;
import com.huafu.crm.customer.entity.Customer;
import com.huafu.crm.customer.entity.CustomerTransfer;
import com.huafu.crm.customer.mapper.CustomerMapper;
import com.huafu.crm.customer.mapper.CustomerTransferMapper;
import com.huafu.crm.customer.query.CustomerQuery;
import com.huafu.crm.customer.service.CustomerService;
import com.huafu.crm.customer.vo.CustomerVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    // 保存后必须回查的关键字段，丢字段时记录 warn
    private static final Set<String> CRITICAL_FIELDS = Set.of(
        "customerName", "customerShortName", "type", "level", "status",
        "province", "city", "district", "address",
        "mainContactName", "mainContactPhone", "mainContactRole",
        "annualRevenue", "creditLimit", "taxRate", "paymentDays",
        "sapCustomerCode", "ownerUserId", "ownerDeptId",
        "customerCategory", "customerSegment", "businessType",
        "countryRegion", "mainBrand", "annualYarnVolume",
        "machineCount", "productionCapacity", "industryPosition",
        "mainCustomerGroup", "bundleCustomerName", "bundleBrand",
        "bundleCustomerId", "bundleCustomerSapCode",
        "salesMerchandiser", "locationLat", "locationLng",
        "unifiedSocialCreditCode", "englishName", "assetType",
        "customerSource", "customerStage", "riskLevel",
        "taxId", "bankName", "bankAccount", "invoiceTitle"
    );

    private String safeInput(String val) {
        return InputSanitizer.safeText(val);
    }

    private final CustomerMapper mapper;
    private final CustomerTransferMapper transferMapper;

    public CustomerServiceImpl(CustomerMapper mapper, CustomerTransferMapper transferMapper) {
        this.mapper = mapper;
        this.transferMapper = transferMapper;
    }

    @Override
    @Transactional
    public CustomerVO create(CustomerCreateDTO dto) {
        String safeCustomerName = safeInput(dto.customerName());
        String safeShortName = safeInput(dto.customerShortName());
        if (safeCustomerName == null || safeCustomerName.isBlank()) {
            throw new BizException(1001, "客户名称不能为空");
        }
        if (safeShortName == null || safeShortName.isBlank()) {
            throw new BizException(1001, "客户简称不能为空");
        }
        Long sameNameCount = mapper.selectCount(new LambdaQueryWrapper<Customer>()
            .eq(Customer::getCustomerName, safeCustomerName.trim())
            .eq(Customer::getDeleted, 0));
        if (sameNameCount > 0) {
            throw new BizException(1002, "客户名称已存在，请勿重复创建：" + safeCustomerName.trim());
        }

        String customerCode = dto.customerCode();
        if (customerCode == null || customerCode.isBlank()) {
            customerCode = "C-" + String.valueOf(System.currentTimeMillis()).substring(5)
                         + String.format("%02d", new java.util.Random().nextInt(100));
        }

        // BUG-2: 客户编码唯一性校验
        Long existing = mapper.selectCount(new LambdaQueryWrapper<Customer>()
            .eq(Customer::getCustomerCode, customerCode)
            .eq(Customer::getDeleted, 0));
        if (existing > 0) {
            throw new BizException(1002, "客户编码已存在");
        }

        Customer c = new Customer();
        // XSS过滤：对所有文本输入字段做HTML转义
        c.setCustomerCode(safeInput(customerCode));
        c.setCustomerName(safeCustomerName.trim());
        c.setCustomerShortName(safeShortName.trim());
        c.setType(dto.type());
        c.setLevel(dto.level());
        c.setStatus(dto.status() != null ? dto.status() : 1);
        c.setProvince(safeInput(dto.province()));
        c.setCity(safeInput(dto.city()));
        c.setDistrict(safeInput(dto.district()));
        c.setAddress(safeInput(dto.address()));
        c.setMainContactName(safeInput(dto.mainContactName()));
        c.setMainContactPhone(safeInput(dto.mainContactPhone()));
        c.setMainContactRole(safeInput(dto.mainContactRole()));
        c.setAnnualRevenue(dto.annualRevenue());
        c.setCreditLimit(dto.creditLimit());
        c.setTaxRate(dto.taxRate());
        c.setPaymentDays(dto.paymentDays());
        c.setSapCustomerCode(safeInput(dto.sapCustomerCode()));
        c.setRemark(safeInput(dto.remark()));
        c.setOwnerUserId(dto.ownerUserId() != null ? dto.ownerUserId() : 1L);
        // P0 扩展字段
        c.setCustomerCategory(safeInput(dto.customerCategory()));
        c.setCustomerSegment(safeInput(dto.customerSegment()));
        c.setBusinessType(dto.businessType());
        c.setCountryRegion(safeInput(dto.countryRegion()));
        c.setMainBrand(safeInput(dto.mainBrand()));
        c.setAnnualYarnVolume(dto.annualYarnVolume());
        c.setMachineCount(dto.machineCount());
        c.setProductionCapacity(safeInput(dto.productionCapacity()));
        c.setIndustryPosition(safeInput(dto.industryPosition()));
        c.setMainCustomerGroup(safeInput(dto.mainCustomerGroup()));
        c.setBundleCustomerName(safeInput(dto.bundleCustomerName()));
        c.setBundleBrand(safeInput(dto.bundleBrand()));
        c.setBundleCustomerId(dto.bundleCustomerId());
        c.setBundleCustomerSapCode(safeInput(dto.bundleCustomerSapCode()));
        c.setOwnerDeptId(dto.ownerDeptId());
        c.setSalesMerchandiser(safeInput(dto.salesMerchandiser()));
        c.setLocationLat(dto.locationLat());
        c.setLocationLng(dto.locationLng());
        c.setUnifiedSocialCreditCode(safeInput(dto.unifiedSocialCreditCode()));
        c.setEnglishName(safeInput(dto.englishName()));
        c.setAssetType(safeInput(dto.assetType()));
        c.setCustomerSource(safeInput(dto.customerSource()));
        c.setCustomerStage(dto.customerStage());
        c.setCompetitorShareJson(dto.competitorShareJson());
        c.setCooperationBrandJson(dto.cooperationBrandJson());
        c.setRiskLevel(dto.riskLevel());
        c.setTaxId(safeInput(dto.taxId()));
        c.setBankName(safeInput(dto.bankName()));
        c.setBankAccount(safeInput(dto.bankAccount()));
        c.setInvoiceTitle(safeInput(dto.invoiceTitle()));
        c.setCompanyCode(safeInput(dto.companyCode()));
        c.setPriceList(safeInput(dto.priceList()));
        c.setCurrency(safeInput(dto.currency()));
        c.setDeliveryFactory(safeInput(dto.deliveryFactory()));
        c.setAccountAssignmentGroup(safeInput(dto.accountAssignmentGroup()));
        c.setTaxClassification(safeInput(dto.taxClassification()));
        c.setShipToParty(safeInput(dto.shipToParty()));
        c.setSoldToParty(safeInput(dto.soldToParty()));
        c.setPayerParty(safeInput(dto.payerParty()));
        c.setRegion(safeInput(dto.region()));
        mapper.insert(c);
        Customer refreshed = mapper.selectById(c.getId());
        validateAfterSave(dto, refreshed, "create");
        return toVO(refreshed);
    }

    @Override
    @Transactional
    public CustomerVO update(Long id, CustomerUpdateDTO dto) {
        Customer c = mapper.selectById(id);
        if (c == null) throw new BizException(1001, "客户不存在");
        if (dto.customerName() != null) c.setCustomerName(safeInput(dto.customerName()));
        if (dto.customerShortName() != null) c.setCustomerShortName(safeInput(dto.customerShortName()));
        if (dto.type() != null) c.setType(dto.type());
        if (dto.level() != null) c.setLevel(dto.level());
        if (dto.status() != null) c.setStatus(dto.status());
        if (dto.province() != null) c.setProvince(safeInput(dto.province()));
        if (dto.city() != null) c.setCity(safeInput(dto.city()));
        if (dto.district() != null) c.setDistrict(safeInput(dto.district()));
        if (dto.address() != null) c.setAddress(safeInput(dto.address()));
        if (dto.mainContactName() != null) c.setMainContactName(safeInput(dto.mainContactName()));
        if (dto.mainContactPhone() != null) c.setMainContactPhone(safeInput(dto.mainContactPhone()));
        if (dto.mainContactRole() != null) c.setMainContactRole(safeInput(dto.mainContactRole()));
        if (dto.annualRevenue() != null) c.setAnnualRevenue(dto.annualRevenue());
        if (dto.creditLimit() != null) c.setCreditLimit(dto.creditLimit());
        if (dto.taxRate() != null) c.setTaxRate(dto.taxRate());
        if (dto.paymentDays() != null) c.setPaymentDays(dto.paymentDays());
        if (dto.sapCustomerCode() != null) c.setSapCustomerCode(safeInput(dto.sapCustomerCode()));
        if (dto.remark() != null) c.setRemark(safeInput(dto.remark()));
        if (dto.ownerUserId() != null) c.setOwnerUserId(dto.ownerUserId());
        // P0 扩展字段
        if (dto.customerCategory() != null) c.setCustomerCategory(safeInput(dto.customerCategory()));
        if (dto.customerSegment() != null) c.setCustomerSegment(safeInput(dto.customerSegment()));
        if (dto.businessType() != null) c.setBusinessType(dto.businessType());
        if (dto.countryRegion() != null) c.setCountryRegion(safeInput(dto.countryRegion()));
        if (dto.mainBrand() != null) c.setMainBrand(safeInput(dto.mainBrand()));
        if (dto.annualYarnVolume() != null) c.setAnnualYarnVolume(dto.annualYarnVolume());
        if (dto.machineCount() != null) c.setMachineCount(dto.machineCount());
        if (dto.productionCapacity() != null) c.setProductionCapacity(safeInput(dto.productionCapacity()));
        if (dto.industryPosition() != null) c.setIndustryPosition(safeInput(dto.industryPosition()));
        if (dto.mainCustomerGroup() != null) c.setMainCustomerGroup(safeInput(dto.mainCustomerGroup()));
        if (dto.bundleCustomerName() != null) {
            c.setBundleCustomerName(safeInput(dto.bundleCustomerName()));
            if (dto.bundleCustomerName().isBlank()) c.setBundleCustomerId(null);
        }
        if (dto.bundleBrand() != null) c.setBundleBrand(safeInput(dto.bundleBrand()));
        if (dto.bundleCustomerId() != null) c.setBundleCustomerId(dto.bundleCustomerId());
        if (dto.bundleCustomerSapCode() != null) c.setBundleCustomerSapCode(safeInput(dto.bundleCustomerSapCode()));
        if (dto.ownerDeptId() != null) c.setOwnerDeptId(dto.ownerDeptId());
        if (dto.salesMerchandiser() != null) c.setSalesMerchandiser(safeInput(dto.salesMerchandiser()));
        if (dto.locationLat() != null) c.setLocationLat(dto.locationLat());
        if (dto.locationLng() != null) c.setLocationLng(dto.locationLng());
        if (dto.unifiedSocialCreditCode() != null) c.setUnifiedSocialCreditCode(safeInput(dto.unifiedSocialCreditCode()));
        if (dto.englishName() != null) c.setEnglishName(safeInput(dto.englishName()));
        if (dto.assetType() != null) c.setAssetType(safeInput(dto.assetType()));
        if (dto.customerSource() != null) c.setCustomerSource(safeInput(dto.customerSource()));
        if (dto.customerStage() != null) c.setCustomerStage(dto.customerStage());
        if (dto.competitorShareJson() != null) c.setCompetitorShareJson(dto.competitorShareJson());
        if (dto.cooperationBrandJson() != null) c.setCooperationBrandJson(dto.cooperationBrandJson());
        if (dto.riskLevel() != null) c.setRiskLevel(dto.riskLevel());
        if (dto.taxId() != null) c.setTaxId(safeInput(dto.taxId()));
        if (dto.bankName() != null) c.setBankName(safeInput(dto.bankName()));
        if (dto.bankAccount() != null) c.setBankAccount(safeInput(dto.bankAccount()));
        if (dto.invoiceTitle() != null) c.setInvoiceTitle(safeInput(dto.invoiceTitle()));
        if (dto.companyCode() != null) c.setCompanyCode(safeInput(dto.companyCode()));
        if (dto.priceList() != null) c.setPriceList(safeInput(dto.priceList()));
        if (dto.currency() != null) c.setCurrency(safeInput(dto.currency()));
        if (dto.deliveryFactory() != null) c.setDeliveryFactory(safeInput(dto.deliveryFactory()));
        if (dto.accountAssignmentGroup() != null) c.setAccountAssignmentGroup(safeInput(dto.accountAssignmentGroup()));
        if (dto.taxClassification() != null) c.setTaxClassification(safeInput(dto.taxClassification()));
        if (dto.shipToParty() != null) c.setShipToParty(safeInput(dto.shipToParty()));
        if (dto.soldToParty() != null) c.setSoldToParty(safeInput(dto.soldToParty()));
        if (dto.payerParty() != null) c.setPayerParty(safeInput(dto.payerParty()));
        if (dto.region() != null) c.setRegion(safeInput(dto.region()));
        if (c.getVersion() == null) c.setVersion(0);
        mapper.updateById(c);
        Customer refreshed = mapper.selectById(id);
        validateAfterSave(dto, refreshed, "update");
        return toVO(refreshed);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerVO get(Long id) {
        Customer c = mapper.selectById(id);
        if (c == null) throw new BizException(1001, "客户不存在");
        return toVO(c);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CustomerVO> page(CustomerQuery query) {
        Page<Customer> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<Customer> qw = new LambdaQueryWrapper<>();
        if (query.customerName() != null && !query.customerName().isBlank())
            qw.like(Customer::getCustomerName, query.customerName());
        if (query.customerCode() != null && !query.customerCode().isBlank())
            qw.like(Customer::getCustomerCode, query.customerCode());
        if (query.type() != null) qw.eq(Customer::getType, query.type());
        if (query.level() != null) qw.eq(Customer::getLevel, query.level());
        if (query.status() != null) qw.eq(Customer::getStatus, query.status());
        if (query.ownerUserId() != null) qw.eq(Customer::getOwnerUserId, query.ownerUserId());
        if (query.businessType() != null) qw.eq(Customer::getBusinessType, query.businessType());
        if (query.customerCategory() != null && !query.customerCategory().isBlank())
            qw.eq(Customer::getCustomerCategory, query.customerCategory());
        if (query.customerSource() != null && !query.customerSource().isBlank())
            qw.eq(Customer::getCustomerSource, query.customerSource());
        if (query.mainCustomerGroup() != null && !query.mainCustomerGroup().isBlank())
            qw.eq(Customer::getMainCustomerGroup, query.mainCustomerGroup());
        if (query.customerStage() != null) qw.eq(Customer::getCustomerStage, query.customerStage());
        if (query.riskLevel() != null) qw.eq(Customer::getRiskLevel, query.riskLevel());
        if (query.salesMerchandiser() != null && !query.salesMerchandiser().isBlank())
            qw.like(Customer::getSalesMerchandiser, query.salesMerchandiser());
        // 只查非公海（ownerUserId 不为空）且未删除
        qw.isNotNull(Customer::getOwnerUserId);
        qw.eq(Customer::getDeleted, 0);
        qw.orderByDesc(Customer::getCreatedTime);
        Page<Customer> result = mapper.selectPage(page, qw);
 return new PageResult<>(
 result.getCurrent(), result.getSize(), result.getTotal(),
 result.getRecords().stream().map(this::toVO).toList());
 }

 @Override
 @Transactional
 public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CustomerVO> pagePublicPool(CustomerQuery query) {
        Page<Customer> page = new Page<>(query.current(), query.size());
        LambdaQueryWrapper<Customer> qw = new LambdaQueryWrapper<>();
        // 公海：ownerUserId 为空
        qw.isNull(Customer::getOwnerUserId);
        qw.eq(Customer::getDeleted, 0);
        if (query.customerName() != null && !query.customerName().isBlank())
            qw.like(Customer::getCustomerName, query.customerName());
        if (query.type() != null) qw.eq(Customer::getType, query.type());
        if (query.level() != null) qw.eq(Customer::getLevel, query.level());
        if (query.status() != null) qw.eq(Customer::getStatus, query.status());
        if (query.businessType() != null) qw.eq(Customer::getBusinessType, query.businessType());
        if (query.customerCategory() != null && !query.customerCategory().isBlank())
            qw.eq(Customer::getCustomerCategory, query.customerCategory());
        if (query.customerSource() != null && !query.customerSource().isBlank())
            qw.eq(Customer::getCustomerSource, query.customerSource());
        if (query.mainCustomerGroup() != null && !query.mainCustomerGroup().isBlank())
            qw.eq(Customer::getMainCustomerGroup, query.mainCustomerGroup());
        if (query.customerStage() != null) qw.eq(Customer::getCustomerStage, query.customerStage());
        if (query.riskLevel() != null) qw.eq(Customer::getRiskLevel, query.riskLevel());
        if (query.salesMerchandiser() != null && !query.salesMerchandiser().isBlank())
            qw.like(Customer::getSalesMerchandiser, query.salesMerchandiser());
        qw.orderByAsc(Customer::getPublicPoolTime);
        Page<Customer> result = mapper.selectPage(page, qw);
 return new PageResult<>(
 result.getCurrent(), result.getSize(), result.getTotal(),
 result.getRecords().stream().map(this::toVO).toList());
    }

    @Override
    @Transactional
    public void claimFromPool(Long id) {
        Customer c = mapper.selectById(id);
        if (c == null) throw new BizException(1001, "客户不存在");
        if (c.getOwnerUserId() != null) throw new BizException(1003, "客户已被认领");
        // TODO: 从 UserContext 获取当前用户ID
        c.setOwnerUserId(1L); // 临时硬编码，后续从上下文取
        c.setPublicPoolTime(null);
        mapper.updateById(c);
    }

    @Override
    @Transactional
    public void transfer(Long id, Long toUserId, String reason) {
        Customer c = mapper.selectById(id);
        if (c == null) throw new BizException(1001, "客户不存在");
        Long fromUserId = c.getOwnerUserId();
        // 记录转移历史
        CustomerTransfer t = new CustomerTransfer();
        t.setCustomerId(id);
        t.setFromUserId(fromUserId);
        t.setToUserId(toUserId);
        t.setReason(reason);
        if (t.getTenantId() == null) t.setTenantId(1L);
        transferMapper.insert(t);
        // 更新客户owner
        c.setOwnerUserId(toUserId);
        mapper.updateById(c);
    }

    @Override
    @Transactional
    public void freeze(Long id, String frozenReason) {
        Customer c = mapper.selectById(id);
        if (c == null) throw new BizException(1001, "客户不存在");
        c.setStatus(2); // 冻结
        if (c.getRemark() != null) {
            c.setRemark(c.getRemark() + " | 冻结原因：" + frozenReason);
        } else {
            c.setRemark("冻结原因：" + frozenReason);
        }
        mapper.updateById(c);
    }

    @Override
    @Transactional
    public void loss(Long id, String lossReason) {
        Customer c = mapper.selectById(id);
        if (c == null) throw new BizException(1001, "客户不存在");
        c.setStatus(3); // 流失
        if (c.getRemark() != null) {
            c.setRemark(c.getRemark() + " | 流失原因：" + lossReason);
        } else {
            c.setRemark("流失原因：" + lossReason);
        }
        mapper.updateById(c);
    }

    /**
     * 保存后回查关键字段，检测丢字段情况并记录 warn 日志。
     * 当检测到字段丢失时记录日志但不抛异常，由前端负责阻断发布。
     */
    private void validateAfterSave(CustomerCreateDTO dto, Customer saved, String operation) {
        if (saved == null) {
            log.warn("[CustomerSaveValidation] {} 后回查结果为 null，customerId 可能丢失", operation);
            return;
        }
        // 检测关键字段是否在入库后丢失
        if (dto.customerName() != null && saved.getCustomerName() == null) {
            log.warn("[CustomerSaveValidation] {} 后 customerName 丢失，提交值={}, 实际值={}",
                operation, dto.customerName(), saved.getCustomerName());
        }
        if (dto.customerShortName() != null && saved.getCustomerShortName() == null) {
            log.warn("[CustomerSaveValidation] {} 后 customerShortName 丢失，提交值={}, 实际值={}",
                operation, dto.customerShortName(), saved.getCustomerShortName());
        }
        if (dto.sapCustomerCode() != null && saved.getSapCustomerCode() == null) {
            log.warn("[CustomerSaveValidation] {} 后 sapCustomerCode 丢失，提交值={}, 实际值={}",
                operation, dto.sapCustomerCode(), saved.getSapCustomerCode());
        }
        if (dto.mainContactName() != null && saved.getMainContactName() == null) {
            log.warn("[CustomerSaveValidation] {} 后 mainContactName 丢失，提交值={}, 实际值={}",
                operation, dto.mainContactName(), saved.getMainContactName());
        }
        if (dto.mainContactPhone() != null && saved.getMainContactPhone() == null) {
            log.warn("[CustomerSaveValidation] {} 后 mainContactPhone 丢失，提交值={}, 实际值={}",
                operation, dto.mainContactPhone(), saved.getMainContactPhone());
        }
        if (dto.province() != null && saved.getProvince() == null) {
            log.warn("[CustomerSaveValidation] {} 后 province 丢失，提交值={}, 实际值={}",
                operation, dto.province(), saved.getProvince());
        }
        if (dto.city() != null && saved.getCity() == null) {
            log.warn("[CustomerSaveValidation] {} 后 city 丢失，提交值={}, 实际值={}",
                operation, dto.city(), saved.getCity());
        }
        if (dto.district() != null && saved.getDistrict() == null) {
            log.warn("[CustomerSaveValidation] {} 后 district 丢失，提交值={}, 实际值={}",
                operation, dto.district(), saved.getDistrict());
        }
        if (dto.address() != null && saved.getAddress() == null) {
            log.warn("[CustomerSaveValidation] {} 后 address 丢失，提交值={}, 实际值={}",
                operation, dto.address(), saved.getAddress());
        }
        if (dto.unifiedSocialCreditCode() != null && saved.getUnifiedSocialCreditCode() == null) {
            log.warn("[CustomerSaveValidation] {} 后 unifiedSocialCreditCode 丢失，提交值={}, 实际值={}",
                operation, dto.unifiedSocialCreditCode(), saved.getUnifiedSocialCreditCode());
        }
    }

    /**
     * 保存后回查关键字段（update DTO 版本）。
     */
    private void validateAfterSave(CustomerUpdateDTO dto, Customer saved, String operation) {
        if (saved == null) {
            log.warn("[CustomerSaveValidation] {} 后回查结果为 null", operation);
            return;
        }
        if (dto.customerName() != null && saved.getCustomerName() == null) {
            log.warn("[CustomerSaveValidation] {} 后 customerName 丢失，提交值={}", operation, dto.customerName());
        }
        if (dto.customerShortName() != null && saved.getCustomerShortName() == null) {
            log.warn("[CustomerSaveValidation] {} 后 customerShortName 丢失，提交值={}", operation, dto.customerShortName());
        }
        if (dto.sapCustomerCode() != null && saved.getSapCustomerCode() == null) {
            log.warn("[CustomerSaveValidation] {} 后 sapCustomerCode 丢失，提交值={}", operation, dto.sapCustomerCode());
        }
        if (dto.mainContactName() != null && saved.getMainContactName() == null) {
            log.warn("[CustomerSaveValidation] {} 后 mainContactName 丢失，提交值={}", operation, dto.mainContactName());
        }
        if (dto.mainContactPhone() != null && saved.getMainContactPhone() == null) {
            log.warn("[CustomerSaveValidation] {} 后 mainContactPhone 丢失，提交值={}", operation, dto.mainContactPhone());
        }
        if (dto.province() != null && saved.getProvince() == null) {
            log.warn("[CustomerSaveValidation] {} 后 province 丢失，提交值={}", operation, dto.province());
        }
        if (dto.city() != null && saved.getCity() == null) {
            log.warn("[CustomerSaveValidation] {} 后 city 丢失，提交值={}", operation, dto.city());
        }
        if (dto.district() != null && saved.getDistrict() == null) {
            log.warn("[CustomerSaveValidation] {} 后 district 丢失，提交值={}", operation, dto.district());
        }
        if (dto.address() != null && saved.getAddress() == null) {
            log.warn("[CustomerSaveValidation] {} 后 address 丢失，提交值={}", operation, dto.address());
        }
        if (dto.unifiedSocialCreditCode() != null && saved.getUnifiedSocialCreditCode() == null) {
            log.warn("[CustomerSaveValidation] {} 后 unifiedSocialCreditCode 丢失，提交值={}", operation, dto.unifiedSocialCreditCode());
        }
    }

    private CustomerVO toVO(Customer c) {
        String levelCode = c.getLevel() != null ? String.valueOf(c.getLevel()) : null;
        String statusCode = c.getStatus() != null ? String.valueOf(c.getStatus()) : null;
        String typeCode = c.getType() != null ? String.valueOf(c.getType()) : null;
        String bizTypeCode = c.getBusinessType() != null ? String.valueOf(c.getBusinessType()) : null;
        return new CustomerVO(c.getId(), c.getCustomerCode(), c.getCustomerName(),
            c.getCustomerShortName(), typeCode, levelCode, statusCode,
            c.getProvince(), c.getCity(), c.getDistrict(), c.getAddress(),
            c.getMainContactName(), c.getMainContactPhone(), c.getMainContactRole(),
            c.getAnnualRevenue(), c.getCreditLimit(), c.getTaxRate(), c.getPaymentDays(),
            c.getSapCustomerCode(), c.getRemark(), c.getLastVisitTime(),
            c.getOwnerUserId(), c.getPublicPoolTime(),
            c.getCreatedBy(), c.getCreatedTime(), c.getTenantId(), c.getVersion(),
            // P0 扩展字段
            c.getCustomerCategory(), c.getCustomerSegment(), bizTypeCode,
            c.getCountryRegion(), c.getMainBrand(), c.getAnnualYarnVolume(),
            c.getMachineCount(), c.getProductionCapacity(), c.getIndustryPosition(),
            c.getMainCustomerGroup(), c.getBundleCustomerName(), c.getBundleBrand(),
            c.getBundleCustomerId(), c.getBundleCustomerSapCode(),
            c.getOwnerDeptId(), c.getSalesMerchandiser(),
            c.getLocationLat(), c.getLocationLng(),
            c.getUnifiedSocialCreditCode(), c.getEnglishName(), c.getAssetType(),
            c.getCustomerSource(), c.getCustomerStage(),
            c.getCompetitorShareJson(), c.getCooperationBrandJson(),
            c.getRiskLevel(),
            c.getTaxId(), c.getBankName(), c.getBankAccount(), c.getInvoiceTitle(),
            c.getCompanyCode(), c.getPriceList(), c.getCurrency(),
            c.getDeliveryFactory(), c.getAccountAssignmentGroup(), c.getTaxClassification(),
            c.getShipToParty(), c.getSoldToParty(), c.getPayerParty(),
            c.getRegion());
    }
}
