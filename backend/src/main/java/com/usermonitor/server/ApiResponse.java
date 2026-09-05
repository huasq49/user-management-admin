package com.usermonitor.server;

public record ApiResponse<T>(int code, String message, T data, long timestamp) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(0, "ok", data, System.currentTimeMillis()); }
    public static <T> ApiResponse<T> fail(int code, String message) { return new ApiResponse<>(code, message, null, System.currentTimeMillis()); }
}
