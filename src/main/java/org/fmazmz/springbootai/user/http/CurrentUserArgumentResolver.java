package org.fmazmz.springbootai.user.http;

import org.fmazmz.springbootai.user.domain.AuthProvider;
import org.fmazmz.springbootai.user.domain.User;
import org.fmazmz.springbootai.user.repository.UserRepository;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRepository userRepository;

    public CurrentUserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken) || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Not authenticated");
        }

        AuthProvider provider = AuthProvider.fromRegistrationId(oauthToken.getAuthorizedClientRegistrationId());
        OAuth2User principal = oauthToken.getPrincipal();
        Object rawProviderId = principal.getAttribute(provider.getIdAttribute());
        if (rawProviderId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Provider ID is missing");
        }

        return userRepository.findByAuthProviderAndProviderId(provider, rawProviderId.toString())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not registered"));
    }
}
