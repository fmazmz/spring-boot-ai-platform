package org.fmazmz.springbootai.user.application;

import org.fmazmz.springbootai.user.AuthProvider;
import org.fmazmz.springbootai.user.GithubEmailResolver;
import org.fmazmz.springbootai.user.GithubUser;
import org.fmazmz.springbootai.user.User;
import org.fmazmz.springbootai.user.repository.UserRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAuthentication {
    private final GithubEmailResolver githubEmailResolver;
    private final UserRepository userRepository;

    public UserAuthentication(GithubEmailResolver githubEmailResolver, UserRepository userRepository) {
        this.githubEmailResolver = githubEmailResolver;
        this.userRepository = userRepository;
    }

    public User resolveUser(OAuth2AuthenticationToken authenticationToken) {
        AuthProvider loginProvider = AuthProvider.fromRegistrationId(authenticationToken.getAuthorizedClientRegistrationId());
        // temporary check
        if (loginProvider != AuthProvider.GITHUB) {
            throw new IllegalStateException("Only Github login is supported.");
        }

        OAuth2User principal = authenticationToken.getPrincipal();
        String providerId = extractProviderId(principal, loginProvider);
        String avatarUrl = principal.getAttribute(loginProvider.getAvatarAttribute());
        String email = githubEmailResolver.resolveGithubEmail(authenticationToken);

        Optional<User> existing = userRepository.findByAuthProviderAndProviderId(loginProvider, providerId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Optional<User> preProvisioned = userRepository.findByEmailIgnoreCase(email);
        if (preProvisioned.isEmpty()) {
            GithubUser newUser = new GithubUser();
            newUser.setAuthProvider(AuthProvider.GITHUB);
            newUser.setProviderId(providerId);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setEmail(email);
            return userRepository.save(newUser);
        }

        User user = preProvisioned.get();
        if (user.getAuthProvider() != AuthProvider.GITHUB && user.getAuthProvider() != null) {
            throw new IllegalStateException("Configured account provider does not match GitHub login.");
        }

        if (user.getProviderId() != null
                && !loginProvider.linkablePlaceholderProviderIds().contains(user.getProviderId())
                && !user.getProviderId().equals(providerId)) {
            throw new IllegalStateException("This internal account is already linked to a different GitHub identity.");
        }

        user.setAuthProvider(AuthProvider.GITHUB);
        user.setProviderId(providerId);
        user.setAvatarUrl(avatarUrl);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            user.setEmail(email);
        }   

        return userRepository.save(user);
    }

    private String extractProviderId(OAuth2User principal, AuthProvider provider) {
        Object rawId = principal.getAttribute(provider.getIdAttribute());
        if (rawId == null) {
            throw new IllegalStateException("OAuth2 provider did not return a user ID");
        }
        return rawId.toString();
    }
}
