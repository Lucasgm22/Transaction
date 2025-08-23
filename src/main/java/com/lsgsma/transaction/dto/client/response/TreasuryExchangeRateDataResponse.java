package com.lsgsma.transaction.dto.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TreasuryExchangeRateDataResponse(

        @JsonProperty("exchange_rate")
        BigDecimal exchangeRate,

        @JsonProperty("record_date")
        LocalDate recordDate
) {}
