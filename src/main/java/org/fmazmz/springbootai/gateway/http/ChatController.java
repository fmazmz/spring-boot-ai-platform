package org.fmazmz.springbootai.gateway.http;

import org.fmazmz.springbootai.gateway.application.ChatService;
import org.fmazmz.springbootai.gateway.application.ModelCatalogService;
import org.fmazmz.springbootai.gateway.dto.ModelOptionResponse;
import org.fmazmz.springbootai.gateway.domain.ProviderType;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/chat")
public class ChatController {
    private final ChatService chatService;
    private final ModelCatalogService modelCatalogService;

    public ChatController(
            ChatService chatService,
            ModelCatalogService modelCatalogService
    ) {
        this.chatService = chatService;
        this.modelCatalogService = modelCatalogService;
    }

    @PostMapping
    public ResponseEntity<String> chat(
            @RequestParam(defaultValue = "OPENROUTER") ProviderType provider,
            @RequestHeader(name = "X-Provider-Api-Key", required = false) String userApiKey,
            @RequestBody ChatRequest request
    ) {
        if (userApiKey == null || userApiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing required header: X-Provider-Api-Key");
        }
        return ResponseEntity.ok(chatService.sendMessage(provider, request, userApiKey));
    }

    @GetMapping(path = "models")
    public ResponseEntity<List<ModelOptionResponse>> getAvailableModels(
            @RequestParam(defaultValue = "OPENROUTER") ProviderType provider
    ) {
        return ResponseEntity.ok(modelCatalogService.getModels(provider));
    }

    @PostMapping(path = "models/sync")
    public ResponseEntity<List<ModelOptionResponse>> syncAvailableModels(
            @RequestParam(defaultValue = "OPENROUTER") ProviderType provider
    ) {
        return ResponseEntity.ok(modelCatalogService.syncModels(provider));
    }
}
