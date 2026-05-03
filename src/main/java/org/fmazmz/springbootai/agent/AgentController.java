package org.fmazmz.springbootai.agent;

import jakarta.validation.Valid;
import org.fmazmz.springbootai.agent.application.AgentService;
import org.fmazmz.springbootai.agent.dto.AgentResponse;
import org.fmazmz.springbootai.agent.dto.CreateAgentRequest;
import org.fmazmz.springbootai.agent.dto.PatchAgentRequest;
import org.fmazmz.springbootai.common.http.ApiResponseWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public ApiResponseWrapper<List<AgentResponse>> listAgents() {
        return new ApiResponseWrapper<>(agentService.listAgents());
    }

    @GetMapping("/{id}")
    public ApiResponseWrapper<AgentResponse> getAgent(@PathVariable UUID id) {
        return new ApiResponseWrapper<>(agentService.getAgent(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<AgentResponse>> createAgent(
            @Valid @RequestBody CreateAgentRequest request
    ) {
        AgentResponse created = agentService.createAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseWrapper<>(created));
    }

    @PutMapping("/{id}")
    public ApiResponseWrapper<AgentResponse> replaceAgent(
            @PathVariable UUID id,
            @Valid @RequestBody CreateAgentRequest request
    ) {
        return new ApiResponseWrapper<>(agentService.replaceAgent(id, request));
    }

    @PatchMapping("/{id}")
    public ApiResponseWrapper<AgentResponse> patchAgent(
            @PathVariable UUID id,
            @Valid @RequestBody PatchAgentRequest request
    ) {
        return new ApiResponseWrapper<>(agentService.patchAgent(id, request));
    }
}
