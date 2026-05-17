package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.entity.CustomerAddress;
import com.huafu.crm.customer.service.CrmCustomerAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/{customerId}/address")
@Tag(name = "客户地址", description = "客户地址CRUD")
public class CustomerAddressController {
    private final CrmCustomerAddressService service;
    public CustomerAddressController(CrmCustomerAddressService service) { this.service = service; }

    @GetMapping("/page")
    @Operation(summary = "分页获取客户地址列表")
    public Result<PageResult<CustomerAddress>> page(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(service.pageByCustomerId(customerId, current, size));
    }

    @GetMapping
    @Operation(summary = "获取客户地址列表（不分页）")
    public Result<List<CustomerAddress>> list(@PathVariable Long customerId) {
        return Result.ok(service.getByCustomerId(customerId));
    }

    @PostMapping
    @Operation(summary = "新增地址")
    public Result<CustomerAddress> create(@PathVariable Long customerId, @RequestBody CustomerAddress addr) {
        return Result.ok(service.create(customerId, addr));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新地址")
    public Result<CustomerAddress> update(@PathVariable Long customerId, @PathVariable Long id, @RequestBody CustomerAddress addr) {
        return Result.ok(service.update(id, addr));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
