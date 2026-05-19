package com.huafu.crm.customer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.entity.CustomerContact;
import com.huafu.crm.customer.mapper.CustomerContactMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer-contact")
@Tag(name = "客户联系人", description = "客户联系人CRUD（软删除）")
public class CustomerContactController {
    private final CustomerContactMapper mapper;
    public CustomerContactController(CustomerContactMapper mapper) { this.mapper = mapper; }

    @GetMapping("/page")
    @Operation(summary = "分页查询客户联系人（只查有效）")
    public Result<PageResult<CustomerContact>> page(
            @RequestParam Long customerId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<CustomerContact> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerContact> qw = new LambdaQueryWrapper<>();
        qw.eq(CustomerContact::getCustomerId, customerId);
        qw.eq(CustomerContact::getIsActive, 1); // P1-5: 只查有效联系人
        qw.orderByDesc(CustomerContact::getIsMain);
        Page<CustomerContact> result = mapper.selectPage(page, qw);
        return Result.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @GetMapping("/list")
    @Operation(summary = "根据客户ID查联系人列表（只查有效，不分页）")
    public Result<?> listByCustomerId(@RequestParam Long customerId) {
        return Result.ok(mapper.selectList(new LambdaQueryWrapper<CustomerContact>()
            .eq(CustomerContact::getCustomerId, customerId)
            .eq(CustomerContact::getIsActive, 1) // P1-5: 只查有效联系人
            .orderByDesc(CustomerContact::getIsMain)));
    }

    @PostMapping
    @Operation(summary = "新增联系人")
    public Result<CustomerContact> create(@RequestBody CustomerContact contact) {
        if (contact.getIsActive() == null) contact.setIsActive(1);
        mapper.insert(contact);
        return Result.ok(contact);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新联系人")
    public Result<CustomerContact> update(@PathVariable Long id, @RequestBody CustomerContact contact) {
        contact.setId(id);
        mapper.updateById(contact);
        return Result.ok(contact);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除联系人（P1-5: 软删除）")
    public Result<Void> delete(@PathVariable Long id) {
        CustomerContact contact = mapper.selectById(id);
        if (contact == null) return Result.ok();
        contact.setIsActive(0); // 软删除
        mapper.updateById(contact);
        return Result.ok();
    }
}
