package com.wex.transaction.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConvertedTransactionResponse(

        UUID id,
        String description,
        LocalDate transactionDate,
        BigDecimal originalPurchaseAmount,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount
) {}
