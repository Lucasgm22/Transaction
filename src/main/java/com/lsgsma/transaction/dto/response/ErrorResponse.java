package com.lsgsma.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(

        @Schema(description = "The timestamp when the error occurred.",
                example = "2025-08-23T16:05:34.123456")
        LocalDateTime timestamp,

        @Schema(description = "The HTTP status code.",
                example = "404")
        int status,

        @Schema(description = "The error reason phrase.",
                example = "Not Found")
        String error,

        @Schema(description = "A detailed, user-friendly message explaining the error.")
        Map<String, String> messages,

        @Schema(description = "The path of the request that resulted in an error.",
                example = "/transaction/a1b2c3d4-e5f6-7890-1234-567890abcdef")
        String path
) {}
