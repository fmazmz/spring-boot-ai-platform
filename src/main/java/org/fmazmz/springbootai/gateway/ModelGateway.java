package org.fmazmz.springbootai.gateway;

import org.fmazmz.springbootai.gateway.openrouter.ChatRequest;
import reactor.core.publisher.Mono;

public interface ModelGateway {
    Mono<String> sendMessage(ChatRequest request);
}
