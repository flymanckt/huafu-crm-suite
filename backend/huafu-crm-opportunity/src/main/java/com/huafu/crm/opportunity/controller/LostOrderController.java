package com.huafu.crm.opportunity.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.opportunity.dto.LostOrderCreateDTO;
import com.huafu.crm.opportunity.query.LostOrderQuery;
import com.huafu.crm.opportunity.service.LostOrderService;
import com.huafu.crm.opportunity.vo.LostOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lost-order")
@Tag(name = "丢单记录", description = "丢单记录管理")
public class LostOrderController {
    private final LostOrderService service;
    public LostOrderController(LostOrderService service) { this.service = service; }

    @PostMapping @Operation(summary = "创建丢单记录")
    public Result<LostOrderVO> create(@RequestBody LostOrderCreateDTO dto) { return Result.ok(service.create(dto)); }

    @GetMapping("/{id}") @Operation(summary = "获取丢单详情")
    public Result<LostOrderVO> getById(@PathVariable Long id) { return Result.ok(service.getById(id)); }

    @PutMapping("/{id}") @Operation(summary = "更新丢单记录")
    public Result<LostOrderVO> update(@PathVariable Long id, @RequestBody LostOrderCreateDTO dto) { return Result.ok(service.update(id, dto)); }

    @DeleteMapping("/{id}") @Operation(summary = "删除丢单记录")
    public Result<Boolean> delete(@PathVariable Long id) { return Result.ok(service.delete(id)); }

    @GetMapping("/page") @Operation(summary = "分页查询丢单")
    public Result<PageResult<LostOrderVO>> page(LostOrderQuery query) { return Result.ok(service.page(query)); }
}
