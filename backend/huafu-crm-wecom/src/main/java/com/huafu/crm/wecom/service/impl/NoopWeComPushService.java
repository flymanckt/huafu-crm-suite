package com.huafu.crm.wecom.service.impl;

import com.huafu.crm.wecom.service.WeComPushService;
import org.springframework.stereotype.Service;

@Service
public class NoopWeComPushService implements WeComPushService {
    @Override public void pushText(String userId, String content) { /* call WeCom API in later phase; no secrets in Phase 0 */ }
}
