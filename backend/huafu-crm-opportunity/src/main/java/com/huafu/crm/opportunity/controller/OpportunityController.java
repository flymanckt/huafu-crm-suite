package com.huafu.crm.opportunity.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.opportunity.dto.OpportunityCreateDTO;
import com.huafu.crm.opportunity.dto.StageUpdateDTO;
import com.huafu.crm.opportunity.query.OpportunityQuery;
import com.huafu.crm.opportunity.service.OpportunityService;
import com.huafu.crm.opportunity.vo.OpportunityVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/opportunity")
@Tag(name = "商机管理", description = "商机CRUD与阶段推进")
public class OpportunityController {
    private final OpportunityService service;
    public OpportunityController(OpportunityService service) { this.service = service; }

    @PostMapping @Operation(summary = "创建商机")
    public Result<OpportunityVO> create(@RequestBody OpportunityCreateDTO dto) { return Result.ok(service.create(dto)); }

    @GetMapping("/{id}") @Operation(summary = "获取商机详情")
    public Result<OpportunityVO> getById(@PathVariable Long id) { return Result.ok(service.getById(id)); }

    @PutMapping("/{id}") @Operation(summary = "更新商机")
    public Result<OpportunityVO> update(@PathVariable Long id, @RequestBody OpportunityCreateDTO dto) { return Result.ok(service.update(id, dto)); }

    @DeleteMapping("/{id}") @Operation(summary = "删除商机")
    public Result<Boolean> delete(@PathVariable Long id) { return Result.ok(service.delete(id)); }

    @GetMapping("/page") @Operation(summary = "分页查询商机")
    public Result<PageResult<OpportunityVO>> page(OpportunityQuery query) { return Result.ok(service.page(query)); }
    @PutMapping("/{id}/stage")
    @Operation(summary = "推进商机阶段（可带赢单金额/丢单原因）")
    public Result<OpportunityVO> updateStage(@PathVariable Long id, @RequestBody StageUpdateDTO dto) {
        return Result.ok(service.updateStage(id, dto.stage(), dto));
    }

    // Simple stage-only endpoint (stage as query param)
    @PutMapping("/{id}/stage-simple")
    @Operation(summary = "推进商机阶段（仅阶段号）")
    public Result<OpportunityVO> updateStageSimple(@PathVariable Long id, @RequestParam Integer stage) {
        return Result.ok(service.updateStage(id, stage, null));
    }
}
