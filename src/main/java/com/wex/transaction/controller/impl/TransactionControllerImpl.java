package com.wex.transaction.controller.impl;

import com.wex.transaction.controller.TransactionController;
import com.wex.transaction.dto.request.CreateTransactionRequest;
import com.wex.transaction.dto.response.ConvertedTransactionResponse;
import com.wex.transaction.dto.response.CreateTransactionResponse;
import com.wex.transaction.service.TransactionService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
public class TransactionControllerImpl implements TransactionController {

    private final TransactionService transactionService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTransactionResponse storeTransaction(@Valid CreateTransactionRequest request) {
        return transactionService.storeTransaction(request);
    }

    @Override
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ConvertedTransactionResponse getConvertedTransaction(@PathVariable UUID id, @RequestParam String currency) {
        return new ConvertedTransactionResponse(UUID.randomUUID(),
                "description",
                LocalDate.now(),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE);
    }
}
