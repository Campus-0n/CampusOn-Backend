package com.campuson.backend.token.controller;

import com.campuson.backend.global.jwt.JwtAuthentication;
import com.campuson.backend.global.response.ResponseBody;
import com.campuson.backend.token.api.TokenApi;
import com.campuson.backend.token.dto.request.TokenRequest;
import com.campuson.backend.token.dto.response.TokenResponse;
import com.campuson.backend.token.entity.Token;
import com.campuson.backend.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.campuson.backend.global.response.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class TokenController implements TokenApi {
    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ResponseEntity<ResponseBody<TokenResponse>> refresh(@RequestBody TokenRequest tokenRequest) {
        Token token = new Token(tokenRequest.accessToken(), tokenRequest.refreshToken());
        TokenResponse response = tokenService.refresh(token);
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseBody<Void>> logout(Authentication authentication) {
        JwtAuthentication jwtAuthentication = (JwtAuthentication) authentication;
        tokenService.logout(jwtAuthentication.userId());
        return ResponseEntity.ok(createSuccessResponse());
    }
}
