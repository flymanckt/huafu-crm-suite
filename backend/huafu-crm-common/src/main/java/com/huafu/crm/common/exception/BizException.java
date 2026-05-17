package com.huafu.crm.common.exception;

public class BizException extends RuntimeException {
    private final String code;

    public BizException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(int code, String message) {
        super(message);
        this.code = String.valueOf(code);
    }

    public String getCode() { return code; }
}
