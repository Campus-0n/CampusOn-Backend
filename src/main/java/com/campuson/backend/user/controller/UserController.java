package com.campuson.backend.user.controller;

import com.campuson.backend.global.jwt.JwtHandler;
import com.campuson.backend.global.jwt.JwtUserClaim;
import com.campuson.backend.global.response.ResponseBody;
import com.campuson.backend.token.dto.response.TokenResponse;
import com.campuson.backend.token.entity.Token;
import com.campuson.backend.user.api.UserApi;
import com.campuson.backend.user.domain.User;
import com.campuson.backend.user.dto.request.LoginRequest;
import com.campuson.backend.user.dto.request.SignupRequest;
import com.campuson.backend.user.dto.request.VerifyEmailRequest;
import com.campuson.backend.user.dto.response.LoginResponse;
import com.campuson.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.campuson.backend.global.response.ResponseUtil.createSuccessResponse;

@RestController
@RequestMapping("/auth/user")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final JwtHandler jwtHandler;

    @PostMapping("/signup")
    public ResponseEntity<ResponseBody<Void>> signup(@RequestBody @Valid SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(createSuccessResponse());
    }

    /**
    @PostMapping("/verify-email")
    public ResponseEntity<ResponseBody<Void>> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        userService.verifyEmail(request);
        return ResponseEntity.ok(createSuccessResponse());
    }
     */

    @PostMapping("/login")
    public ResponseEntity<ResponseBody<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        User user = userService.login(request); // getVerifiedUser 아니라 login (이전에 정의한 메서드명)
        Token token = jwtHandler.createTokens(JwtUserClaim.create(user));
        return ResponseEntity.ok(createSuccessResponse(LoginResponse.of(token, user.getName())));
    }
}