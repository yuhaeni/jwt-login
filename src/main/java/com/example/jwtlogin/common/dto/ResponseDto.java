package com.example.jwtlogin.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class ResponseDto<T> {
    private boolean result;
    private Integer status;
    private String message;
    private T data;

    @Builder
    public ResponseDto(boolean result, Integer status, String message, T data) {
        this.result = result;
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
