package org.fmazmz.springbootai.config;

import jakarta.servlet.http.HttpSession;
import org.fmazmz.springbootai.user.http.AuthController;
import org.fmazmz.springbootai.user.application.UserAuthentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserAuthentication userAuthentication
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/signup", "/login", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .oauth2Login(oauth -> oauth.successHandler(
                        (request, response, authentication) -> {
                    if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                        HttpSession session = request.getSession(false);
                        boolean allowCreate = session != null
                                && Boolean.TRUE.equals(session.getAttribute(AuthController.SIGNUP_FLOW_SESSION_KEY));
                        if (session != null) {
                            session.removeAttribute(AuthController.SIGNUP_FLOW_SESSION_KEY);
                        }
                        userAuthentication.resolveUser(oauthToken, allowCreate);
                    }
                    response.sendRedirect("/");
                }));

        return http.build();
    }
}
