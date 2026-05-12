package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.agent.domain.Agent;
import org.fmazmz.springbootai.agent.repository.AgentRoleRepository;
import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.domain.Message;
import org.fmazmz.springbootai.gateway.dto.AgentChatRequest;
import org.fmazmz.springbootai.gateway.dto.AgentChatResponse;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import org.fmazmz.springbootai.gateway.infra.providers.openrouter.OpenRouterResponseParser;
import org.fmazmz.springbootai.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgentChatService {

    private final ProviderClientFactory providerClientFactory;
    private final InMemoryChatSessionStore sessionStore;
    private final OpenRouterResponseParser responseParser;
    private final AgentRoleRepository agentRoleRepository;

    @Value("${gateway.providers.openrouter.default-model}")
    private String defaultModel;

    public AgentChatService(
            ProviderClientFactory providerClientFactory,
            InMemoryChatSessionStore sessionStore,
            OpenRouterResponseParser responseParser,
            AgentRoleRepository agentRoleRepository
    ) {
        this.providerClientFactory = providerClientFactory;
        this.sessionStore = sessionStore;
        this.responseParser = responseParser;
        this.agentRoleRepository = agentRoleRepository;
    }

    public AgentChatResponse chat(
            LlmProvider providerType,
            AgentChatRequest request,
            String userApiKey,
            User currentUser
    ) {
        String agentName = request.agent().name();
        Agent agent = agentRoleRepository.findByName(agentName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unknown agent: " + agentName
                                + ". Expected a seeded agent row for this enum name."));
        String systemPrompt = agent.getPrompt() != null && agent.getPrompt().getText() != null
                ? agent.getPrompt().getText().strip()
                : "";
        if (systemPrompt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Agent " + agentName + " has no system prompt configured");
        }

        String sessionId = (request.sessionId() == null || request.sessionId().isBlank())
                ? UUID.randomUUID().toString()
                : request.sessionId().trim();
        String storeKey = currentUser.getId() + "|" + sessionId;

        List<Message> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt));
        for (InMemoryChatSessionStore.Turn turn : sessionStore.historySnapshot(storeKey)) {
            messages.add(message(turn.role(), turn.content()));
        }
        messages.add(message("user", request.message()));

        ChatRequest chatRequest = new ChatRequest(null, defaultModel, messages, false);

        String rawJson;
        try {
            rawJson = providerClientFactory.getClient(providerType).chat(chatRequest, userApiKey).block();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Provider rate limit reached. Please retry in a moment.",
                        ex
                );
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Provider request failed with status " + ex.getStatusCode().value(),
                    ex
            );
        }

        if (rawJson == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Empty provider response");
        }

        String reply = responseParser.extractAssistantContent(rawJson);
        sessionStore.appendUserThenAssistant(storeKey, request.message(), reply);
        return new AgentChatResponse(reply, sessionId);
    }

    private static Message message(String role, String content) {
        Message m = new Message();
        m.setRole(role);
        m.setContent(content);
        return m;
    }
}
