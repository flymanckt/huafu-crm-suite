package com.huafu.crm.ai.model;

import java.time.LocalDateTime;

public record ParseTask(Long id, Long tenantId, String sourceType, String content, LocalDateTime createdAt) {}
