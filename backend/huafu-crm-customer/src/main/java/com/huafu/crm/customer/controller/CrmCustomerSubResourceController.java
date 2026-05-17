package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.common.entity.*;
import com.huafu.crm.customer.entity.CustomerAddress;
import com.huafu.crm.customer.entity.CustomerContact;
import com.huafu.crm.customer.entity.CustomerTransfer;
import com.huafu.crm.customer.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/v1/customers")
@Tag(name = "客户扩展信息管理", description = "客户扩展信息、子资源管理（附件、联系人、纱线用量等）")
public class CrmCustomerSubResourceController {

    private final CrmCustomerExtService extService;
    private final CrmCustomerOverviewService overviewService;
    private final CrmCustomerYarnUsageService yarnUsageService;
    private final CrmCustomerSapInfoService sapInfoService;
    private final CrmCustomerSapOrgService sapOrgService;
    private final CrmCustomerSapPartnerService sapPartnerService;
    private final CrmCustomerContactService contactService;
    private final CrmCustomerBundleService bundleService;
    private final CrmCustomerAttachmentService attachmentService;
    private final CrmCustomerAddressService addressService;
    private final CrmCustomerTransferService transferService;

    public CrmCustomerSubResourceController(
            CrmCustomerExtService extService,
            CrmCustomerOverviewService overviewService,
            CrmCustomerYarnUsageService yarnUsageService,
            CrmCustomerSapInfoService sapInfoService,
            CrmCustomerSapOrgService sapOrgService,
            CrmCustomerSapPartnerService sapPartnerService,
            CrmCustomerContactService contactService,
            CrmCustomerBundleService bundleService,
            CrmCustomerAttachmentService attachmentService,
            CrmCustomerAddressService addressService,
            CrmCustomerTransferService transferService) {
        this.extService = extService;
        this.overviewService = overviewService;
        this.yarnUsageService = yarnUsageService;
        this.sapInfoService = sapInfoService;
        this.sapOrgService = sapOrgService;
        this.sapPartnerService = sapPartnerService;
        this.contactService = contactService;
        this.bundleService = bundleService;
        this.attachmentService = attachmentService;
        this.addressService = addressService;
        this.transferService = transferService;
    }

    // ==================== 扩展信息 ====================
    @GetMapping("/{id}/ext")
    @Operation(summary = "获取客户扩展信息")
    public Result<CrmCustomerExt> getExt(@PathVariable Long id) {
        return Result.ok(extService.getByCustomerId(id));
    }

    @PutMapping("/{id}/ext")
    @Operation(summary = "更新客户扩展信息")
    public Result<CrmCustomerExt> updateExt(@PathVariable Long id, @RequestBody CrmCustomerExt ext) {
        return Result.ok(extService.saveOrUpdate(id, ext));
    }

    // ==================== 整体概述 ====================
    @GetMapping("/{id}/overview")
    @Operation(summary = "获取客户整体概述")
    public Result<CrmCustomerOverview> getOverview(@PathVariable Long id) {
        return Result.ok(overviewService.getByCustomerId(id));
    }

    @PutMapping("/{id}/overview")
    @Operation(summary = "更新客户整体概述")
    public Result<CrmCustomerOverview> updateOverview(@PathVariable Long id, @RequestBody CrmCustomerOverview overview) {
        return Result.ok(overviewService.saveOrUpdate(id, overview));
    }

