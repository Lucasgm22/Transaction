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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final ExchangeRateService exchangeRateService;
    private final TransactionRepository transactionRepository;

    public CreateTransactionResponse storeTransaction(final CreateTransactionRequest request) {
        log.debug("Starting transaction store");
        var newTransaction = transactionMapper.toEntity(request);

        var storedTransaction = transactionRepository.save(newTransaction);
        log.info("Transaction {} successfully stored in database", storedTransaction.getUuid());
        return new CreateTransactionResponse(storedTransaction.getUuid());
    }

    public ConvertedTransactionResponse getConvertedTransaction(final UUID id, final String currency) {
        log.debug("Starting transaction {} conversion process", id);
        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));

        log.debug("Transaction {} found in the database", id);
        var exchangeRate = exchangeRateService.getExchangeRate(currency, transaction.getTransactionDate());

        log.debug("Exchange rate {} found for currency {}", exchangeRate, currency);

        var convertedAmount = transaction.getPurchaseAmount()
                .multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Transaction {} successfully converted  to currency {}. Final Value: {}", id, currency, convertedAmount);

        return transactionMapper.toConvertedDto(transaction, exchangeRate, convertedAmount);
    }
}
