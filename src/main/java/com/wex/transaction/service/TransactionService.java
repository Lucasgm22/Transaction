package com.wex.transaction.service;

import com.wex.transaction.dto.request.CreateTransactionRequest;
import com.wex.transaction.dto.response.ConvertedTransactionResponse;
import com.wex.transaction.dto.response.CreateTransactionResponse;
import com.wex.transaction.exception.TransactionNotFoundException;
import com.wex.transaction.mapper.TransactionMapper;
import com.wex.transaction.repository.TransactionRepository;
import java.math.RoundingMode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final ExchangeRateService exchangeRateService;
    private final TransactionRepository transactionRepository;

    public CreateTransactionResponse storeTransaction(CreateTransactionRequest request) {

        var newTransaction = transactionMapper.toEntity(request);

        var storedTransaction = transactionRepository.save(newTransaction);

        return new CreateTransactionResponse(storedTransaction.getUuid());
    }

    public ConvertedTransactionResponse getConvertedTransaction(UUID id, String currency) {
        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));

        var exchangeRate = exchangeRateService.getExchangeRate(currency, transaction.getTransactionDate());

        var convertedAmount = transaction.getPurchaseAmount()
                .multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);


        return transactionMapper.toConvertedDto(transaction, exchangeRate, convertedAmount);
    }
}
