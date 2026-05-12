package org.fmazmz.springbootai.gateway.infra.providers.openrouter;

import org.fmazmz.springbootai.gateway.application.ProviderClient;
import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenRouterClient implements ProviderClient {
    private final RestClient client;
    private final OpenRouterCompletionInvoker completionInvoker;
    private final String internalApiKey;

    public OpenRouterClient(
            @Qualifier("openRouterRestClient") RestClient openRouterClient,
            OpenRouterCompletionInvoker completionInvoker,
            @Value("${gateway.providers.openrouter.api-key}") String apiKey
    ) {
        this.client = openRouterClient;
        this.completionInvoker = completionInvoker;
        this.internalApiKey = apiKey;
    }

    @Override
    public LlmProvider providerType() {
        return LlmProvider.OPENROUTER;
    }

    @Override
    public Mono<String> chat(ChatRequest request, String userApiKey) {
        Map<String, Object> body = new HashMap<>(3);
        body.put("model", request.model());
        body.put("messages", request.messages());
        body.put("stream", request.stream());

        return Mono.fromCallable(() -> completionInvoker.postChatCompletions(body, userApiKey))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> fetchModels() {
        return Mono.fromCallable(() -> client.get()
                        .uri("/models")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + internalApiKey)
                        .retrieve()
                        .body(String.class))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
