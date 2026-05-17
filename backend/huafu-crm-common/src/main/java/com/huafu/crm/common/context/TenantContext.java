package com.huafu.crm.common.context;

public final class TenantContext {
    private static final ThreadLocal<Long> TENANT = new ThreadLocal<>();
    private TenantContext() {}
    public static void setTenantId(Long tenantId) { TENANT.set(tenantId); }
    public static Long getTenantId() { return TENANT.get(); }
    public static void clear() { TENANT.remove(); }
}
