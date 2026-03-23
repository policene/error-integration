package com.policene.error_handler.handler.dto;

public record ErrorMessage(
        String message,
        String cause,
        int count,
        boolean critical
) {
}
