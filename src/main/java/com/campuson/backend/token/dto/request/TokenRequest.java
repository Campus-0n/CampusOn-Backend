package com.campuson.backend.token.dto.request;

public record TokenRequest (
        String accessToken,
        String refreshToken
){

}

