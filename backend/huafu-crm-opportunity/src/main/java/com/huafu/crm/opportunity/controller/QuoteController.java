package com.huafu.crm.opportunity.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.opportunity.dto.QuoteCreateDTO;
import com.huafu.crm.opportunity.dto.QuoteItemDTO;
import com.huafu.crm.opportunity.dto.QuoteUpdateDTO;
import com.huafu.crm.opportunity.service.QuoteService;
import com.huafu.crm.opportunity.vo.QuoteItemVO;
import com.huafu.crm.opportunity.vo.QuoteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quote")
@Tag(name = "报价单管理", description = "报价单CRUD与状态推进")
public class QuoteController {
    private final QuoteService service;
    public QuoteController(QuoteService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "新建报价单")
    public Result<QuoteVO> create(@Valid @RequestBody QuoteCreateDTO dto) {
        return Result.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取报价单详情含明细")
    public Result<QuoteVO> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询报价单")
    public Result<PageResult<QuoteVO>> page(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long salesUserId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return Result.ok(service.page(customerId, salesUserId, status, current, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新报价单")
    public Result<QuoteVO> update(@PathVariable Long id, @Valid @RequestBody QuoteUpdateDTO dto) {
        return Result.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报价单")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/send")
    @Operation(summary = "发送报价单")
    public Result<QuoteVO> send(@PathVariable Long id) {
        return Result.ok(service.send(id));
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认报价单")
    public Result<QuoteVO> confirm(@PathVariable Long id) {
        return Result.ok(service.confirm(id));
    }

    @PostMapping("/{id}/item")
    @Operation(summary = "添加报价明细行")
    public Result<QuoteItemVO> addItem(@PathVariable Long id, @Valid @RequestBody QuoteItemDTO dto) {
        return Result.ok(service.addItem(id, dto));
    }

    @PutMapping("/item/{itemId}")
    @Operation(summary = "更新报价明细行")
    public Result<QuoteItemVO> updateItem(@PathVariable Long itemId, @Valid @RequestBody QuoteItemDTO dto) {
        return Result.ok(service.updateItem(itemId, dto));
    }

    @DeleteMapping("/item/{itemId}")
    @Operation(summary = "删除报价明细行")
    public Result<Void> deleteItem(@PathVariable Long itemId) {
        service.deleteItem(itemId);
        return Result.ok();
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "获取报价单明细列表")
    public Result<List<QuoteItemVO>> getItems(@PathVariable Long id) {
        return Result.ok(service.getItems(id));
    }
}
