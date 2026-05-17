package com.huafu.crm.ai.client;

import com.huafu.crm.ai.model.DailyReportAiResult;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Arrays;

public class MockMiniMaxAiClient implements AiClient {
    @Override public DailyReportAiResult parseDailyReport(String text) {
        // Phase 0 avoids binding to an unverified Spring AI MiniMax starter. Replace this adapter once dependency is confirmed.
        String safeText = text == null ? "" : text;
        List<String> lines = Arrays.stream(safeText.split("\\R"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        int opportunityCount = countLines(lines, "商机", "机会", "opportunity");
        int marketIntelligenceCount = countLines(lines, "商情", "市场情报", "intelligence");
        int lostOrderCount = countLines(lines, "丢单", "流失", "lost");
        List<String> highlights = lines.stream().limit(3).toList();
        if (highlights.isEmpty()) {
            highlights = List.of("mock parser: no daily report content");
        }
        return new DailyReportAiResult(safeText, opportunityCount, marketIntelligenceCount, lostOrderCount, highlights);
    }

    private int countLines(List<String> lines, String... keywords) {
        int count = 0;
        for (String line : lines) {
            String lower = line.toLowerCase();
            for (String keyword : keywords) {
                if (lower.contains(keyword.toLowerCase())) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
}
