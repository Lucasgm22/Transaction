package com.lsgsma.transaction.service;

import com.lsgsma.transaction.dto.request.CreateTransactionRequest;
import com.lsgsma.transaction.dto.response.ConvertedTransactionResponse;
import com.lsgsma.transaction.dto.response.CreateTransactionResponse;
import com.lsgsma.transaction.exception.ExchangeRateNotFoundException;
import com.lsgsma.transaction.exception.TransactionNotFoundException;
import com.lsgsma.transaction.mapper.TransactionMapper;
import com.lsgsma.transaction.model.Transaction;
import com.lsgsma.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Captor
    private ArgumentCaptor<Transaction> transactionArgumentCaptor;

    @Test
    void givenValidTransaction_whenStoreTransaction_thenReturnUuid() {

        var id = UUID.randomUUID();
        var transactionRequest = new CreateTransactionRequest("description"
                , LocalDate.of(2024, 8, 20)
                , BigDecimal.valueOf(150.75));
        var mockedSavedTransaction = new Transaction(id
                , "description"
                , LocalDate.of(2024, 8, 20)
                , BigDecimal.valueOf(150.75));

        when(transactionMapper.toEntity(any())).thenCallRealMethod();
        when(transactionRepository.save(transactionArgumentCaptor.capture())).thenReturn(mockedSavedTransaction);
        var actual = transactionService.storeTransaction(transactionRequest);
        var expected = new CreateTransactionResponse(id);

        var toStoreTransactionValue = transactionArgumentCaptor.getValue();


        verifyNoInteractions(exchangeRateService);
        verify(transactionRepository, only()).save(any());
        assertEquals("description", toStoreTransactionValue.getDescription());
        assertEquals(LocalDate.of(2024, 8, 20), toStoreTransactionValue.getTransactionDate());
        assertEquals(BigDecimal.valueOf(150.75), toStoreTransactionValue.getPurchaseAmount());
        assertEquals(expected, actual);
    }

    @Test
    void givenUUID_whenNoTransactionStored_thenThrowTransactionNotFound() {
        var id = UUID.randomUUID();
        var currency = "Brazil-Real";

        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        var ex = assertThrowsExactly(TransactionNotFoundException.class, () -> transactionService.getConvertedTransaction(id, currency));
        assertEquals("Transaction not found with id: " + id, ex.getMessage());
        verifyNoInteractions(transactionMapper);
        verifyNoInteractions(exchangeRateService);
    }

    @Test
    void givenInvalidCurrency_whenExchangeRateServiceThrowsException_thenPropagateException() {
        var id = UUID.randomUUID();
        var currency = "Invalid-Currency";

        var mockedSavedTransaction = new Transaction(id
                , "description"
                , LocalDate.of(2024, 8, 20)
                , BigDecimal.valueOf(150.75));

        when(transactionRepository.findById(id)).thenReturn(Optional.of(mockedSavedTransaction));
        when(exchangeRateService.getExchangeRate(currency, LocalDate.of(2024, 8, 20)))
                .thenThrow(new ExchangeRateNotFoundException("mocked test"));

        var ex = assertThrowsExactly(ExchangeRateNotFoundException.class, () -> transactionService.getConvertedTransaction(id, currency));

        assertEquals("mocked test", ex.getMessage());
        verifyNoInteractions(transactionMapper);
    }

    @Test
    void givenValidUUIDAndCurrency_whenConvertTransaction_thenCorrectlyConvert() {
        var id = UUID.randomUUID();
        var currency = "Brazil-Real";

        var mockedSavedTransaction = new Transaction(id
                , "description"
                , LocalDate.of(2024, 8, 20)
                , BigDecimal.valueOf(150.75));

        when(transactionRepository.findById(id)).thenReturn(Optional.of(mockedSavedTransaction));
        when(exchangeRateService.getExchangeRate(currency, LocalDate.of(2024, 8, 20)))
                .thenReturn(BigDecimal.valueOf(5.5));
        when(transactionMapper.toConvertedDto(any(), any(), any())).thenCallRealMethod();

        var actual = transactionService.getConvertedTransaction(id, currency);
        var expected = new ConvertedTransactionResponse(id
                , "description"
                , LocalDate.of(2024, 8, 20)
                , BigDecimal.valueOf(150.75)
                , BigDecimal.valueOf(5.5)
                , BigDecimal.valueOf(829.13)
                );

        assertEquals(expected, actual);
    }

}