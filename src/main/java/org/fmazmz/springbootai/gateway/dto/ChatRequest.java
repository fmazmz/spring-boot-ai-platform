package org.fmazmz.springbootai.gateway.dto;

import org.fmazmz.springbootai.gateway.domain.Message;

import java.util.List;

public record ChatRequest(
        String model,
        List<Message> messages,
        boolean stream
) {
}
