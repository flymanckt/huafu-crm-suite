package com.huafu.crm.performance.query;

public record DailyReportQuery(Integer current, Integer size, Long userId, Integer parseStatus) {
    public DailyReportQuery { if (current == null) current = 1; if (size == null) size = 20; }
}
