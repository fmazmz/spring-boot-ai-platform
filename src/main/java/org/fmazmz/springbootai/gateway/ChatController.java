package org.fmazmz.springbootai.gateway;

import org.fmazmz.springbootai.gateway.openrouter.OpenRouterGw;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
