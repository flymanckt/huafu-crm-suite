package com.huafu.crm.opportunity.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.opportunity.dto.LeadCreateDTO;
import com.huafu.crm.opportunity.query.LeadQuery;
import com.huafu.crm.opportunity.service.LeadService;
import com.huafu.crm.opportunity.vo.LeadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lead")
@Tag(name = "线索商情", description = "线索/商情管理")
public class LeadController {
    private final LeadService service;
    public LeadController(LeadService service) { this.service = service; }

    @PostMapping @Operation(summary = "创建线索/商情")
    public Result<LeadVO> create(@RequestBody LeadCreateDTO dto) { return Result.ok(service.create(dto)); }

    @GetMapping("/{id}") @Operation(summary = "获取线索详情")
    public Result<LeadVO> getById(@PathVariable Long id) { return Result.ok(service.getById(id)); }

    @PutMapping("/{id}") @Operation(summary = "更新线索/商情")
    public Result<LeadVO> update(@PathVariable Long id, @RequestBody LeadCreateDTO dto) { return Result.ok(service.update(id, dto)); }

    @DeleteMapping("/{id}") @Operation(summary = "删除线索/商情")
    public Result<Boolean> delete(@PathVariable Long id) { return Result.ok(service.delete(id)); }

    @GetMapping("/page") @Operation(summary = "分页查询线索")
    public Result<PageResult<LeadVO>> page(LeadQuery query) { return Result.ok(service.page(query)); }
}
