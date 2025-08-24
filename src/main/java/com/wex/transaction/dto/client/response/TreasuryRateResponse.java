package com.wex.transaction.dto.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TreasuryRateResponse(

        List<TreasuryRateDataResponse> data
) {}
