package org.fmazmz.springbootai.gateway.openrouter;

import org.fmazmz.springbootai.gateway.GatewayRequest;
import org.fmazmz.springbootai.gateway.ModelGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OpenRouterGw implements ModelGateway {
    private final WebClient client;

    public OpenRouterGw(WebClient openRouterClient) {
        this.client = openRouterClient;
    }

    @Override
    public Mono<String> sendMessage(GatewayRequest request) {
        return client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + request.apiKey())
                .bodyValue(Map.of(
                        "model", request.model(),
                        "messages", request.messages(),
                        "stream", request.stream()
                ))
                .retrieve()
                .bodyToMono(String.class);
    }
}
