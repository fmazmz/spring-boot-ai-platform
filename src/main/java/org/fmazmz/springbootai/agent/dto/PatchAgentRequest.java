package org.fmazmz.springbootai.agent.dto;

import jakarta.validation.constraints.Size;

public record PatchAgentRequest(
        @Size(max = 255) String name,
        @Size(max = 65_536) String promptText
) {
}
