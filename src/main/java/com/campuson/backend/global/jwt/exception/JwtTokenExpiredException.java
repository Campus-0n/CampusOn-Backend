package com.campuson.backend.global.jwt.exception;

import com.campuson.backend.global.exception.ExceptionType;
import lombok.Getter;

@Getter
public class JwtTokenExpiredException extends JwtAuthenticationException {

    public JwtTokenExpiredException(Throwable cause) {
        super(cause, ExceptionType.JWT_EXPIRED);
    }

}
