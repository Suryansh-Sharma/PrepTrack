package com.suryansh.preptrack.core.exception;

import java.util.Map;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {
}
