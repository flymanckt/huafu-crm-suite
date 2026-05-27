package com.huafu.crm.target.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.target.dto.TargetCreateDTO;
import com.huafu.crm.target.query.TargetQuery;
import com.huafu.crm.target.service.TargetService;
import com.huafu.crm.target.vo.TargetVO;
import com.huafu.crm.target.vo.TargetAchieveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/target")
@Tag(name = "目标管理", description = "目标与达成率")
public class TargetController {
    private final TargetService service;
    public TargetController(TargetService service) { this.service = service; }

    @PostMapping @Operation(summary = "创建目标")
    public Result<TargetVO> create(@RequestBody TargetCreateDTO dto) { return Result.ok(service.create(dto)); }

    @GetMapping("/{id}") @Operation(summary = "获取目标详情")
    public Result<TargetVO> getById(@PathVariable Long id) { return Result.ok(service.getById(id)); }

    @PutMapping("/{id}") @Operation(summary = "更新目标")
    public Result<TargetVO> update(@PathVariable Long id, @RequestBody TargetCreateDTO dto) { return Result.ok(service.update(id, dto)); }

    @DeleteMapping("/{id}") @Operation(summary = "删除目标")
    public Result<Boolean> delete(@PathVariable Long id) { return Result.ok(service.delete(id)); }

    @GetMapping("/page") @Operation(summary = "分页查询目标")
    public Result<PageResult<TargetVO>> page(TargetQuery query) { return Result.ok(service.page(query)); }

    @GetMapping("/{id}/achieve") @Operation(summary = "查询目标达成情况")
    public Result<PageResult<TargetAchieveVO>> achievePage(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1") Integer current,
        @RequestParam(defaultValue = "20") Integer size) {
        return Result.ok(service.achievePage(id, current, size));
    }
}
