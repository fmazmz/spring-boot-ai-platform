package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ProviderClientFactory providerClientFactory;

    public ChatService(ProviderClientFactory providerClientFactory) {
        this.providerClientFactory = providerClientFactory;
    }

    public String sendMessage(LlmProvider providerType, ChatRequest request, String userApiKey) {
        return providerClientFactory.getClient(providerType)
                .chat(request, userApiKey)
                .block();
    }
}
