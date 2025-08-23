package com.lsgsma.transaction.controller.impl;

import com.lsgsma.transaction.controller.TransactionController;
import com.lsgsma.transaction.dto.request.CreateTransactionRequest;
import com.lsgsma.transaction.dto.response.ConvertedTransactionResponse;
import com.lsgsma.transaction.dto.response.CreateTransactionResponse;
import com.lsgsma.transaction.service.TransactionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/transaction")
public class TransactionControllerImpl implements TransactionController {

    private final TransactionService transactionService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTransactionResponse storeTransaction(final CreateTransactionRequest request) {
        log.info("Received request to store transaction with description: '{}'", request.description());
        return transactionService.storeTransaction(request);
    }

    @Override
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ConvertedTransactionResponse getConvertedTransaction(@PathVariable final UUID id, final String currency) {
        log.info("Received request to convert transaction ID {} to currency {}", id, StringUtils.isNotBlank(currency) ? currency : "United States-Dollar" );
        return transactionService.getConvertedTransaction(id, currency);
    }
}
