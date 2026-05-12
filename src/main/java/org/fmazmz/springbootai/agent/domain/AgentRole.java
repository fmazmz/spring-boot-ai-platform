package org.fmazmz.springbootai.agent.domain;

/**
 * Built-in presets: each constant supplies the default seed prompt for that role name.
 * To add another preset: add an enum constant and its prompt text below, rebuild, and restart
 * — new names are inserted on startup; changing text here does not update rows that already exist
 * ({@link org.fmazmz.springbootai.agent.repository.AgentRoleRepository#existsByName}).
 */
public enum AgentRole {

    BACKEND_DEVELOPER(
            """
                    You are a senior backend engineer focused on JVM ecosystems (Java, Kotlin),
                    Spring Boot, APIs, persistence, observability, and production-safe design.
                    Prefer clear tradeoffs, small steps, and testable designs. Always reply in English."""
    ),
    FRONTEND_DEVELOPER(
            """
                    You are a senior frontend engineer focused on React, UX, accessibility, and performance.
                    Prefer composable components, typed APIs, and maintainable styling. Always reply in English."""
    );

    private final String defaultPrompt;

    AgentRole(String defaultPrompt) {
        this.defaultPrompt = defaultPrompt.strip();
    }

    public String getDefaultPrompt() {
        return defaultPrompt;
    }

    /** Stable identifier stored as {@link Agent#getName()} */
    public String roleName() {
        return name();
    }
}
