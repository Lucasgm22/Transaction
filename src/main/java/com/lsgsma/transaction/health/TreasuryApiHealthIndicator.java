package com.lsgsma.transaction.health;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TreasuryApiHealthIndicator implements HealthIndicator {

    private final RestClient treasuryRestClient;

    @Override
    public Health health() {
        try {
            treasuryRestClient.get()
                    .uri("/v1/accounting/od/rates_of_exchange", uriBuilder -> uriBuilder
                            .queryParam("page[size]", 1)
                            .build())
                    .retrieve()
                    .toBodilessEntity();

            return Health.up().build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
