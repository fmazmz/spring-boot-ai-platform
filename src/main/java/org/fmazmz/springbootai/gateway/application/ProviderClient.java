package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import reactor.core.publisher.Mono;

public interface ProviderClient {
    LlmProvider providerType();

    Mono<String> chat(ChatRequest request, String userApiKey);

    Mono<String> fetchModels();
}
