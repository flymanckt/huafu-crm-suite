package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.dto.ModuleRecordSaveDTO;
import com.huafu.crm.customer.service.ModuleRecordService;
import com.huafu.crm.customer.vo.ModuleRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crm/v1/module-records")
@Tag(name = "业务模块台账", description = "展会、样品、色卡、订单等配置化模块台账")
public class ModuleRecordController {
    private final ModuleRecordService service;

    public ModuleRecordController(ModuleRecordService service) {
        this.service = service;
    }

    @GetMapping("/{moduleKey}")
    @Operation(summary = "分页查询模块台账")
    public Result<PageResult<ModuleRecordVO>> page(
        @PathVariable String moduleKey,
        @RequestParam(defaultValue = "1") long current,
        @RequestParam(defaultValue = "20") long size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status
    ) {
        return Result.ok(service.page(moduleKey, current, size, keyword, status));
    }

    @PostMapping("/{moduleKey}")
    @Operation(summary = "新增模块台账")
    public Result<ModuleRecordVO> create(@PathVariable String moduleKey, @RequestBody ModuleRecordSaveDTO dto) {
        return Result.ok(service.create(moduleKey, dto));
    }

    @PutMapping("/{moduleKey}/{id}")
    @Operation(summary = "更新模块台账")
    public Result<ModuleRecordVO> update(@PathVariable String moduleKey, @PathVariable Long id, @RequestBody ModuleRecordSaveDTO dto) {
        return Result.ok(service.update(moduleKey, id, dto));
    }

    @DeleteMapping("/{moduleKey}/{id}")
    @Operation(summary = "删除模块台账")
    public Result<Void> delete(@PathVariable String moduleKey, @PathVariable Long id) {
        service.delete(moduleKey, id);
        return Result.ok();
    }
}
