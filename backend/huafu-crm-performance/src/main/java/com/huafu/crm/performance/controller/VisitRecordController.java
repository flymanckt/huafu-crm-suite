package com.huafu.crm.performance.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.performance.dto.VisitRecordCreateDTO;
import com.huafu.crm.performance.query.VisitRecordQuery;
import com.huafu.crm.performance.service.VisitRecordService;
import com.huafu.crm.performance.vo.VisitRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visit-record")
@Tag(name = "拜访记录", description = "客户拜访记录管理")
public class VisitRecordController {
    private final VisitRecordService service;
    public VisitRecordController(VisitRecordService service) { this.service = service; }

    @PostMapping @Operation(summary = "创建拜访记录")
    public Result<VisitRecordVO> create(@RequestBody VisitRecordCreateDTO dto) { return Result.ok(service.create(dto)); }

    @GetMapping("/{id}") @Operation(summary = "获取拜访记录详情")
    public Result<VisitRecordVO> getById(@PathVariable Long id) { return Result.ok(service.getById(id)); }

    @PutMapping("/{id}") @Operation(summary = "更新拜访记录")
    public Result<VisitRecordVO> update(@PathVariable Long id, @RequestBody VisitRecordCreateDTO dto) { return Result.ok(service.update(id, dto)); }

    @DeleteMapping("/{id}") @Operation(summary = "删除拜访记录")
    public Result<Boolean> delete(@PathVariable Long id) { return Result.ok(service.delete(id)); }

    @GetMapping("/page") @Operation(summary = "分页查询拜访记录")
    public Result<PageResult<VisitRecordVO>> page(VisitRecordQuery query) { return Result.ok(service.page(query)); }
}
