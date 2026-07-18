package com.campuson.backend.user.api;

import com.campuson.backend.global.config.swagger.SwaggerApiFailedResponse;
import com.campuson.backend.global.config.swagger.SwaggerApiResponses;
import com.campuson.backend.global.config.swagger.SwaggerApiSuccessResponse;
import com.campuson.backend.global.exception.ExceptionType;
import com.campuson.backend.global.jwt.JwtUserClaim;
import com.campuson.backend.global.response.ResponseBody;
import com.campuson.backend.token.dto.response.TokenResponse;
import com.campuson.backend.token.entity.Token;
import com.campuson.backend.user.domain.User;
import com.campuson.backend.user.dto.request.LoginRequest;
import com.campuson.backend.user.dto.request.SignupRequest;
import com.campuson.backend.user.dto.request.VerifyEmailRequest;
import com.campuson.backend.user.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.campuson.backend.global.response.ResponseUtil.createSuccessResponse;

public interface UserApi {

    @PostMapping("/signup")
    ResponseEntity<ResponseBody<Void>> signup(@RequestBody @Valid SignupRequest request);

    @PostMapping("/verify-email")
    ResponseEntity<ResponseBody<Void>> verifyEmail(@RequestBody @Valid VerifyEmailRequest request);

    @PostMapping("/login")
    ResponseEntity<ResponseBody<LoginResponse>> login(@RequestBody @Valid LoginRequest request);
}