    // ==================== 纱线用量 ====================
    @GetMapping("/{id}/yarn-usage/page")
    @Operation(summary = "分页获取纱线用量列表")
    public Result<com.huafu.crm.common.api.PageResult<CrmCustomerYarnUsage>> pageYarnUsage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(yarnUsageService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/yarn-usage")
    @Operation(summary = "获取纱线用量列表")
    public Result<List<CrmCustomerYarnUsage>> getYarnUsage(@PathVariable Long id) {
        return Result.ok(yarnUsageService.getByCustomerId(id));
    }

    @PostMapping("/{id}/yarn-usage")
    @Operation(summary = "新增纱线用量")
    public Result<CrmCustomerYarnUsage> createYarnUsage(@PathVariable Long id, @RequestBody CrmCustomerYarnUsage yarnUsage) {
        return Result.ok(yarnUsageService.create(id, yarnUsage));
    }

    @PutMapping("/{id}/yarn-usage/{yarnUsageId}")
    @Operation(summary = "更新纱线用量")
    public Result<CrmCustomerYarnUsage> updateYarnUsage(@PathVariable Long id, @PathVariable Long yarnUsageId, @RequestBody CrmCustomerYarnUsage yarnUsage) {
        return Result.ok(yarnUsageService.update(yarnUsageId, yarnUsage));
    }

    @DeleteMapping("/{id}/yarn-usage/{yarnUsageId}")
    @Operation(summary = "删除纱线用量")
    public Result<Void> deleteYarnUsage(@PathVariable Long id, @PathVariable Long yarnUsageId) {
        yarnUsageService.delete(yarnUsageId);
        return Result.ok();
    }

    // ==================== SAP主数据 ====================
    @GetMapping("/{id}/sap-infos/page")
    @Operation(summary = "分页获取客户SAP主数据")
    public Result<com.huafu.crm.common.api.PageResult<CrmCustomerSapInfo>> pageSapInfos(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(sapInfoService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/sap-infos")
    @Operation(summary = "获取客户SAP主数据")
    public Result<List<CrmCustomerSapInfo>> getSapInfos(@PathVariable Long id) {
        return Result.ok(sapInfoService.getByCustomerId(id));
    }

    @PostMapping("/{id}/sap-infos")
    @Operation(summary = "新增客户SAP主数据")
    public Result<CrmCustomerSapInfo> createSapInfo(@PathVariable Long id, @RequestBody CrmCustomerSapInfo sapInfo) {
        return Result.ok(sapInfoService.create(id, sapInfo));
    }

    @PutMapping("/{id}/sap-infos/{sapInfoId}")
    @Operation(summary = "更新客户SAP主数据")
    public Result<CrmCustomerSapInfo> updateSapInfo(@PathVariable Long id, @PathVariable Long sapInfoId, @RequestBody CrmCustomerSapInfo sapInfo) {
        return Result.ok(sapInfoService.update(sapInfoId, sapInfo));
    }

    @DeleteMapping("/{id}/sap-infos/{sapInfoId}")
    @Operation(summary = "删除客户SAP主数据")
    public Result<Void> deleteSapInfo(@PathVariable Long id, @PathVariable Long sapInfoId) {
        sapInfoService.delete(sapInfoId);
        return Result.ok();
    }

    // ==================== SAP组织信息 ====================
    @GetMapping("/{id}/sap-orgs/page")
    @Operation(summary = "分页获取SAP组织信息列表")
    public Result<com.huafu.crm.common.api.PageResult<CrmCustomerSapOrg>> pageSapOrgs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(sapOrgService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/sap-orgs")
    @Operation(summary = "获取SAP组织信息列表")
    public Result<List<CrmCustomerSapOrg>> getSapOrgs(@PathVariable Long id) {
        return Result.ok(sapOrgService.getByCustomerId(id));
    }

    @PostMapping("/{id}/sap-orgs")
    @Operation(summary = "新增SAP组织信息")
    public Result<CrmCustomerSapOrg> createSapOrg(@PathVariable Long id, @RequestBody CrmCustomerSapOrg sapOrg) {
        return Result.ok(sapOrgService.create(id, sapOrg));
    }

    @PutMapping("/{id}/sap-orgs/{sapOrgId}")
    @Operation(summary = "更新SAP组织信息")
    public Result<CrmCustomerSapOrg> updateSapOrg(@PathVariable Long id, @PathVariable Long sapOrgId, @RequestBody CrmCustomerSapOrg sapOrg) {
        return Result.ok(sapOrgService.update(sapOrgId, sapOrg));
    }

    @DeleteMapping("/{id}/sap-orgs/{sapOrgId}")
    @Operation(summary = "删除SAP组织信息")
    public Result<Void> deleteSapOrg(@PathVariable Long id, @PathVariable Long sapOrgId) {
        sapOrgService.delete(sapOrgId);
        return Result.ok();
    }

    // ==================== SAP合作伙伴 ====================
    @GetMapping("/{id}/sap-partners/page")
    @Operation(summary = "分页获取SAP合作伙伴列表")
    public Result<com.huafu.crm.common.api.PageResult<CrmCustomerSapPartner>> pageSapPartners(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(sapPartnerService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/sap-partners")
    @Operation(summary = "获取SAP合作伙伴列表")
    public Result<List<CrmCustomerSapPartner>> getSapPartners(@PathVariable Long id) {
        return Result.ok(sapPartnerService.getByCustomerId(id));
    }

    @PostMapping("/{id}/sap-partners")
    @Operation(summary = "新增SAP合作伙伴")
    public Result<CrmCustomerSapPartner> createSapPartner(@PathVariable Long id, @RequestBody CrmCustomerSapPartner partner) {
        return Result.ok(sapPartnerService.create(id, partner));
    }

    @PutMapping("/{id}/sap-partners/{partnerId}")
    @Operation(summary = "更新SAP合作伙伴")
    public Result<CrmCustomerSapPartner> updateSapPartner(@PathVariable Long id, @PathVariable Long partnerId, @RequestBody CrmCustomerSapPartner partner) {
        return Result.ok(sapPartnerService.update(partnerId, partner));
    }

    @DeleteMapping("/{id}/sap-partners/{partnerId}")
    @Operation(summary = "删除SAP合作伙伴")
    public Result<Void> deleteSapPartner(@PathVariable Long id, @PathVariable Long partnerId) {
        sapPartnerService.delete(partnerId);
        return Result.ok();
    }

    // ==================== 联系人 ====================
    @GetMapping("/{id}/contacts/page")
    @Operation(summary = "分页获取联系人列表")
    public Result<com.huafu.crm.common.api.PageResult<CustomerContact>> pageContacts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(contactService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/contacts")
    @Operation(summary = "获取联系人列表")
    public Result<List<CustomerContact>> getContacts(@PathVariable Long id) {
        return Result.ok(contactService.getByCustomerId(id));
    }

    @PostMapping("/{id}/contacts")
    @Operation(summary = "新增联系人")
    public Result<CustomerContact> createContact(@PathVariable Long id, @RequestBody CustomerContact contact) {
        return Result.ok(contactService.create(id, contact));
    }

    @PutMapping("/{id}/contacts/{contactId}")
    @Operation(summary = "更新联系人")
    public Result<CustomerContact> updateContact(@PathVariable Long id, @PathVariable Long contactId, @RequestBody CustomerContact contact) {
        return Result.ok(contactService.update(contactId, contact));
    }

    @DeleteMapping("/{id}/contacts/{contactId}")
    @Operation(summary = "删除联系人")
    public Result<Void> deleteContact(@PathVariable Long id, @PathVariable Long contactId) {
        contactService.delete(contactId);
        return Result.ok();
    }

    @GetMapping("/{id}/contacts/tree")
    @Operation(summary = "获取联系人架构图（树形结构）")
    public Result<List<CustomerContact>> getContactsTree(@PathVariable Long id) {
        return Result.ok(contactService.getTreeByCustomerId(id));
    }

    // ==================== 捆绑关系 ====================
    @GetMapping("/{id}/bundles/page")
    @Operation(summary = "分页获取捆绑关系列表（作为母公司）")
    public Result<com.huafu.crm.common.api.PageResult<CrmCustomerBundle>> pageBundles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(bundleService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/bundles")
    @Operation(summary = "获取捆绑关系列表（作为母公司）")
    public Result<List<CrmCustomerBundle>> getBundles(@PathVariable Long id) {
        return Result.ok(bundleService.getByCustomerId(id));
    }

    @PostMapping("/{id}/bundles")
    @Operation(summary = "新增捆绑关系")
    public Result<CrmCustomerBundle> createBundle(@PathVariable Long id, @RequestBody CrmCustomerBundle bundle) {
        return Result.ok(bundleService.create(id, bundle.getChildCustomerId(), bundle));
    }

    @DeleteMapping("/{id}/bundles/{bundleId}")
    @Operation(summary = "删除捆绑关系")
    public Result<Void> deleteBundle(@PathVariable Long id, @PathVariable Long bundleId) {
        bundleService.delete(bundleId);
        return Result.ok();
    }

    @GetMapping("/{id}/bundle-of")
    @Operation(summary = "获取被谁捆绑（作为子公司）")
    public Result<List<CrmCustomerBundle>> getBundleOf(@PathVariable Long id) {
        return Result.ok(bundleService.getBundleOf(id));
    }

    // ==================== 附件 ====================
    @GetMapping("/{id}/attachments/page")
    @Operation(summary = "分页获取附件列表")
    public Result<com.huafu.crm.common.api.PageResult<CrmCustomerAttachment>> pageAttachments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(attachmentService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/attachments")
    @Operation(summary = "获取附件列表")
    public Result<List<CrmCustomerAttachment>> getAttachments(@PathVariable Long id) {
        return Result.ok(attachmentService.getByCustomerId(id));
    }

    @PostMapping("/{id}/attachments")
    @Operation(summary = "上传附件")
    public Result<CrmCustomerAttachment> createAttachment(@PathVariable Long id, @RequestBody CrmCustomerAttachment attachment) {
        return Result.ok(attachmentService.create(id, attachment));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    @Operation(summary = "删除附件")
    public Result<Void> deleteAttachment(@PathVariable Long id, @PathVariable Long attachmentId) {
        attachmentService.delete(id, attachmentId);
        return Result.ok();
    }

    // ==================== 客户地址 ====================
    @GetMapping("/{id}/address/page")
    @Operation(summary = "分页获取客户地址列表")
    public Result<com.huafu.crm.common.api.PageResult<CustomerAddress>> pageAddress(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(addressService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/address")
    @Operation(summary = "获取客户地址列表")
    public Result<List<CustomerAddress>> getAddress(@PathVariable Long id) {
        return Result.ok(addressService.getByCustomerId(id));
    }

    @PostMapping("/{id}/address")
    @Operation(summary = "新增地址")
    public Result<CustomerAddress> createAddress(@PathVariable Long id, @RequestBody CustomerAddress address) {
        return Result.ok(addressService.create(id, address));
    }

    @PutMapping("/{id}/address/{addressId}")
    @Operation(summary = "更新地址")
    public Result<CustomerAddress> updateAddress(@PathVariable Long id, @PathVariable Long addressId, @RequestBody CustomerAddress address) {
        return Result.ok(addressService.update(addressId, address));
    }

    @DeleteMapping("/{id}/address/{addressId}")
    @Operation(summary = "删除地址")
    public Result<Void> deleteAddress(@PathVariable Long id, @PathVariable Long addressId) {
        addressService.delete(addressId);
        return Result.ok();
    }

    // ==================== 客户交接记录 ====================
    @GetMapping("/{id}/transfer/page")
    @Operation(summary = "分页获取交接历史列表")
    public Result<com.huafu.crm.common.api.PageResult<CustomerTransfer>> pageTransfer(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(transferService.pageByCustomerId(id, current, size));
    }

    @GetMapping("/{id}/transfer")
    @Operation(summary = "获取交接历史列表")
    public Result<List<CustomerTransfer>> getTransfer(@PathVariable Long id) {
        return Result.ok(transferService.getByCustomerId(id));
    }

    @PostMapping("/{id}/transfer")
    @Operation(summary = "新建交接记录")
    public Result<CustomerTransfer> createTransfer(@PathVariable Long id, @RequestBody CustomerTransfer transfer) {
        return Result.ok(transferService.create(id, transfer));
    }

    @PutMapping("/{id}/transfer/{transferId}/confirm")
    @Operation(summary = "接交人确认交接")
    public Result<CustomerTransfer> confirmTransfer(@PathVariable Long id, @PathVariable Long transferId) {
        return Result.ok(transferService.confirm(transferId));
    }

    @DeleteMapping("/{id}/transfer/{transferId}")
    @Operation(summary = "删除交接记录")
    public Result<Void> deleteTransfer(@PathVariable Long id, @PathVariable Long transferId) {
        transferService.delete(transferId);
        return Result.ok();
    }
}
