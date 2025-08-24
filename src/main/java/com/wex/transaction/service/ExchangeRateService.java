package com.wex.transaction.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ExchangeRateService {

    public BigDecimal getExchangeRate(String currency, LocalDate transactionDate) {
        if (StringUtils.isBlank(currency)) {
            return BigDecimal.ONE;
        }

        return BigDecimal.TWO;
    }
}
