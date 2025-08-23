package com.lsgsma.transaction.mapper;

import com.lsgsma.transaction.dto.request.CreateTransactionRequest;
import com.lsgsma.transaction.dto.response.ConvertedTransactionResponse;
import com.lsgsma.transaction.model.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(final CreateTransactionRequest request) {
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

    public ConvertedTransactionResponse toConvertedDto(final Transaction transaction, final BigDecimal exchangeRate, final BigDecimal convertedAmount) {
        if (transaction == null) {
            return null;
        }

        return new ConvertedTransactionResponse(
                transaction.getId(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getPurchaseAmount(),
                exchangeRate,
                convertedAmount
        );
    }
}
