package org.fmazmz.springbootai.gateway.infra.providers.openrouter;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterCompletionInvokerWireMockTest {

    private WireMockServer wireMockServer;
    private OpenRouterCompletionInvoker invoker;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        RestClient restClient = RestClient.builder().baseUrl(wireMockServer.baseUrl()).build();
        invoker = new OpenRouterCompletionInvoker(restClient);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void retriesOn503ThenReturnsBody() {
        wireMockServer.stubFor(
                post(urlEqualTo("/chat/completions"))
                        .inScenario("retry")
                        .whenScenarioStateIs(STARTED)
                        .willReturn(aResponse().withStatus(503))
                        .willSetStateTo("second"));

        wireMockServer.stubFor(
                post(urlEqualTo("/chat/completions"))
                        .inScenario("retry")
                        .whenScenarioStateIs("second")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}")));

        Map<String, Object> body = Map.of(
                "model", "m",
                "messages", List.of(Map.of("role", "user", "content", "hi")),
                "stream", false);

        String raw = invoker.postChatCompletions(body, "sk-test");

        assertThat(raw).contains("ok");
        wireMockServer.verify(2, postRequestedFor(urlEqualTo("/chat/completions")));
        wireMockServer.verify(
                2,
                postRequestedFor(urlEqualTo("/chat/completions"))
                        .withHeader("Authorization", WireMock.equalTo("Bearer sk-test")));
    }
}
