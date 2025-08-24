package com.wex.transaction.dto.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TreasureResponse(

        List<TreasureRateDate> data
) {}
