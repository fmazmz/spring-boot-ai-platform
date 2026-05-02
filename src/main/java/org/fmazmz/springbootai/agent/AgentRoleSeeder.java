package org.fmazmz.springbootai.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
@Slf4j
@Order(20)
public class AgentRoleSeeder implements CommandLineRunner {
    private final AgentRoleRepository agentRoleRepository;

    public AgentRoleSeeder(AgentRoleRepository agentRoleRepository) {
        this.agentRoleRepository = agentRoleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Seeding Roles ...");

        for (AgentRole i : EnumSet.allOf(AgentRole.class)) {
            Role agentRole = new Role();
            agentRole.setName(i.name());

            Prompt prompt = new Prompt();
            prompt.setText("You are a " + i);
            agentRole.setPrompt(prompt);

            agentRoleRepository.save(agentRole);
        }

        log.info("Roles seeded successfully.");
    }
}
