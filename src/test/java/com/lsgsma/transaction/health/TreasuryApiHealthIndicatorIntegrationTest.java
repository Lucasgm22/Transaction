package com.lsgsma.transaction.health;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TreasuryApiHealthIndicatorIntegrationTest {

    @Autowired
    private TreasuryApiHealthIndicator treasuryApiHealthIndicator;

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("api.treasury.base-url", wireMockServer::baseUrl);
    }

    @Test
    void givenTreasuryApiIsHealthy_whenHealthIsChecked_thenStatusIsUp() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/accounting/od/rates_of_exchange.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\":[]}")));

        var health = treasuryApiHealthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void givenTreasuryApiIsDown_whenHealthIsChecked_thenStatusIsDown() {
        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/accounting/od/rates_of_exchange.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"NOT FOUND\"}")));

        var health = treasuryApiHealthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}