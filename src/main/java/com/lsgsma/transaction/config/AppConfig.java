package com.lsgsma.transaction.config;

import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    private final String treasuryApiBaseUrl;
    private final ObservationRegistry observationRegistry;

    public AppConfig(@Value("${api.treasury.base-url}")
                     final String treasuryApiBaseUrl,
                     final ObservationRegistry observationRegistry) {
            this.treasuryApiBaseUrl = treasuryApiBaseUrl;
            this.observationRegistry = observationRegistry;
    }

    @Bean
    public RestClient treasuryRestClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(treasuryApiBaseUrl)
                .observationRegistry(observationRegistry)
                .requestFactory(requestFactory)
                .defaultHeader("Accept", "application/json")
                .build();
    }
}

