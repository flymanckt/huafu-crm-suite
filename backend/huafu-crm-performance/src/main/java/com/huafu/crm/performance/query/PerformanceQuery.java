package com.huafu.crm.performance.query;

public record PerformanceQuery(Integer current, Integer size, Integer statYear, Integer statMonth) {
    public PerformanceQuery { if (current == null) current = 1; if (size == null) size = 20; }
}
