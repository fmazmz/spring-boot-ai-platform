package org.fmazmz.springbootai.gateway.infra.providers.openrouter;

import org.fmazmz.springbootai.gateway.application.ProviderClient;
import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OpenRouterClient implements ProviderClient {
    private final WebClient client;
    private final String internalApiKey;

    public OpenRouterClient(
            @Qualifier("openRouterWebClient") WebClient openRouterClient,
            @Value("${gateway.providers.openrouter.api-key}") String apiKey
    ) {
        this.client = openRouterClient;
        this.internalApiKey = apiKey;
    }

    @Override
    public LlmProvider providerType() {
        return LlmProvider.OPENROUTER;
    }

    @Override
    public Mono<String> chat(ChatRequest request, String userApiKey) {
        return client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + userApiKey)
                .bodyValue(Map.of(
                        "model", request.model(),
                        "messages", request.messages(),
                        "stream", request.stream()
                ))
                .retrieve()
                .bodyToMono(String.class);
    }

    @Override
    public Mono<String> fetchModels() {
        return client.get()
                .uri("/models")
                .header("Authorization", "Bearer " + internalApiKey)
                .retrieve()
                .bodyToMono(String.class);
    }
}
