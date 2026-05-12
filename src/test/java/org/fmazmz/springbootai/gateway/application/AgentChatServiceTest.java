package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.agent.domain.Agent;
import org.fmazmz.springbootai.agent.domain.AgentRole;
import org.fmazmz.springbootai.agent.domain.Prompt;
import org.fmazmz.springbootai.agent.repository.AgentRoleRepository;
import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.domain.Message;
import org.fmazmz.springbootai.gateway.dto.AgentChatRequest;
import org.fmazmz.springbootai.gateway.dto.AgentChatResponse;
import org.fmazmz.springbootai.gateway.dto.ChatRequest;
import org.fmazmz.springbootai.gateway.infra.providers.openrouter.OpenRouterResponseParser;
import org.fmazmz.springbootai.user.domain.GithubUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentChatServiceTest {

    private static final String SAMPLE_JSON =
            "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Answer 1\"}}]}";

    @Mock
    private ProviderClientFactory providerClientFactory;

    @Mock
    private ProviderClient providerClient;

    @Mock
    private AgentRoleRepository agentRoleRepository;

    private InMemoryChatSessionStore sessionStore;
    private AgentChatService agentChatService;

    @BeforeEach
    void setUp() {
        sessionStore = new InMemoryChatSessionStore();
        agentChatService = new AgentChatService(
                providerClientFactory,
                sessionStore,
                new OpenRouterResponseParser(),
                agentRoleRepository);
        ReflectionTestUtils.setField(agentChatService, "defaultModel", "test/model");

        Agent backend = new Agent();
        backend.setName("BACKEND_DEVELOPER");
        Prompt p = new Prompt();
        p.setText("You are the backend agent.");
        backend.setPrompt(p);
        when(agentRoleRepository.findByName("BACKEND_DEVELOPER")).thenReturn(Optional.of(backend));

        when(providerClientFactory.getClient(LlmProvider.OPENROUTER)).thenReturn(providerClient);
        when(providerClient.chat(any(ChatRequest.class), eq("user-key"))).thenReturn(Mono.just(SAMPLE_JSON));
    }

    @Test
    void secondTurnIncludesPriorHistory() {
        GithubUser user = new GithubUser();
        user.setId(UUID.randomUUID());

        AgentChatResponse first = agentChatService.chat(
                LlmProvider.OPENROUTER,
                new AgentChatRequest(AgentRole.BACKEND_DEVELOPER, "first user message", null),
                "user-key",
                user);

        assertThat(first.sessionId()).isNotBlank();
        assertThat(first.reply()).isEqualTo("Answer 1");

        when(providerClient.chat(any(ChatRequest.class), eq("user-key")))
                .thenReturn(Mono.just(
                        "{\"choices\":[{\"message\":{\"content\":\"Answer 2\"}}]}"));

        agentChatService.chat(
                LlmProvider.OPENROUTER,
                new AgentChatRequest(AgentRole.BACKEND_DEVELOPER, "second user message", first.sessionId()),
                "user-key",
                user);

        ArgumentCaptor<ChatRequest> captor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(providerClient, times(2)).chat(captor.capture(), eq("user-key"));

        List<Message> firstMessages = captor.getAllValues().get(0).messages();
        List<Message> secondMessages = captor.getAllValues().get(1).messages();

        assertThat(firstMessages).hasSize(2);
        assertThat(firstMessages.getFirst().getRole()).isEqualTo("system");
        assertThat(firstMessages.getFirst().getContent()).isEqualTo("You are the backend agent.");
        assertThat(firstMessages.get(1).getContent()).isEqualTo("first user message");

        assertThat(secondMessages).hasSize(4);
        assertThat(secondMessages.get(1).getRole()).isEqualTo("user");
        assertThat(secondMessages.get(1).getContent()).isEqualTo("first user message");
        assertThat(secondMessages.get(2).getRole()).isEqualTo("assistant");
        assertThat(secondMessages.get(2).getContent()).isEqualTo("Answer 1");
        assertThat(secondMessages.get(3).getContent()).isEqualTo("second user message");
    }
}
