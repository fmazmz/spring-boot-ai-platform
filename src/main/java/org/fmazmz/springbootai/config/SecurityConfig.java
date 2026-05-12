package org.fmazmz.springbootai.config;

import org.fmazmz.springbootai.user.application.UserAuthentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Paths that stay public so OAuth2 login and framework error dispatch work.
     * All other URLs require an authenticated principal (no anonymous access by default).
     */
    private static final String[] PERMIT_ALL_PATHS = {
            "/error",
            "/login",
            "/oauth2/**",
            "/login/oauth2/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserAuthentication userAuthentication
    ) throws Exception {
        http.securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.pathPattern("/api/**")
                        )
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .oauth2Login(oauth -> oauth.successHandler((request, response, authentication) -> {
                    if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                        userAuthentication.resolveUser(oauthToken);
                    }
                    response.sendRedirect("/");
                }));

        return http.build();
    }
}
