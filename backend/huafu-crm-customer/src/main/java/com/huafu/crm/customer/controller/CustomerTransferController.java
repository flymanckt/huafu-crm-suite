package com.huafu.crm.customer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.dto.CustomerTransferDetailDTO;
import com.huafu.crm.customer.entity.CustomerTransfer;
import com.huafu.crm.customer.mapper.CustomerTransferMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/{customerId}/transfer")
@Tag(name = "客户交接记录", description = "客户交接历史CRUD")
public class CustomerTransferController {
    private final CustomerTransferMapper mapper;
    public CustomerTransferController(CustomerTransferMapper mapper) { this.mapper = mapper; }

    @GetMapping("/page")
    @Operation(summary = "分页获取交接历史列表")
    public Result<PageResult<CustomerTransfer>> page(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<CustomerTransfer> page = new Page<>(current, size);
        LambdaQueryWrapper<CustomerTransfer> qw = new LambdaQueryWrapper<>();
        qw.eq(CustomerTransfer::getCustomerId, customerId);
        qw.orderByDesc(CustomerTransfer::getTransferDate);
        qw.orderByDesc(CustomerTransfer::getCreatedTime);
        Page<CustomerTransfer> result = mapper.selectPage(page, qw);
        return Result.ok(new PageResult<>(result.getCurrent(), result.getSize(), result.getTotal(), result.getRecords()));
    }

    @GetMapping
    @Operation(summary = "获取交接历史列表（不分页）")
    public Result<List<CustomerTransfer>> list(@PathVariable Long customerId) {
        return Result.ok(mapper.selectList(new LambdaQueryWrapper<CustomerTransfer>()
            .eq(CustomerTransfer::getCustomerId, customerId)
            .orderByDesc(CustomerTransfer::getTransferDate)
            .orderByDesc(CustomerTransfer::getCreatedTime)));
    }

    @PostMapping
    @Operation(summary = "新建交接记录")
    public Result<CustomerTransfer> create(@PathVariable Long customerId, @RequestBody CustomerTransfer t) {
        t.setCustomerId(customerId);
        if (t.getStatus() == null) t.setStatus(1);
        mapper.insert(t);
        return Result.ok(t);
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "接交人确认交接")
    public Result<Void> confirm(@PathVariable Long id) {
        CustomerTransfer t = mapper.selectById(id);
        if (t != null) {
            t.setStatus(2);
            mapper.updateById(t);
        }
        return Result.ok();
    }
}
