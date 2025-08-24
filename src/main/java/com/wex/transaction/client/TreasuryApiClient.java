package com.wex.transaction.client;

import com.wex.transaction.dto.client.response.TreasuryRateResponse;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TreasuryApiClient {

    private final RestClient treasuryRestClient;

    public Optional<TreasuryRateResponse> fetchExchangeRates(String currency, LocalDate startDate, LocalDate endDate) {
        log.info("Calling TreasuryAPI for currency: '{}' on interval from {} to {}", currency, startDate, endDate);
        try {
            var response = treasuryRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("v1/accounting/od/rates_of_exchange")
                            .queryParam("fields", "exchange_rate,record_date")
                            .queryParam("filter", "country_currency_desc:eq:" + currency + ",record_date:gte:" + startDate + ",record_date:lte:" + endDate)
                            .queryParam("sort", "-record_date")
                            .queryParam("page[size]", 1)
                            .build())
                    .retrieve()
                    .body(TreasuryRateResponse.class);
            log.info("Successfully received the response from the TreasuryAPI for currency '{}'", currency);
            return Optional.ofNullable(response);
        } catch (RestClientException e) {
            log.error("Error calling Treasury API for currency {}: {}", currency, e.getMessage());
            return Optional.empty();
        }
    }
}
