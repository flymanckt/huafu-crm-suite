package com.huafu.crm.opportunity.query;

public record LeadQuery(Integer current, Integer size, Integer leadType, Integer status) {
    public LeadQuery { if (current == null) current = 1; if (size == null) size = 20; }
}
