package org.fmazmz.springbootai.user.http;

import org.fmazmz.springbootai.user.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    public static final String SIGNUP_FLOW_SESSION_KEY = "AUTH_SIGNUP_FLOW";

    @GetMapping("/signup")
    public ResponseEntity<Void> signupWithGithub(HttpSession session) {
        session.setAttribute(SIGNUP_FLOW_SESSION_KEY, true);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/github")
                .build();
    }

    @GetMapping("/login")
    public ResponseEntity<Void> loginWithGithub(HttpSession session) {
        session.setAttribute(SIGNUP_FLOW_SESSION_KEY, false);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/github")
                .build();
    }

    @GetMapping("/me")
    public MeResponse me(@CurrentUser User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getAuthProvider().name(),
                user.getAvatarUrl()
        );
    }
}
