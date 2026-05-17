package com.huafu.crm.performance.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.performance.dto.DailyReportCreateDTO;
import com.huafu.crm.performance.query.DailyReportQuery;
import com.huafu.crm.performance.service.DailyReportService;
import com.huafu.crm.performance.vo.DailyReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/daily-report")
@Tag(name = "销售日报", description = "日报列表与AI解析结果")
public class DailyReportController {
    private final DailyReportService service;
    public DailyReportController(DailyReportService service) { this.service = service; }

    @PostMapping @Operation(summary = "提交日报")
    public Result<DailyReportVO> create(@RequestBody DailyReportCreateDTO dto) { return Result.ok(service.create(dto)); }

    @GetMapping("/{id}") @Operation(summary = "获取日报详情")
    public Result<DailyReportVO> getById(@PathVariable Long id) { return Result.ok(service.getById(id)); }

    @PutMapping("/{id}") @Operation(summary = "更新日报")
    public Result<DailyReportVO> update(@PathVariable Long id, @RequestBody DailyReportCreateDTO dto) { return Result.ok(service.update(id, dto)); }

    @DeleteMapping("/{id}") @Operation(summary = "删除日报")
    public Result<Boolean> delete(@PathVariable Long id) { return Result.ok(service.delete(id)); }

    @GetMapping("/page") @Operation(summary = "分页查询日报")
    public Result<PageResult<DailyReportVO>> page(DailyReportQuery query) { return Result.ok(service.page(query)); }
}
