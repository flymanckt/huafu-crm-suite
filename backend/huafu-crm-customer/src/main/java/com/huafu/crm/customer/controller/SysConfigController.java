package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.dto.SysConfigDTO;
import com.huafu.crm.customer.entity.SysConfig;
import com.huafu.crm.customer.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crm/v1/system/configs")
@Tag(name = "系统配置", description = "系统参数配置")
public class SysConfigController {

    private final SysConfigService sysConfigService;

    public SysConfigController(SysConfigService sysConfigService) {
        this.sysConfigService = sysConfigService;
    }

    @GetMapping("/groups")
    @Operation(summary = "查询所有配置分组")
    public Result<List<String>> listGroups() {
        return Result.ok(sysConfigService.listGroups());
    }

    @GetMapping
    @Operation(summary = "查询所有可见配置")
    public Result<List<SysConfig>> listVisible() {
        return Result.ok(sysConfigService.listVisible());
    }

    @GetMapping("/group/{group}")
    @Operation(summary = "按分组查询配置")
    public Result<List<SysConfig>> listByGroup(@PathVariable String group) {
        return Result.ok(sysConfigService.listByGroup(group));
    }

    @GetMapping("/key/{configKey}")
    @Operation(summary = "按Key查询单个配置")
    public Result<SysConfig> getByKey(@PathVariable String configKey) {
        return Result.ok(sysConfigService.getByConfigKey(configKey));
    }

    @GetMapping("/value/{configKey}")
    @Operation(summary = "获取配置值")
    public Result<String> getValue(@PathVariable String configKey) {
        return Result.ok(sysConfigService.getConfigValue(configKey));
    }

    @PostMapping
    @Operation(summary = "新建配置")
    public Result<Boolean> create(@Valid @RequestBody SysConfigDTO dto) {
        return Result.ok(sysConfigService.saveConfig(dto));
    }

    @PutMapping
    @Operation(summary = "更新配置")
    public Result<Boolean> update(@Valid @RequestBody SysConfigDTO dto) {
        return Result.ok(sysConfigService.updateConfig(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(sysConfigService.deleteConfig(id));
    }
}
