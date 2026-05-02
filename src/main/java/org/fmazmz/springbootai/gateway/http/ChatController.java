package org.fmazmz.springbootai.gateway.http;

import io.swagger.v3.oas.annotations.Parameter;
import org.fmazmz.springbootai.common.http.ApiResponseWrapper;
import org.fmazmz.springbootai.common.http.PagedResult;
import org.fmazmz.springbootai.gateway.application.ChatService;
import org.fmazmz.springbootai.gateway.application.ModelCatalogService;
import org.fmazmz.springbootai.gateway.dto.ModelOptionResponse;
import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import org.fmazmz.springbootai.user.domain.User;
import org.fmazmz.springbootai.user.http.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponseWrapper<String>> chat(
            @RequestParam(defaultValue = "OPENROUTER") LlmProvider provider,
            @RequestHeader(name = "X-Provider-Api-Key", required = false) String userApiKey,
            @Parameter(hidden = true)
            @CurrentUser User currentUser,
            @RequestBody ChatRequest request
    ) {
        if (userApiKey == null || userApiKey.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseWrapper<>("Missing required header: X-Provider-Api-Key"));
        }
        return ResponseEntity.ok(new ApiResponseWrapper<>(chatService.sendMessage(provider, request, userApiKey, currentUser)));
    }

    @GetMapping(path = "models")
    public ResponseEntity<ApiResponseWrapper<PagedResult<ModelOptionResponse>>> getAvailableModels(
            @RequestParam(defaultValue = "OPENROUTER") LlmProvider provider,
            @RequestParam(required = false) String search,
            @Parameter(hidden = true)
            Pageable pageable
    ) {
        Page<ModelOptionResponse> modelsPage = modelCatalogService.getModels(provider, search, pageable);
        return ResponseEntity.ok(new ApiResponseWrapper<>(PagedResult.from(modelsPage)));
    }

    @PostMapping(path = "models/sync")
    public ResponseEntity<ApiResponseWrapper<java.util.List<ModelOptionResponse>>> syncAvailableModels(
            @RequestParam(defaultValue = "OPENROUTER") LlmProvider provider,
            @Parameter(hidden = true)
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(new ApiResponseWrapper<>(modelCatalogService.syncModels(provider)));
    }
}
