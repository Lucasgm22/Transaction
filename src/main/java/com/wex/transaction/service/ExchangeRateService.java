package com.wex.transaction.service;

import com.wex.transaction.client.TreasuryApiClient;
import com.wex.transaction.dto.client.response.TreasuryRateDataResponse;
import com.wex.transaction.exception.ExchangeRateNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final TreasuryApiClient treasuryApiClient;

    public BigDecimal getExchangeRate(String currency, LocalDate transactionDate) {
        if (StringUtils.isBlank(currency)) {
            return BigDecimal.ONE;
        }

        var sixMonthsAgo = transactionDate.minusMonths(6);

        return treasuryApiClient
                .fetchExchangeRates(currency, sixMonthsAgo, transactionDate)
                .flatMap(response -> response.data().stream().findFirst())
                .map(TreasuryRateDataResponse::exchangeRate)
                .orElseThrow(() -> new ExchangeRateNotFoundException("Could not retrieve exchange rates for " + currency));
    }
}
