package com.campuson.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Email @NotBlank String email,
        @NotBlank String loginId,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String name
) {}
