package com.lsgsma.transaction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConvertedTransactionResponse(

        @Schema(description = "Unique identifier of the transaction (UUID)",
                example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
        UUID id,

        @Schema(description = "Brief description of the transaction, max 50 characters.",
                example = "New keyboard for home office")
        String description,

        @Schema(description = "Date of the transaction in YYYY-MM-DD format.",
                example = "2025-08-20")
        LocalDate transactionDate,


        @Schema(description = "Original transaction purchase amount in US Dollars.",
                example = "150.75")
        BigDecimal originalPurchaseAmount,

        @Schema(description = "The exchange rate used for the conversion on the transaction date.",
                example = "5.123")
        BigDecimal exchangeRate,

        @Schema(description = "The converted transaction purchase amount in the target currency.",
                example = "772.35")
        BigDecimal convertedAmount
) {}
