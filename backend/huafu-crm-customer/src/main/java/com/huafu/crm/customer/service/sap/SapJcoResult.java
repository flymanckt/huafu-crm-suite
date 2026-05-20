package com.huafu.crm.customer.service.sap;

public record SapJcoResult(boolean success, String responsePayload, String errorMessage, boolean retryable) {
    public static SapJcoResult success(String responsePayload) {
        return new SapJcoResult(true, responsePayload, null, false);
    }

    public static SapJcoResult failed(String errorMessage, boolean retryable) {
        return new SapJcoResult(false, null, errorMessage, retryable);
    }
}
