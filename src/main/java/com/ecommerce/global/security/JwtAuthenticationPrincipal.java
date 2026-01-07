package com.ecommerce.global.security;

import java.security.Principal;

public record JwtAuthenticationPrincipal(
        Long memberId,
        String email
) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}
