package com.campuson.backend.global.jwt.exception;

import com.campuson.backend.global.exception.ExceptionType;

public class JwtAccessDeniedException extends JwtAuthenticationException {
    public JwtAccessDeniedException() {
        super(ExceptionType.ACCESS_DENIED);
    }
}
