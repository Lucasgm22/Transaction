package com.lsgsma.transaction.service;

import com.lsgsma.transaction.client.TreasuryApiClient;
import com.lsgsma.transaction.dto.client.response.TreasuryExchangeRateDataResponse;
import com.lsgsma.transaction.dto.client.response.TreasuryExchangeRateResponse;
import com.lsgsma.transaction.exception.ExchangeRateNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {


    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Mock
    private TreasuryApiClient treasuryApiClient;

    @Test
    void givenNullCurrency_whenGetExchangeRate_thenReturnOne() {
        var actualExchangeRate= exchangeRateService.getExchangeRate(null, LocalDate.of(2024, 8, 20));
        assertEquals(BigDecimal.ONE, actualExchangeRate);
        verifyNoInteractions(treasuryApiClient);
    }

    @Test
    void givenEmptyCurrency_whenGetExchangeRate_thenReturnOne() {
        var actualExchangeRate= exchangeRateService.getExchangeRate("", LocalDate.of(2024, 8, 20));
        assertEquals(BigDecimal.ONE, actualExchangeRate);
        verifyNoInteractions(treasuryApiClient);
    }

    @Test
    void givenValidCurrencyAndDate_whenTreasuryApiReturnsValid_thenCorrectlyReturnExchangeRate() {
        var date = LocalDate.of(2024, 8, 20);
        var sixMonthsAgo = date.minusMonths(6);
        var currency = "Brazil-Real";

        var treasuryApiResponse = new TreasuryExchangeRateResponse(
                List.of(new TreasuryExchangeRateDataResponse(BigDecimal.valueOf(5.5), LocalDate.of(2024, 6, 20)))
        );

        when(treasuryApiClient.getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date))
                .thenReturn(Optional.of(treasuryApiResponse));

        var actualExchangeRate = exchangeRateService.getExchangeRate(currency, date);

        assertEquals(BigDecimal.valueOf(5.5), actualExchangeRate);
        verify(treasuryApiClient, only()).getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date);
    }

    @Test
    void givenValidCurrencyAndDate_whenTreasuryApiReturnsNoData_thenThrowExchangeRateNotFoundException() {
        var date = LocalDate.of(2024, 8, 20);
        var sixMonthsAgo = date.minusMonths(6);
        var currency = "Brazil-Real";

        var treasuryApiResponse = new TreasuryExchangeRateResponse(new ArrayList<>());

        when(treasuryApiClient.getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date))
                .thenReturn(Optional.of(treasuryApiResponse));

        var ex = assertThrowsExactly(ExchangeRateNotFoundException.class, () -> exchangeRateService.getExchangeRate(currency, date));

        assertEquals("Could not retrieve exchange rates for " + currency, ex.getMessage());
        verify(treasuryApiClient, only()).getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date);
    }

    @Test
    void givenValidCurrencyAndDate_whenTreasuryApiReturnsNullData_thenThrowExchangeRateNotFoundException() {
        var date = LocalDate.of(2024, 8, 20);
        var sixMonthsAgo = date.minusMonths(6);
        var currency = "Brazil-Real";

        var treasuryApiResponse = new TreasuryExchangeRateResponse(null);

        when(treasuryApiClient.getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date))
                .thenReturn(Optional.of(treasuryApiResponse));

        var ex = assertThrowsExactly(ExchangeRateNotFoundException.class, () -> exchangeRateService.getExchangeRate(currency, date));

        assertEquals("Could not retrieve exchange rates for " + currency, ex.getMessage());
        verify(treasuryApiClient, only()).getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date);
    }

    @Test
    void givenValidCurrencyAndDate_whenTreasuryApiDoesNotReturn_thenThrowExchangeRateNotFoundException() {
        var date = LocalDate.of(2024, 8, 20);
        var sixMonthsAgo = date.minusMonths(6);
        var currency = "Brazil-Real";


        when(treasuryApiClient.getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date))
                .thenReturn(Optional.empty());

        var ex = assertThrowsExactly(ExchangeRateNotFoundException.class, () -> exchangeRateService.getExchangeRate(currency, date));

        assertEquals("Could not retrieve exchange rates for " + currency, ex.getMessage());
        verify(treasuryApiClient, only()).getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date);
    }

}