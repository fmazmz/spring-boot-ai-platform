package org.fmazmz.springbootai.gateway.dto;

import org.fmazmz.springbootai.gateway.domain.Message;

import java.util.List;
import java.util.UUID;

public record ChatRequest(
        UUID sessionId,
        String model,
        List<Message> messages,
        boolean stream
) {
}
