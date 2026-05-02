package org.fmazmz.springbootai.user.http;

import io.swagger.v3.oas.annotations.Parameter;
import org.fmazmz.springbootai.common.http.ApiResponseWrapper;
import org.fmazmz.springbootai.user.domain.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/login")
    public ResponseEntity<Void> loginWithGithub() {
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, "/oauth2/authorization/github")
                .build();
    }

    @GetMapping("/api/v1/auth/me")
    public ApiResponseWrapper<MeResponse> me(
            @Parameter(hidden = true)
            @CurrentUser User user
    ) {
        return new ApiResponseWrapper<>(new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getAuthProvider().name(),
                user.getAvatarUrl()
        ));
    }
}
