package org.fmazmz.springbootai.gateway.infra.providers.openrouter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
public class OpenRouterResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractAssistantContent(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Provider response missing choices");
            }
            JsonNode content = choices.get(0).path("message").path("content");
            if (content.isMissingNode() || !content.isTextual()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Provider response missing assistant content");
            }
            return content.asText();
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Could not parse provider JSON response", ex);
        }
    }
}
