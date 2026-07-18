package com.campuson.backend.user.dto.response;

import com.campuson.backend.token.entity.Token;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String name
) {
    public static LoginResponse of(Token token, String name) {
        return new LoginResponse(token.getAccessToken(), token.getRefreshToken(), name);
    }
}
