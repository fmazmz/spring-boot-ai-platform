package org.fmazmz.springbootai.agent.dto;

import org.fmazmz.springbootai.agent.domain.Agent;

import java.util.UUID;

public record AgentResponse(UUID id, String name, String promptText) {

    public static AgentResponse from(Agent role) {
        String text = role.getPrompt() != null ? role.getPrompt().getText() : null;
        return new AgentResponse(role.getId(), role.getName(), text);
    }
}
