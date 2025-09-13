package com.lsgsma.transaction.service;

import com.lsgsma.transaction.client.TreasuryApiClient;
import com.lsgsma.transaction.dto.client.response.TreasuryExchangeRateDataResponse;
import com.lsgsma.transaction.dto.client.response.TreasuryExchangeRateResponse;
import com.lsgsma.transaction.exception.ExchangeRateNotFoundException;
import com.lsgsma.transaction.infra.CacheWarmingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {


    private ExchangeRateService exchangeRateService;

    @Mock
    private TreasuryApiClient treasuryApiClient;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache exchangeRateCache;

    @Mock
    private CacheWarmingService cacheWarmingService;

    @BeforeEach
    void setup() {

        when(cacheManager.getCache(anyString())).thenReturn(exchangeRateCache);

        exchangeRateService = new ExchangeRateService(treasuryApiClient, cacheManager, cacheWarmingService);
    }

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
    void givenExchangeRateCached_whenGetExchangeRate_theReturnImediatly() {
        var date = LocalDate.of(2024, 8, 20);
        var currency = "Brazil-Real";

        when(exchangeRateCache.get(currency + "::" + date, BigDecimal.class)).thenReturn(BigDecimal.valueOf(5.5));
        var actualExchangeRate = exchangeRateService.getExchangeRate(currency, date);

        verifyNoInteractions(treasuryApiClient);
        verifyNoMoreInteractions(exchangeRateCache);
        assertEquals(BigDecimal.valueOf(5.5), actualExchangeRate);
    }

    @Test
    void givenValidCurrencyAndDate_whenTreasuryApiReturnsValid_thenCorrectlyReturnExchangeRateAndCacheValues() {
        var date = LocalDate.of(2024, 8, 20);
        var sixMonthsAgo = date.minusMonths(6);
        var currency = "Brazil-Real";
        var recordDate = LocalDate.of(2024, 6, 20);

        var treasuryApiResponse = new TreasuryExchangeRateResponse(
                List.of(new TreasuryExchangeRateDataResponse(BigDecimal.valueOf(5.5), recordDate))
        );

        when(treasuryApiClient.getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date))
                .thenReturn(Optional.of(treasuryApiResponse));

        var actualExchangeRate = exchangeRateService.getExchangeRate(currency, date);

        verify(exchangeRateCache).get(currency + "::" + date, BigDecimal.class);
        assertEquals(BigDecimal.valueOf(5.5), actualExchangeRate);

        verify(cacheWarmingService, only()).warmExchangeRateCache(currency, date, recordDate, BigDecimal.valueOf(5.5));
        verifyNoMoreInteractions(exchangeRateCache);
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

        verify(exchangeRateCache).get(currency + "::" + date, BigDecimal.class);
        verifyNoMoreInteractions(exchangeRateCache);

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

        verify(exchangeRateCache).get(currency + "::" + date, BigDecimal.class);
        verifyNoMoreInteractions(exchangeRateCache);

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

        verify(exchangeRateCache).get(currency + "::" + date, BigDecimal.class);
        verifyNoMoreInteractions(exchangeRateCache);

        assertEquals("Could not retrieve exchange rates for " + currency, ex.getMessage());
        verify(treasuryApiClient, only()).getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsAgo, date);
    }

}