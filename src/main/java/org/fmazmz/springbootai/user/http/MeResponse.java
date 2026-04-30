package org.fmazmz.springbootai.user.http;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String email,
        String authProvider,
        String avatarUrl
) {
}
