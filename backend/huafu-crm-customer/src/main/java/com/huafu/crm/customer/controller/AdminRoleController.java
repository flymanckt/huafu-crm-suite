package com.huafu.crm.customer.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.customer.dto.RoleCreateDTO;
import com.huafu.crm.customer.dto.RoleUpdateDTO;
import com.huafu.crm.customer.dto.RoleVO;
import com.huafu.crm.customer.dto.MenuNodeVO;
import com.huafu.crm.customer.service.AdminRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/role")
@Tag(name = "角色管理", description = "系统角色CRUD与菜单授权")
public class AdminRoleController {
    private final AdminRoleService service;
    public AdminRoleController(AdminRoleService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "新建角色")
    public Result<RoleVO> create(@Valid @RequestBody RoleCreateDTO dto) {
        return Result.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    public Result<RoleVO> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询角色")
    public Result<PageResult<RoleVO>> page(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleKey,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer dataScope,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(service.page(roleName, roleKey, status, dataScope, current, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public Result<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateDTO dto) {
        return Result.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}/menus")
    @Operation(summary = "获取角色菜单ID列表")
    public Result<List<Long>> getMenus(@PathVariable Long id) {
        return Result.ok(service.getMenuIds(id));
    }

    @GetMapping("/menus/tree")
    @Operation(summary = "获取系统菜单权限树")
    public Result<List<MenuNodeVO>> menuTree() {
        return Result.ok(service.menuTree());
    }

    @PutMapping("/{id}/menus")
    @Operation(summary = "更新角色菜单")
    public Result<Void> updateMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        service.updateMenus(id, menuIds);
        return Result.ok();
    }
}
