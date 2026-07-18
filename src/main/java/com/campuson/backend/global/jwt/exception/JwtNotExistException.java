package com.campuson.backend.global.jwt.exception;

import com.campuson.backend.global.exception.ExceptionType;

public class JwtNotExistException extends JwtAuthenticationException {
    public JwtNotExistException() {
        super(ExceptionType.JWT_NOT_EXIST);
    }
}
