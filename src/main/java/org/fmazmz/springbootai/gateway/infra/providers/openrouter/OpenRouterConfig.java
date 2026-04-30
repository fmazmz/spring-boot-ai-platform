package org.fmazmz.springbootai.gateway.infra.providers.openrouter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenRouterConfig {

    // 10 MB
    private static final int MAX_IN_MEM_SIZE = 10 * 1024 * 1024;

    @Value("${gateway.providers.openrouter.base-url}")
    private String baseUrl;

    @Bean
    public WebClient openRouterWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer ->
                        configurer.defaultCodecs()
                                .maxInMemorySize(MAX_IN_MEM_SIZE)
                )
                .build();
    }
}
