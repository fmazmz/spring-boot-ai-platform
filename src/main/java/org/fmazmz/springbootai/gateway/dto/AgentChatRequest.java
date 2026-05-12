package org.fmazmz.springbootai.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.fmazmz.springbootai.agent.domain.AgentRole;

public record AgentChatRequest(
        @NotNull
        @Schema(
                description = "Agent role: exact `AgentRole` enum name (matches persisted `Agent.name`, e.g. after seeding).",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "BACKEND_DEVELOPER"
        )
        AgentRole agent,
        @NotBlank @Size(max = 8000) String message,
        @Size(max = 256) String sessionId
) {
}
