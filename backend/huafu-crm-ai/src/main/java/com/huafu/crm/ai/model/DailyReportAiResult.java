package com.huafu.crm.ai.model;

import java.util.List;
import java.util.Map;

public record DailyReportAiResult(
        String originalText,
        int opportunityCount,
        int marketIntelligenceCount,
        int lostOrderCount,
        List<String> highlights,
        Map<String, Object> parsedJson) {

    public DailyReportAiResult(String originalText, int opportunityCount, int marketIntelligenceCount, int lostOrderCount, List<String> highlights) {
        this(originalText, opportunityCount, marketIntelligenceCount, lostOrderCount, highlights, Map.of());
    }
}
