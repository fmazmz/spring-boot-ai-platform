package org.fmazmz.springbootai.agent;

import lombok.extern.slf4j.Slf4j;
import org.fmazmz.springbootai.agent.domain.AgentRole;
import org.fmazmz.springbootai.agent.domain.Prompt;
import org.fmazmz.springbootai.agent.domain.Agent;
import org.fmazmz.springbootai.agent.repository.AgentRoleRepository;
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
            String name = i.roleName();
            if (agentRoleRepository.existsByName(name)) {
                continue;
            }
            Agent agentRole = new Agent();
            agentRole.setName(name);

            Prompt prompt = new Prompt();
            prompt.setText(i.getDefaultPrompt());
            agentRole.setPrompt(prompt);

            agentRoleRepository.save(agentRole);
        }

        log.info("Roles seeded successfully.");
    }
}
