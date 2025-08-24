package com.wex.transaction.dto.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TreasureRateDate(

        BigDecimal exchangeRate,
        LocalDate recordDate
) {}
