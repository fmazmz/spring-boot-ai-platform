package org.fmazmz.springbootai.agent.repository;

import org.fmazmz.springbootai.agent.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentRoleRepository extends JpaRepository<Agent, UUID> {

    boolean existsByName(String name);

    Optional<Agent> findByName(String name);
}
