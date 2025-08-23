package com.lsgsma.transaction.dto.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TreasuryExchangeRateResponse(

        List<TreasuryExchangeRateDataResponse> data
) {}
