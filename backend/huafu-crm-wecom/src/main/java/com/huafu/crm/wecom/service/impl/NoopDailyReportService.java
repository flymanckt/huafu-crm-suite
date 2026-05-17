package com.huafu.crm.wecom.service.impl;

import com.huafu.crm.wecom.service.DailyReportService;
import org.springframework.stereotype.Service;

@Service
public class NoopDailyReportService implements DailyReportService {
    @Override public void handleDailyReportText(String userId, String content) { /* persist/AI parse in later phase */ }
}
