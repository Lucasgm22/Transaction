package com.lsgsma.transaction.service;

import com.lsgsma.transaction.client.TreasuryApiClient;
import com.lsgsma.transaction.exception.ExchangeRateNotFoundException;
import com.lsgsma.transaction.infra.CacheWarmingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import static com.lsgsma.transaction.config.AppConfig.EXCHANGE_RATE_CACHE;
import static com.lsgsma.transaction.util.CacheUtil.buildCacheKey;
import static java.util.Objects.nonNull;

@Service
@Slf4j
public class ExchangeRateService {

    private final TreasuryApiClient treasuryApiClient;
    private final Cache exchangeRateCache;
    private final CacheWarmingService cacheWarmingService;

    public ExchangeRateService(TreasuryApiClient treasuryApiClient, CacheManager cacheManager, CacheWarmingService cacheWarmingService) {
        this.treasuryApiClient = treasuryApiClient;
        this.exchangeRateCache = cacheManager.getCache(EXCHANGE_RATE_CACHE);
        this.cacheWarmingService = cacheWarmingService;
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
                    var recordDate = treasuryRateDataResponse.recordDate();
                    var exchangeRate = treasuryRateDataResponse.exchangeRate();
                    log.info("Using exchange rate {} from {}", exchangeRate, recordDate);
                    cacheWarmingService.warmExchangeRateCache(currency, transactionDate, recordDate, exchangeRate);
                    return treasuryRateDataResponse.exchangeRate();
                })
                .orElseThrow(() -> new ExchangeRateNotFoundException("Could not retrieve exchange rates for " + currency));
    }
}
