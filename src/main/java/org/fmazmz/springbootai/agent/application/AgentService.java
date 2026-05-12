package org.fmazmz.springbootai.agent.application;

import org.fmazmz.springbootai.agent.domain.Prompt;
import org.fmazmz.springbootai.agent.domain.Agent;
import org.fmazmz.springbootai.agent.dto.AgentResponse;
import org.fmazmz.springbootai.agent.dto.CreateAgentRequest;
import org.fmazmz.springbootai.agent.dto.PatchAgentRequest;
import org.fmazmz.springbootai.agent.repository.AgentRoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AgentService {

    private final AgentRoleRepository agentRoleRepository;

    public AgentService(AgentRoleRepository agentRoleRepository) {
        this.agentRoleRepository = agentRoleRepository;
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> listAgents() {
        return agentRoleRepository.findAll().stream().map(AgentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AgentResponse getAgent(UUID id) {
        return AgentResponse.from(agentRoleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found")));
    }

    @Transactional
    public AgentResponse createAgent(CreateAgentRequest request) {
        Agent role = new Agent();
        role.setName(request.name());
        Prompt prompt = new Prompt();
        prompt.setText(request.promptText() != null ? request.promptText() : "");
        role.setPrompt(prompt);
        return AgentResponse.from(agentRoleRepository.save(role));
    }

    @Transactional
    public AgentResponse replaceAgent(UUID id, CreateAgentRequest request) {
        Agent role = agentRoleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found"));
        role.setName(request.name());
        if (role.getPrompt() == null) {
            role.setPrompt(new Prompt());
        }
        role.getPrompt().setText(request.promptText() != null ? request.promptText() : "");
        return AgentResponse.from(agentRoleRepository.save(role));
    }

    @Transactional
    public AgentResponse patchAgent(UUID id, PatchAgentRequest request) {
        Agent role = agentRoleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found"));
        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
            }
            role.setName(request.name());
        }
        if (request.promptText() != null) {
            if (role.getPrompt() == null) {
                role.setPrompt(new Prompt());
            }
            role.getPrompt().setText(request.promptText());
        }
        return AgentResponse.from(agentRoleRepository.save(role));
    }
}
