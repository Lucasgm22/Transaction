package com.lsgsma.transaction.infra;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.lsgsma.transaction.config.AppConfig.EXCHANGE_RATE_CACHE;
import static com.lsgsma.transaction.util.CacheUtil.buildCacheKey;

@Component
@Slf4j
public class CacheWarmingService {

    private final Cache exchangeRateCache;

    public CacheWarmingService(CacheManager cacheManager) {
        this.exchangeRateCache = cacheManager.getCache(EXCHANGE_RATE_CACHE);
    }


    @Async
    public void warmExchangeRateCache(final String currency, final LocalDate transactionDate, final LocalDate recordDate, final BigDecimal value) {
        log.debug("Starting async cache warming for currency '{}'", currency);
        for (var dateToCache = recordDate; !dateToCache.isAfter(transactionDate); dateToCache = dateToCache.plusDays(1)) {
            log.debug("Caching {}::{}, with value {}", currency, dateToCache, value);
            exchangeRateCache.putIfAbsent(buildCacheKey(currency, dateToCache), value);
        }
        log.debug("Finished async cache warming for currency '{}'", currency);
    }
}
