package com.wex.transaction.service;

import com.wex.transaction.client.TreasuryApiClient;
import com.wex.transaction.exception.ExchangeRateNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateService {

    private final TreasuryApiClient treasuryApiClient;

    public BigDecimal getExchangeRate(String currency, LocalDate transactionDate) {
        log.debug("Starting fetch for exchange rate");

        if (StringUtils.isBlank(currency)) {
            log.warn("No currency given, no conversion applied");
            return BigDecimal.ONE;
        }

        var sixMonthsAgo = transactionDate.minusMonths(6);

        return treasuryApiClient
                .fetchExchangeRates(currency, sixMonthsAgo, transactionDate)
                .filter(treasuryRateResponse -> nonNull(treasuryRateResponse.data()))
                .flatMap(response -> response.data().stream().findAny())
                .map(treasuryRateDataResponse -> {
                    log.info("Using exchange rate {} from {}", treasuryRateDataResponse.exchangeRate(), treasuryRateDataResponse.recordDate());
                    return treasuryRateDataResponse.exchangeRate();
                })
                .orElseThrow(() -> new ExchangeRateNotFoundException("Could not retrieve exchange rates for " + currency));
    }
}
