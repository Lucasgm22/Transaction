package com.wex.transaction.mapper;

import com.wex.transaction.dto.request.CreateTransactionRequest;
import com.wex.transaction.dto.response.ConvertedTransactionResponse;
import com.wex.transaction.model.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(CreateTransactionRequest request) {
        if (request == null) {
            return null;
        }

        var transaction = new Transaction();
        transaction.setDescription(request.description());
        transaction.setTransactionDate(request.transactionDate());

        var roundedAmount = request.purchaseAmount().setScale(2, RoundingMode.HALF_UP);
        transaction.setPurchaseAmount(roundedAmount);

        return transaction;
    }

    public ConvertedTransactionResponse toConvertedDto(Transaction transaction, BigDecimal exchangeRate, BigDecimal convertedAmount) {
        return new ConvertedTransactionResponse(
                transaction.getUuid(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getPurchaseAmount(),
                exchangeRate,
                convertedAmount
        );
    }
}
