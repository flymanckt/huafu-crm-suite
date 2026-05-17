package com.huafu.crm.opportunity.query;

public record LostOrderQuery(Integer current, Integer size, Integer lostType) {
    public LostOrderQuery { if (current == null) current = 1; if (size == null) size = 20; }
}
