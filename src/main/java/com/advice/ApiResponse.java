package com.advice;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {
    private LocalDateTime  timestamp;
    private T data;
    private ApiError apiError;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    public ApiResponse(T data) {
        this();
        this.data = data;
    }


    public ApiResponse(ApiError apiError) {
        this();
        this.apiError = apiError;
    }
}
