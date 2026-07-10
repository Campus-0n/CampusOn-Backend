package com.campuson.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ExceptionType {

    // Common
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR, "C001", "예상치 못한 에러 발생"),
    BINDING_ERROR(BAD_REQUEST, "C002", "바인딩시 에러 발생"),
    ESSENTIAL_FIELD_MISSING_ERROR(NO_CONTENT, "C003", "필수적인 필드 부재");

    // 앞으로 도메인이 생길 때마다 여기에 추가하면 됩니다.
    // 예: MEMBER_NOT_FOUND(NOT_FOUND, "M001", "존재하지 않는 회원입니다.")

    private final HttpStatus status;
    private final String code;
    private final String message;
}
