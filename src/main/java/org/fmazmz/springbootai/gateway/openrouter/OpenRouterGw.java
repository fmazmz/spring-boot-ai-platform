package org.fmazmz.springbootai.gateway.openrouter;

import org.fmazmz.springbootai.gateway.ModelGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OpenRouterGw implements ModelGateway {
    private final WebClient client;
    private final String apiKey;

    public OpenRouterGw(
            WebClient openRouterClient,
            @Value("${gateway.providers.openrouter.api-key}") String apiKey
    ) {
        this.client = openRouterClient;
        this.apiKey = apiKey;
    }

    @Override
    public Mono<String> sendMessage(ChatRequest request) {
        return client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", request.model(),
                        "messages", request.messages(),
                        "stream", request.stream()
                ))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getModels() {
        return client.get()
                .uri("/models")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(String.class);
    }
}
