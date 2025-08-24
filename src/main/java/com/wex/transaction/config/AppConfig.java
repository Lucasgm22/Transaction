package com.wex.transaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    private final String treasuryApiBaseUrl;

    public AppConfig(
            @Value("${api.treasury.base-url}")
            String treasuryApiBaseUrl) {
            this.treasuryApiBaseUrl = treasuryApiBaseUrl;
    }

    @Bean
    public RestClient treasuryRestClient() {
        return RestClient.builder()
                .baseUrl(treasuryApiBaseUrl)
                .build();
    }
}

