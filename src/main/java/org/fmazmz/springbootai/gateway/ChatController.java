package org.fmazmz.springbootai.gateway;

import io.swagger.v3.oas.annotations.Parameter;
import org.fmazmz.springbootai.gateway.domain.Model;
import org.fmazmz.springbootai.gateway.openrouter.ChatRequest;
import org.fmazmz.springbootai.gateway.openrouter.ModelMapper;
import org.fmazmz.springbootai.gateway.openrouter.OpenRouterGw;
import org.fmazmz.springbootai.gateway.repository.ModelRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/chat")
public class ChatController {
    private final OpenRouterGw openRouterGw;
    private final ModelMapper modelMapper;
    private final ModelRepository modelRepository;

    public ChatController(
            OpenRouterGw openRouterGw,
            ModelMapper modelMapper,
            ModelRepository modelRepository
    ) {
        this.openRouterGw = openRouterGw;
        this.modelMapper = modelMapper;
        this.modelRepository = modelRepository;
    }

    @PostMapping
    public ResponseEntity chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(openRouterGw.sendMessage(request));
    }

    @GetMapping(path = "models")
    public ResponseEntity<List<ModelOptionResponse>> getAvailableModels() {
        List<ModelOptionResponse> options = modelRepository.findAllByOrderBySlugAsc()
                .stream()
                .map(this::toOption)
                .toList();
        return ResponseEntity.ok(options);
    }

    @PostMapping(path = "models/sync")
    public ResponseEntity<List<ModelOptionResponse>> syncAvailableModels() {
        List<ModelOptionResponse> options = modelMapper.syncOpenRouterModels()
                .stream()
                .map(this::toOption)
                .toList();
        return ResponseEntity.ok(options);
    }

    private ModelOptionResponse toOption(Model model) {
        return new ModelOptionResponse(
                model.getId(),
                model.getSlug(),
                model.getPricing() != null ? model.getPricing().getPrompt() : null,
                model.getPricing() != null ? model.getPricing().getCompletion() : null,
                model.getSupportedParameters() != null ? model.getSupportedParameters() : List.of()
        );
    }
}
