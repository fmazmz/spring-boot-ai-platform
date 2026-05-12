package org.fmazmz.springbootai.gateway.application;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryChatSessionStore {

    private static final int MAX_TURNS = 25;

    private final ConcurrentHashMap<String, List<Turn>> sessions = new ConcurrentHashMap<>();

    public List<Turn> historySnapshot(String compositeKey) {
        List<Turn> list = sessions.get(compositeKey);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

    public void appendUserThenAssistant(String compositeKey, String userContent, String assistantContent) {
        sessions.compute(compositeKey, (k, existing) -> {
            List<Turn> turns = existing != null ? existing : new ArrayList<>();
            turns.add(new Turn("user", userContent));
            turns.add(new Turn("assistant", assistantContent));
            while (turns.size() > MAX_TURNS * 2) {
                turns.removeFirst();
            }
            return turns;
        });
    }

    public record Turn(String role, String content) {
    }
}
