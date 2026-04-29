package org.fmazmz.springbootai.gateway;

import reactor.core.publisher.Mono;

public interface ModelGateway {
    Mono<String> sendMessage(GatewayRequest request);
}
