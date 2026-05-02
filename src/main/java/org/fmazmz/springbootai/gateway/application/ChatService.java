package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.domain.Message;
import org.fmazmz.springbootai.gateway.domain.Session;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import org.fmazmz.springbootai.gateway.repository.SessionRepository;
import org.fmazmz.springbootai.user.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChatService {
    private final ProviderClientFactory providerClientFactory;
    private final SessionRepository sessionRepository;

    public ChatService(ProviderClientFactory providerClientFactory, SessionRepository sessionRepository) {
        this.providerClientFactory = providerClientFactory;
        this.sessionRepository = sessionRepository;
    }

    public String sendMessage(LlmProvider providerType, ChatRequest request, String userApiKey, User currentUser) {
        Session session = resolveSession(request, currentUser);
        if (request.messages() != null) {
            request.messages().forEach(message -> session.addMessage(copyForPersistence(message)));
        }
        sessionRepository.save(session);

        try {
            return providerClientFactory.getClient(providerType)
                    .chat(request, userApiKey)
                    .block();
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
    }

    private Session resolveSession(ChatRequest request, User currentUser) {
        if (request.sessionId() != null) {
            return sessionRepository.findByIdAndUserId(request.sessionId(), currentUser.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found for current user"));
        }

        Session session = new Session();
        session.setUser(currentUser);
        return session;
    }

    private Message copyForPersistence(Message message) {
        Message persistedMessage = new Message();
        persistedMessage.setRole(message.getRole());
        persistedMessage.setContent(message.getContent());
        return persistedMessage;
    }
}
