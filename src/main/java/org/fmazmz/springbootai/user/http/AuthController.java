package org.fmazmz.springbootai.user.http;

import jakarta.servlet.http.HttpSession;
import org.fmazmz.springbootai.user.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    public static final String SIGNUP_FLOW_SESSION_KEY = "AUTH_SIGNUP_FLOW";

    @GetMapping("/signup")
    public ResponseEntity<Void> signup(HttpSession session) {
        session.setAttribute(SIGNUP_FLOW_SESSION_KEY, true);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/github")
                .build();
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpSession session) {
        session.setAttribute(SIGNUP_FLOW_SESSION_KEY, false);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/github")
                .build();
    }

    @GetMapping("/api/v1/auth/me")
    public MeResponse me(@CurrentUser User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getAuthProvider().name(),
                user.getAvatarUrl()
        );
    }
}
