package com.huafu.crm.ai.client;

import com.huafu.crm.ai.model.DailyReportAiResult;

public interface AiClient { DailyReportAiResult parseDailyReport(String text); }
