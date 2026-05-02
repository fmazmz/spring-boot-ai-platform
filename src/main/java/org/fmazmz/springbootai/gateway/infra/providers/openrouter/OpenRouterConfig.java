package org.fmazmz.springbootai.gateway.infra.providers.openrouter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenRouterConfig {
    @Value("${gateway.providers.openrouter.base-url}")
    private String baseUrl;

    @Bean
    public RestClient openRouterRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
