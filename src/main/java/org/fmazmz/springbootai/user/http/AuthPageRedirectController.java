package org.fmazmz.springbootai.user.http;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthPageRedirectController {
    @GetMapping("/signup")
    public ResponseEntity<Void> signup(HttpSession session) {
        session.setAttribute(AuthController.SIGNUP_FLOW_SESSION_KEY, true);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/github")
                .build();
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(HttpSession session) {
        session.setAttribute(AuthController.SIGNUP_FLOW_SESSION_KEY, false);
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/github")
                .build();
    }
}
