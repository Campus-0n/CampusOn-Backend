package com.campuson.backend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
@AllArgsConstructor
public enum ExceptionType {

    // Common
    UNEXPECTED_SERVER_ERROR(INTERNAL_SERVER_ERROR, "C001", "예상치 못한 에러 발생"),
    BINDING_ERROR(BAD_REQUEST, "C002", "바인딩시 에러 발생"),
    ESSENTIAL_FIELD_MISSING_ERROR(NO_CONTENT, "C003", "필수적인 필드 부재"),

    // Security
    ILLEGAL_REGISTRATION_ID(NOT_ACCEPTABLE, "S001", "잘못된 registration id 입니다"),
    NEED_AUTHORIZED(UNAUTHORIZED, "S002", "인증이 필요합니다."),
    ACCESS_DENIED(FORBIDDEN, "S003", "권한이 없습니다."),
    JWT_EXPIRED(UNAUTHORIZED, "S004", "JWT 토큰이 만료되었습니다."),
    JWT_INVALID(UNAUTHORIZED, "S005", "JWT 토큰이 올바르지 않습니다."),
    JWT_NOT_EXIST(UNAUTHORIZED, "S006", "JWT 토큰이 존재하지 않습니다."),

    // User

    // 회원가입 / 로그인
    DUPLICATE_EMAIL(CONFLICT, "U001", "이미 가입된 이메일입니다."),
    USER_NOT_FOUND(NOT_FOUND, "U002", "존재하지 않는 사용자입니다."),
    INVALID_LOGIN(UNAUTHORIZED, "U003", "이메일 또는 비밀번호가 올바르지 않습니다."),
    EMAIL_NOT_VERIFIED(FORBIDDEN, "U004", "이메일 인증이 필요합니다."),
    DUPLICATE_LOGIN_ID(CONFLICT, "U005", "이미 사용 중인 아이디입니다."),
    INVALID_EMAIL_DOMAIN(BAD_REQUEST, "U006", "학교 이메일로만 가입할 수 있습니다."),

    // 이메일 인증 코드 (ConfirmationToken)
    CONFIRMATION_TOKEN_NOT_EXIST(BAD_REQUEST, "T003", "인증 코드가 존재하지 않습니다. 재발급 받아주세요."),
    CONFIRMATION_TOKEN_EXPIRED(BAD_REQUEST, "T004", "인증 코드가 만료되었습니다."),
    CONFIRMATION_TOKEN_NOT_MATCHED(BAD_REQUEST, "T005", "인증 코드가 일치하지 않습니다."),

    // Token
    REFRESH_TOKEN_NOT_EXIST(NOT_FOUND, "T001", "리프래시 토큰이 존재하지 않습니다"),
    TOKEN_NOT_MATCHED(UNAUTHORIZED, "T002","일치하지 않는 토큰입니다"),

    // Reservation
    RESERVATION_NOT_FOUND(NOT_FOUND, "RS001", "예약을 찾을 수 없습니다."),
    NOT_RESERVATION_OWNER(FORBIDDEN, "RS003", "본인의 예약이 아닙니다."),
    TIME_CONFLICT(CONFLICT, "RS004", "해당 시간대에 이미 예약이 있습니다."),
    INVALID_END_TIME(BAD_REQUEST, "RS005", "종료시각은 정시 50분 단위여야 합니다."),
    INVALID_HEADCOUNT(BAD_REQUEST, "RS006", "이용 인원이 올바르지 않습니다."),
    EXTENSION_LIMIT_EXCEEDED(CONFLICT, "RS007", "연장 가능 횟수를 초과했습니다."),
    INVALID_STATUS(CONFLICT, "RS008", "현재 상태에서 할 수 없는 동작입니다."),
    INVALID_TIME_RANGE(BAD_REQUEST, "RS009", "시작시각이 종료시각보다 앞서야 합니다."),
    INVALID_RESERVATION_DURATION(BAD_REQUEST, "RS011", "예약은 2시간 단위여야 합니다. (정시 시작, 예: 09:00~10:50)"),
    INVALID_EXTENSION_UNIT(BAD_REQUEST, "RS012", "연장은 1시간 단위로만 가능합니다."),

    // 체크인(QR + GPS 인증)
    CHECKIN_TIME_WINDOW(CONFLICT, "RS013", "체크인 가능 시간이 아닙니다."),
    CHECKIN_INVALID_QR(CONFLICT, "RS014", "예약한 강의실의 QR이 아닙니다."),
    CHECKIN_OUT_OF_RANGE(CONFLICT, "RS015", "강의실 반경 밖입니다."),

    // Room
    ROOM_NOT_FOUND(NOT_FOUND, "RM001", "존재하지 않는 강의실입니다."),
    NO_AVAILABLE_ROOM(NOT_FOUND, "RM002", "현재 이용 가능한 강의실이 없습니다."),
    OUT_OF_OPERATING_HOURS(BAD_REQUEST, "RM003", "운영시간 외 시간입니다.");

    // 앞으로 도메인이 생길 때마다 여기에 추가하면 됩니다.
    // 예: MEMBER_NOT_FOUND(NOT_FOUND, "M001", "존재하지 않는 회원입니다.")

    private final HttpStatus status;
    private final String code;
    private final String message;
}
