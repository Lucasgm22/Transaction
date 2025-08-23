package com.lsgsma.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(

        @Schema(description = "Brief description of the transaction, max 50 characters.",
                example = "New keyboard for home office")
        @NotBlank @Size(max = 50)
        String description,

        @Schema(description = "Date of the transaction in YYYY-MM-DD format.",
                example = "2025-08-20")
        @NotNull @PastOrPresent
        LocalDate transactionDate,

        @Schema(description = "Total transaction purchase amount in USD, must be a positive value.",
                example = "150.75")
        @NotNull @Positive
        BigDecimal purchaseAmount
) {}
