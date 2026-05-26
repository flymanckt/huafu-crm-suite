package com.huafu.crm.ai.client;

import com.huafu.crm.ai.model.DailyReportAiResult;

public interface AiClient {
    DailyReportAiResult parseDailyReport(String text);

    default DailyReportAiResult parseDailyReport(String text, Long dailyReportId) {
        return parseDailyReport(text);
    }
}
