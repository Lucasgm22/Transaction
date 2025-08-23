package com.wex.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(

        @NotBlank @Size(max = 50)
        String description,

        @NotNull @PastOrPresent
        LocalDate transactionDate,

        @NotNull @Positive
        BigDecimal purchaseAmount
) {}
