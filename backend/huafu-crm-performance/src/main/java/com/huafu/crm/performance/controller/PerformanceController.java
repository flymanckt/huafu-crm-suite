package com.huafu.crm.performance.controller;

import com.huafu.crm.common.api.PageResult;
import com.huafu.crm.common.api.Result;
import com.huafu.crm.performance.job.PerformanceAutoCalcJob;
import com.huafu.crm.performance.query.PerformanceQuery;
import com.huafu.crm.performance.service.PerformanceService;
import com.huafu.crm.performance.vo.PerformanceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/performance")
@Tag(name = "勤力度评分", description = "G/C/O等级与勤力度评分")
public class PerformanceController {
    private final PerformanceService service;
    private final PerformanceAutoCalcJob calcJob;
    public PerformanceController(PerformanceService service, PerformanceAutoCalcJob calcJob) {
        this.service = service;
        this.calcJob = calcJob;
    }

    @GetMapping("/page") @Operation(summary = "分页查询勤力度")
    public Result<PageResult<PerformanceVO>> page(PerformanceQuery query) { return Result.ok(service.page(query)); }

    @PostMapping("/calc/{userId}") @Operation(summary = "手动重新计算绩效")
    public Result<Void> recalc(@PathVariable Long userId) {
        java.time.YearMonth ym = java.time.YearMonth.now();
        calcJob.calcForUser(userId, ym.getYear(), ym.getMonthValue());
        return Result.ok();
    }

    @PostMapping("/calc-all") @Operation(summary = "重新计算所有绩效")
    public Result<Void> recalcAll() {
        calcJob.autoCalcAll();
        return Result.ok();
    }
}
