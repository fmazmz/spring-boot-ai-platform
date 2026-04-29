package org.fmazmz.springbootai.gateway;

import java.util.List;

public record GatewayRequest(
        String apiKey,
        String model,
        List<Message> messages,
        boolean stream
) {
}
