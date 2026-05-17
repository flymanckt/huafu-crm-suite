package com.huafu.crm.performance.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huafu.crm.performance.entity.Performance;
import com.huafu.crm.performance.entity.VisitRecord;
import com.huafu.crm.performance.entity.DailyReport;
import com.huafu.crm.performance.entity.Customer;
import com.huafu.crm.performance.mapper.PerformanceMapper;
import com.huafu.crm.performance.mapper.VisitRecordMapper;
import com.huafu.crm.performance.mapper.DailyReportMapper;
import com.huafu.crm.performance.mapper.CustomerMapper;
import com.huafu.crm.performance.mapper.TargetMapper;
import com.huafu.crm.performance.entity.Target;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
public class PerformanceAutoCalcJob {
    private final PerformanceMapper performanceMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final DailyReportMapper dailyReportMapper;
    private final CustomerMapper customerMapper;
    private final TargetMapper targetMapper;

    public PerformanceAutoCalcJob(PerformanceMapper performanceMapper,
                                  VisitRecordMapper visitRecordMapper,
                                  DailyReportMapper dailyReportMapper,
                                  CustomerMapper customerMapper,
                                  TargetMapper targetMapper) {
        this.performanceMapper = performanceMapper;
        this.visitRecordMapper = visitRecordMapper;
        this.dailyReportMapper = dailyReportMapper;
        this.customerMapper = customerMapper;
        this.targetMapper = targetMapper;
    }

    /** 每日凌晨2点执行 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoCalcAll() {
        YearMonth ym = YearMonth.now();
        int year = ym.getYear();
        int month = ym.getMonthValue();

        // 遍历所有用户（简化版：取所有有绩效记录的用户）
        List<Performance> existingPerf = performanceMapper.selectList(
            new LambdaQueryWrapper<Performance>()
                .eq(Performance::getStatYear, year)
                .eq(Performance::getStatMonth, month));
        for (Performance p : existingPerf) {
            calcForUser(p.getUserId(), year, month);
        }
    }

    /** 手动触发计算 */
    public void calcForUser(Long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        // 拜访次数
        long visitCount = visitRecordMapper.selectCount(
            new LambdaQueryWrapper<VisitRecord>()
                .eq(VisitRecord::getUserId, userId)
                .between(VisitRecord::getVisitDate, start, end));

        // 日报数量
        long reportCount = dailyReportMapper.selectCount(
            new LambdaQueryWrapper<DailyReport>()
                .eq(DailyReport::getUserId, userId)
                .between(DailyReport::getReportDate, start, end));

        // 新增客户数
        long newCustomerCount = customerMapper.selectCount(
            new LambdaQueryWrapper<Customer>()
                .eq(Customer::getOwnerUserId, userId)
                .between(Customer::getCreatedTime, start.atStartOfDay(), end.atTime(23, 59, 59)));

        // 目标
        LambdaQueryWrapper<Target> tqw = new LambdaQueryWrapper<Target>()
            .eq(Target::getUserId, userId)
            .eq(Target::getTargetYear, year)
            .eq(Target::getTargetMonth, month);
        List<Target> targets = targetMapper.selectList(tqw);

        int visitTarget = 0, reportTarget = 0, newCustomerTarget = 0;
        for (Target t : targets) {
            if (t.getMetricType() == 4) visitTarget = t.getTargetValue().intValue(); // 拜访
            if (t.getMetricType() == 3) newCustomerTarget = t.getTargetValue().intValue(); // 客户数
        }
        reportTarget = 22; // 默认工作日

        BigDecimal visitRate = visitTarget > 0
            ? BigDecimal.valueOf(visitCount * 100.0 / visitTarget) : BigDecimal.ZERO;
        BigDecimal reportRate = reportTarget > 0
            ? BigDecimal.valueOf(reportCount * 100.0 / reportTarget) : BigDecimal.ZERO;
        BigDecimal newCustomerRate = newCustomerTarget > 0
            ? BigDecimal.valueOf(newCustomerCount * 100.0 / newCustomerTarget) : BigDecimal.ZERO;

        // upsert
        Performance perf = performanceMapper.selectOne(
            new LambdaQueryWrapper<Performance>()
                .eq(Performance::getUserId, userId)
                .eq(Performance::getStatYear, year)
                .eq(Performance::getStatMonth, month));

        if (perf == null) {
            perf = new Performance();
            perf.setUserId(userId);
            perf.setStatYear(year);
            perf.setStatMonth(month);
            perf.setVisitTarget(visitTarget);
            perf.setReportTarget(reportTarget);
            perf.setNewCustomerTarget(newCustomerTarget);
            if (perf.getTenantId() == null) perf.setTenantId(1L);
            performanceMapper.insert(perf);
        } else {
            perf.setVisitTarget(visitTarget);
            perf.setReportTarget(reportTarget);
            perf.setNewCustomerTarget(newCustomerTarget);
        }
        perf.setVisitCount((int) visitCount);
        perf.setReportCount((int) reportCount);
        perf.setNewCustomerCount((int) newCustomerCount);
        perf.setVisitRate(visitRate);
        perf.setReportRate(reportRate);
        perf.setNewCustomerRate(newCustomerRate);
        performanceMapper.updateById(perf);
    }
}
