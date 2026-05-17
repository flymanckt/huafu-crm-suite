package com.huafu.crm.performance.query;

public record VisitRecordQuery(Integer current, Integer size, Long userId, String visitDate) {
    public VisitRecordQuery { if (current == null) current = 1; if (size == null) size = 20; }
}
