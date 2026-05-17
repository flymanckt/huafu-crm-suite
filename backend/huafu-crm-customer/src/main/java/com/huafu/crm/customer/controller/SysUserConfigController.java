package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.Result;
import com.huafu.crm.common.context.UserContext;
import com.huafu.crm.common.entity.SysUserFilterConfig;
import com.huafu.crm.customer.service.SysUserConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crm/v1/user-configs")
public class SysUserConfigController {

    private final SysUserConfigService userConfigService;
    private final UserContext userContext;

    public SysUserConfigController(SysUserConfigService userConfigService, UserContext userContext) {
        this.userConfigService = userConfigService;
        this.userContext = userContext;
    }

    private Long getCurrentUserId() {
        return userContext.getCurrentUserId().orElse(1L);
    }

    @GetMapping("/columns/{pageCode}")
    public Result<String> getColumnConfig(@PathVariable String pageCode) {
        return Result.ok(userConfigService.getColumnConfig(getCurrentUserId(), pageCode));
    }

    @PutMapping("/columns/{pageCode}")
    public Result<Boolean> saveColumnConfig(@PathVariable String pageCode,
                                            @RequestParam(required = false) String columnConfigs,
                                            @RequestBody(required = false) Map<String, Object> body) {
        String configs = resolveConfigs(columnConfigs, body, "columnConfigs");
        return Result.ok(userConfigService.saveColumnConfig(getCurrentUserId(), pageCode, configs, null));
    }

    @PostMapping("/columns/{pageCode}/reset")
    public Result<Boolean> resetColumnConfig(@PathVariable String pageCode) {
        return Result.ok(userConfigService.resetColumnConfig(getCurrentUserId(), pageCode));
    }

    @GetMapping("/filters/{pageCode}")
    public Result<String> getFilterConfig(@PathVariable String pageCode) {
        return Result.ok(userConfigService.getFilterConfig(getCurrentUserId(), pageCode));
    }

    @PutMapping("/filters/{pageCode}")
    public Result<Boolean> saveFilterConfig(@PathVariable String pageCode, 
                                            @RequestParam(required = false) String filterConfigs,
                                            @RequestBody(required = false) Map<String, Object> body,
                                            @RequestParam(required = false) String configName) {
        String configs = resolveConfigs(filterConfigs, body, "filterConfigs");
        return Result.ok(userConfigService.saveFilterConfig(getCurrentUserId(), pageCode, configs, configName));
    }

    @GetMapping("/filters/{pageCode}/list")
    public Result<List<SysUserFilterConfig>> getFilterConfigList(@PathVariable String pageCode) {
        return Result.ok(userConfigService.getFilterConfigList(getCurrentUserId(), pageCode));
    }

    @DeleteMapping("/filters/{pageCode}/{configName}")
    public Result<Boolean> deleteFilterConfig(@PathVariable String pageCode, @PathVariable String configName) {
        return Result.ok(userConfigService.deleteFilterConfig(getCurrentUserId(), pageCode, configName));
    }

    private String resolveConfigs(String requestParamValue, Map<String, Object> body, String bodyKey) {
        if (requestParamValue != null && !requestParamValue.isBlank()) {
            return requestParamValue;
        }
        if (body == null) {
            return "{}";
        }
        Object value = body.get(bodyKey);
        if (value == null) {
            value = body;
        }
        if (value instanceof String text) {
            return text;
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
