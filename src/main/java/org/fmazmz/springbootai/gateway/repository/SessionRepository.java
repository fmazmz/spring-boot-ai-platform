package org.fmazmz.springbootai.gateway.repository;

import org.fmazmz.springbootai.gateway.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByIdAndUserId(UUID sessionId, UUID userId);
}
