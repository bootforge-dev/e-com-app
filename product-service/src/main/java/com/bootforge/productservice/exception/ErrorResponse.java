package com.bootforge.productservice.exception;

import lombok.Builder;

import java.util.Map;

@Builder
public record ErrorResponse(
        Integer status,
        String error,
        String message,
        String path,
        Map<String, String> errors
) {
    public static ErrorResponse build(Integer status, String error, String message, String path, Map<String, String> errors){
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .errors(errors)
                .build();
    }

    public static ErrorResponse build(Integer status, String error, String message, String path){
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
