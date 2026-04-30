package org.fmazmz.springbootai.user.repository;

import org.fmazmz.springbootai.user.AuthProvider;
import org.fmazmz.springbootai.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByAuthProviderAndProviderId(AuthProvider provider, String providerId);
    Optional<User> findByEmailIgnoreCase(String email);
}
