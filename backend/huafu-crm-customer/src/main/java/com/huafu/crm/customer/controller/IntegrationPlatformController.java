package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.dto.IntegrationConnectionConfigDTO;
import com.huafu.crm.customer.dto.IntegrationFieldMappingDTO;
import com.huafu.crm.customer.dto.IntegrationInterfaceDTO;
import com.huafu.crm.customer.dto.IntegrationLogCreateDTO;
import com.huafu.crm.customer.dto.SapRfcConfigDTO;
import com.huafu.crm.customer.entity.IntegrationConnectionConfig;
import com.huafu.crm.customer.entity.IntegrationFieldMapping;
import com.huafu.crm.customer.entity.IntegrationInterface;
import com.huafu.crm.customer.entity.IntegrationLog;
import com.huafu.crm.customer.entity.SapRfcConfig;
import com.huafu.crm.customer.service.IntegrationPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crm/v1/integration")
@Tag(name = "集成平台", description = "SAP RFC配置、接口字段映射、平台日志和异常重推")
public class IntegrationPlatformController {
    private final IntegrationPlatformService service;

    public IntegrationPlatformController(IntegrationPlatformService service) {
        this.service = service;
    }

    @GetMapping("/connections")
    @Operation(summary = "查询通用连接配置")
    public Result<List<IntegrationConnectionConfig>> listConnectionConfigs(@RequestParam(required = false) String connectionType) {
        return Result.ok(service.listConnectionConfigs(connectionType));
    }

    @PostMapping("/connections")
    @Operation(summary = "新增通用连接配置")
    public Result<IntegrationConnectionConfig> createConnectionConfig(@Valid @RequestBody IntegrationConnectionConfigDTO dto) {
        return Result.ok(service.saveConnectionConfig(dto));
    }

    @PutMapping("/connections/{id}")
    @Operation(summary = "更新通用连接配置")
    public Result<IntegrationConnectionConfig> updateConnectionConfig(@PathVariable Long id, @Valid @RequestBody IntegrationConnectionConfigDTO dto) {
        return Result.ok(service.updateConnectionConfig(id, dto));
    }

    @DeleteMapping("/connections/{id}")
    @Operation(summary = "删除通用连接配置")
    public Result<Void> deleteConnectionConfig(@PathVariable Long id) {
        service.deleteConnectionConfig(id);
        return Result.ok();
    }

    @PostMapping("/connections/{id}/test")
    @Operation(summary = "测试通用连接配置参数")
    public Result<String> testConnectionConfig(@PathVariable Long id) {
        return Result.ok(service.testConnectionConfig(id));
    }

    @GetMapping("/sap-rfc-configs")
    @Operation(summary = "查询SAP RFC配置")
    public Result<List<SapRfcConfig>> listSapRfcConfigs() {
        return Result.ok(service.listSapRfcConfigs());
    }

    @PostMapping("/sap-rfc-configs")
    @Operation(summary = "新增SAP RFC配置")
    public Result<SapRfcConfig> createSapRfcConfig(@Valid @RequestBody SapRfcConfigDTO dto) {
        return Result.ok(service.saveSapRfcConfig(dto));
    }

    @PutMapping("/sap-rfc-configs/{id}")
    @Operation(summary = "更新SAP RFC配置")
    public Result<SapRfcConfig> updateSapRfcConfig(@PathVariable Long id, @Valid @RequestBody SapRfcConfigDTO dto) {
        return Result.ok(service.updateSapRfcConfig(id, dto));
    }

    @DeleteMapping("/sap-rfc-configs/{id}")
    @Operation(summary = "删除SAP RFC配置")
    public Result<Void> deleteSapRfcConfig(@PathVariable Long id) {
        service.deleteSapRfcConfig(id);
        return Result.ok();
    }

    @PostMapping("/sap-rfc-configs/{id}/test")
    @Operation(summary = "测试SAP RFC配置参数")
    public Result<String> testSapRfcConfig(@PathVariable Long id) {
        return Result.ok(service.testSapRfcConfig(id));
    }

    @GetMapping("/interfaces")
    @Operation(summary = "查询集成接口")
    public Result<List<IntegrationInterface>> listInterfaces(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String systemCode,
        @RequestParam(required = false) String enabled
    ) {
        return Result.ok(service.listInterfaces(keyword, systemCode, enabled));
    }

    @PostMapping("/interfaces")
    @Operation(summary = "新增集成接口")
    public Result<IntegrationInterface> createInterface(@Valid @RequestBody IntegrationInterfaceDTO dto) {
        return Result.ok(service.saveInterface(dto));
    }

    @PutMapping("/interfaces/{id}")
    @Operation(summary = "更新集成接口")
    public Result<IntegrationInterface> updateInterface(@PathVariable Long id, @Valid @RequestBody IntegrationInterfaceDTO dto) {
        return Result.ok(service.updateInterface(id, dto));
    }

    @DeleteMapping("/interfaces/{id}")
    @Operation(summary = "删除集成接口")
    public Result<Void> deleteInterface(@PathVariable Long id) {
        service.deleteInterface(id);
        return Result.ok();
    }

    @GetMapping("/interfaces/{interfaceId}/mappings")
    @Operation(summary = "查询接口字段映射")
    public Result<List<IntegrationFieldMapping>> listMappings(@PathVariable Long interfaceId) {
        return Result.ok(service.listMappings(interfaceId));
    }

    @PostMapping("/mappings")
    @Operation(summary = "新增字段映射")
    public Result<IntegrationFieldMapping> createMapping(@Valid @RequestBody IntegrationFieldMappingDTO dto) {
        return Result.ok(service.saveMapping(dto));
    }

    @PutMapping("/mappings/{id}")
    @Operation(summary = "更新字段映射")
    public Result<IntegrationFieldMapping> updateMapping(@PathVariable Long id, @Valid @RequestBody IntegrationFieldMappingDTO dto) {
        return Result.ok(service.updateMapping(id, dto));
    }

    @DeleteMapping("/mappings/{id}")
    @Operation(summary = "删除字段映射")
    public Result<Void> deleteMapping(@PathVariable Long id) {
        service.deleteMapping(id);
        return Result.ok();
    }

    @GetMapping("/logs")
    @Operation(summary = "分页查询集成日志")
    public Result<PageResult<IntegrationLog>> pageLogs(
        @RequestParam(defaultValue = "1") long current,
        @RequestParam(defaultValue = "20") long size,
        @RequestParam(required = false) String interfaceCode,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword
    ) {
        return Result.ok(service.pageLogs(current, size, interfaceCode, status, keyword));
    }

    @PostMapping("/logs")
    @Operation(summary = "写入集成日志")
    public Result<IntegrationLog> createLog(@Valid @RequestBody IntegrationLogCreateDTO dto) {
        return Result.ok(service.createLog(dto));
    }

    @PostMapping("/logs/{id}/repush")
    @Operation(summary = "异常数据重新推送")
    public Result<IntegrationLog> repushLog(@PathVariable Long id) {
        return Result.ok(service.repushLog(id));
    }

    @PostMapping("/logs/{id}/execute")
    @Operation(summary = "立即执行集成日志")
    public Result<IntegrationLog> executeLog(@PathVariable Long id) {
        return Result.ok(service.executeLog(id));
    }

    @PostMapping("/logs/execute-pending")
    @Operation(summary = "执行待推送集成日志")
    public Result<Integer> executePendingLogs(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(service.executePendingLogs(limit));
    }
}
