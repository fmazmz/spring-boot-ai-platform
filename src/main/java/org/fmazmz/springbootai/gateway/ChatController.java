package org.fmazmz.springbootai.gateway;

import io.swagger.v3.oas.annotations.Parameter;
import org.fmazmz.springbootai.gateway.openrouter.OpenRouterGw;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/chat")
public class ChatController {
    private final OpenRouterGw openRouterGw;

    public ChatController(OpenRouterGw openRouterGw) {
        this.openRouterGw = openRouterGw;
    }

    @PostMapping
    public ResponseEntity chat(@RequestBody GatewayRequest request) {
        return ResponseEntity.ok(openRouterGw.sendMessage(request));
    }

    @GetMapping(path = "models")
    public ResponseEntity getAvailableModels(@Parameter String apiKey) {
        return ResponseEntity.ok(openRouterGw.getModels(apiKey));
    }
}
