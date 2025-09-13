package com.lsgsma.transaction.service;

import com.lsgsma.transaction.client.TreasuryApiClient;
import com.lsgsma.transaction.exception.ExchangeRateNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class ExchangeRateService {

    private static final String EXCHANGE_RATE_CACHE = "exchange-rate-cache";

    private final TreasuryApiClient treasuryApiClient;
    private final Cache exchangeRateCache;

    public ExchangeRateService(TreasuryApiClient treasuryApiClient, CacheManager cacheManager) {
        this.treasuryApiClient = treasuryApiClient;
        this.exchangeRateCache = cacheManager.getCache(EXCHANGE_RATE_CACHE);
    }

    public BigDecimal getExchangeRate(final String currency, final LocalDate transactionDate) {
        log.debug("Starting fetch for exchange rate");

        if (StringUtils.isBlank(currency)) {
            log.warn("No currency given, no conversion applied");
            return BigDecimal.ONE;
        }

        var value = exchangeRateCache.get(buildCacheKey(currency, transactionDate), BigDecimal.class);
        if (value != null) {
            return value;
        }

        var sixMonthsEarlier = transactionDate.minusMonths(6);

        return treasuryApiClient
                .getTopExchangeRateByCurrencyInRecordDateRangeSortedByRecordDateDesc(currency, sixMonthsEarlier, transactionDate)
                .filter(treasuryRateResponse -> nonNull(treasuryRateResponse.data()))
                .flatMap(response -> response.data().stream().findFirst())
                .map(treasuryRateDataResponse -> {
                    log.info("Using exchange rate {} from {}", treasuryRateDataResponse.exchangeRate(), treasuryRateDataResponse.recordDate());
                    cacheWarmGetExchangeRateCache(currency, transactionDate, treasuryRateDataResponse.recordDate(), treasuryRateDataResponse.exchangeRate());
                    return treasuryRateDataResponse.exchangeRate();
                })
                .orElseThrow(() -> new ExchangeRateNotFoundException("Could not retrieve exchange rates for " + currency));
    }

    private void cacheWarmGetExchangeRateCache(final String currency, final LocalDate transactionDate, final LocalDate recordDate, final BigDecimal value) {
        for (var dateToCache = recordDate; !dateToCache.isAfter(transactionDate); dateToCache = dateToCache.plusDays(1)) {
            exchangeRateCache.put(buildCacheKey(currency, dateToCache), value);
        }
    }

    private String buildCacheKey(Object... parts) {
        return Arrays.stream(parts)
                .map(Object::toString)
                .collect(Collectors.joining("::"));
    }
}
