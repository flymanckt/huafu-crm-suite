package com.huafu.crm.common.context;

import java.util.Set;

public record LoginUser(Long userId, Long tenantId, String username, Set<String> roles) {}
