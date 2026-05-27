package com.huafu.crm.target.query;

public record TargetQuery(Integer current, Integer size, Integer targetYear, Integer targetMonth) {
    public TargetQuery { if (current == null) current = 1; if (size == null) size = 20; }
}
