package com.huafu.crm.opportunity.query;

public record OpportunityQuery(Integer current, Integer size, Integer stage, Long customerId) {
    public OpportunityQuery { if (current == null) current = 1; if (size == null) size = 20; }
}
