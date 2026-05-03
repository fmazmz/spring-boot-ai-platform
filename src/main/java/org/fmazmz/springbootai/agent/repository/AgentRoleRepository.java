package org.fmazmz.springbootai.agent.repository;

import org.fmazmz.springbootai.agent.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AgentRoleRepository extends JpaRepository<Role, UUID> {

    boolean existsByName(String name);
}
