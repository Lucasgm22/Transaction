package com.wex.transaction.dto.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TreasuryRateDataResponse(

        @JsonProperty("exchange_rate")
        BigDecimal exchangeRate
) {}
