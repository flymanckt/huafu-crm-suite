package com.huafu.crm.common.api;

import java.util.List;

public record PageResult<T>(long current, long size, long total, List<T> records) {
    public static <T> PageResult<T> of(long current, long size, long total, List<T> records) {
        return new PageResult<>(current, size, total, records);
    }
}
