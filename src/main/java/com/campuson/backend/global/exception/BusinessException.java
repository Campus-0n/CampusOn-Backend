package com.campuson.backend.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ExceptionType exceptionType;

    /** 기본 메시지 대신 노출할 동적 메시지(예: "현재 거리 82m"). 없으면 null. */
    private final String customMessage;

    public BusinessException(ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
        this.customMessage = null;
    }

    public BusinessException(ExceptionType exceptionType, String customMessage) {
        super(customMessage);
        this.exceptionType = exceptionType;
        this.customMessage = customMessage;
    }
}
