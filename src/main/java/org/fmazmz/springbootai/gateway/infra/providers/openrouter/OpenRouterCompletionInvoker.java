package org.fmazmz.springbootai.gateway.infra.providers.openrouter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
public class OpenRouterCompletionInvoker {

    private static final int MAX_ATTEMPTS = 4;
    private static final long INITIAL_DELAY_MS = 400;

    private final RestClient client;

    public OpenRouterCompletionInvoker(@Qualifier("openRouterRestClient") RestClient openRouterClient) {
        this.client = openRouterClient;
    }

    /**
     * POST /chat/completions with exponential backoff on 429 and 503.
     */
    public String postChatCompletions(Map<String, Object> body, String userApiKey) {
        long delayMs = INITIAL_DELAY_MS;
        RestClientResponseException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return client.post()
                        .uri("/chat/completions")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userApiKey)
                        .body(body)
                        .retrieve()
                        .body(String.class);
            } catch (RestClientResponseException ex) {
                last = ex;
                int code = ex.getStatusCode().value();
                if ((code == 429 || code == 503) && attempt < MAX_ATTEMPTS) {
                    sleepQuietly(delayMs);
                    delayMs *= 2;
                    continue;
                }
                throw ex;
            }
        }
        throw last;
    }

    private static void sleepQuietly(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during provider retry backoff", ie);
        }
    }
}
