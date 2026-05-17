package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.dto.CustomerCreateDTO;
import com.huafu.crm.customer.dto.CustomerUpdateDTO;
import com.huafu.crm.customer.dto.CustomerTransferDTO;
import com.huafu.crm.customer.dto.CustomerFreezeDTO;
import com.huafu.crm.customer.dto.CustomerLossDTO;
import com.huafu.crm.customer.query.CustomerQuery;
import com.huafu.crm.customer.service.CustomerService;
import com.huafu.crm.customer.vo.CustomerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@Tag(name = "客户管理", description = "客户CRUD、公海、认领")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "创建客户")
    public Result<CustomerVO> create(@Valid @RequestBody CustomerCreateDTO dto) {
        return Result.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新客户")
    public Result<CustomerVO> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDTO dto) {
        return Result.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取客户详情")
    public Result<CustomerVO> getById(@PathVariable Long id) {
        return Result.ok(service.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询客户")
    public Result<PageResult<CustomerVO>> page(CustomerQuery query) {
        return Result.ok(service.page(query));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除客户")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    @GetMapping("/public-pool")
    @Operation(summary = "公海客户列表")
    public Result<PageResult<CustomerVO>> publicPool(CustomerQuery query) {
        return Result.ok(service.pagePublicPool(query));
    }

    @PostMapping("/claim/{id}")
    @Operation(summary = "从公海认领客户")
    public Result<Void> claimFromPool(@PathVariable Long id) {
        service.claimFromPool(id);
        return Result.ok();
    }

    @PutMapping("/{id}/transfer")
    @Operation(summary = "转移客户")
    public Result<Void> transfer(@PathVariable Long id, @RequestBody CustomerTransferDTO dto) {
        service.transfer(id, dto.toUserId(), dto.reason());
        return Result.ok();
    }

    @PutMapping("/{id}/freeze")
    @Operation(summary = "冻结客户")
    public Result<Void> freeze(@PathVariable Long id, @RequestBody CustomerFreezeDTO dto) {
        service.freeze(id, dto.frozenReason());
        return Result.ok();
    }

    @PutMapping("/{id}/loss")
    @Operation(summary = "标记客户流失")
    public Result<Void> loss(@PathVariable Long id, @RequestBody CustomerLossDTO dto) {
        service.loss(id, dto.lossReason());
        return Result.ok();
    }
}
