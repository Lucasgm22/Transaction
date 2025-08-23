package com.wex.transaction.service;

import com.wex.transaction.dto.request.CreateTransactionRequest;
import com.wex.transaction.dto.response.CreateTransactionResponse;
import com.wex.transaction.mapper.TransactionMapper;
import com.wex.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;

    public CreateTransactionResponse storeTransaction(CreateTransactionRequest request) {

        var newTransaction = transactionMapper.toEntity(request);

        var storedTransaction = transactionRepository.save(newTransaction);

        return new CreateTransactionResponse(storedTransaction.getUuid());
    }
}
