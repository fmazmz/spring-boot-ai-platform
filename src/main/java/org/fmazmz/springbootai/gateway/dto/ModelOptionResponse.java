package org.fmazmz.springbootai.gateway.dto;

import java.util.List;
import java.util.UUID;

public record ModelOptionResponse(
        UUID id,
        String slug,
        String promptPrice,
        String completionPrice,
        List<String> supportedParameters
) {
}
