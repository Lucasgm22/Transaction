package com.lsgsma.transaction.mapper;

import com.lsgsma.transaction.dto.request.CreateTransactionRequest;
import com.lsgsma.transaction.dto.response.ConvertedTransactionResponse;
import com.lsgsma.transaction.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionMapperTest {

    @InjectMocks
    private TransactionMapper transactionMapper;

    @Test
    void givenNullTransactionRequest_whenMapToTransaction_thenReturnNull() {
        assertNull(transactionMapper.toEntity(null));
    }

    @Test
    void givenNullTransaction_whenConvertToDTO_thenReturnNull() {
        assertNull(transactionMapper.toConvertedDto(null, BigDecimal.ONE, BigDecimal.ONE));
    }

    @Test
    void givenTransactionRequest_whenMapToTransaction_thenShouldSuccessfullyMap() {
        var transactionRequest = new CreateTransactionRequest("description"
                , LocalDate.of(2024, 8, 20)
                , BigDecimal.valueOf(150.745));

        var actualTransaction = transactionMapper.toEntity(transactionRequest);

        assertNull(actualTransaction.getId());
        assertEquals("description", actualTransaction.getDescription());
        assertEquals(LocalDate.of(2024, 8, 20), actualTransaction.getTransactionDate());
        assertEquals(BigDecimal.valueOf(150.75), actualTransaction.getPurchaseAmount());
    }

    @Test
    void givenValidTransactionAndExchangeRateAndConvertedAmount_whenMapToConvertedResponse_thenShouldSuccessfullyMap() {

        var id = UUID.randomUUID();
        var transaction = new Transaction(id
                ,"description"
                , LocalDate.of(2024, 8, 20)
                , BigDecimal.valueOf(150.745));
        var expectedResponse = new ConvertedTransactionResponse(id
                , "description"
                , LocalDate.of(2024, 8, 20)
                , BigDecimal.valueOf(150.745)
                , BigDecimal.valueOf(5.5)
                , BigDecimal.valueOf(829.13)
        );
        var actualResponse = transactionMapper.toConvertedDto(transaction, BigDecimal.valueOf(5.5), BigDecimal.valueOf(829.13));

        assertEquals(expectedResponse, actualResponse);
    }
}
