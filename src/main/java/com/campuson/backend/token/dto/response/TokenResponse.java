package com.campuson.backend.token.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {

}
