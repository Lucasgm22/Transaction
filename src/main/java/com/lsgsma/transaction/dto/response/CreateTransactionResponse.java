package com.lsgsma.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record CreateTransactionResponse(
        @Schema(description = "Unique identifier of the transaction (UUID)",
                example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        UUID id
) {}
