package com.lsgsma.transaction.infra;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheWarmingServiceTest {

    private CacheWarmingService cacheWarmingService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache exchangeRateCache;

    @BeforeEach
    void setup() {
        when(cacheManager.getCache(anyString())).thenReturn(exchangeRateCache);

        cacheWarmingService = new CacheWarmingService(cacheManager);
    }

    @Test
    void givenInputs_whenCallToWarmExchangeRateCache_thenWarmTheCacheProperly() {
        var date = LocalDate.of(2024, 8, 20);
        var currency = "Brazil-Real";
        var recordDate = LocalDate.of(2024, 6, 20);

        cacheWarmingService.warmExchangeRateCache(currency, date, recordDate, BigDecimal.valueOf(5.5));

        for (var dateToCache = recordDate; !dateToCache.isAfter(date); dateToCache = dateToCache.plusDays(1)) {
            verify(exchangeRateCache).putIfAbsent(currency + "::" + dateToCache, BigDecimal.valueOf(5.5));
        }
        verifyNoMoreInteractions(exchangeRateCache);
    }
}