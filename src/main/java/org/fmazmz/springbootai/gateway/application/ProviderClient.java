package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.gateway.domain.ProviderType;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import reactor.core.publisher.Mono;

public interface ProviderClient {
    ProviderType providerType();

    Mono<String> chat(ChatRequest request, String userApiKey);

    Mono<String> fetchModels();
}
