# CampusOn 백엔드 구조 정리

교내 강의실/스터디룸 예약 서비스 **CampusOn**의 백엔드 정리 문서입니다.
회원 인증(JWT), 강의실 예약, 예약 상태 자동 관리(스케줄러)를 제공합니다.

## 기술 스택

| 구분 | 내용 |
|------|------|
| 언어/런타임 | Java 25 |
| 프레임워크 | Spring Boot 4.1.0 (Spring Framework 7) |
| 빌드 | Gradle 9.5.1 (Foojay 툴체인) |
| 데이터 | Spring Data JPA · Hibernate 7 · MySQL 8.4 |
| 인증 | Spring Security + 자체 JWT |
| 문서화 | springdoc-openapi (Swagger UI) |
| 기타 | Lombok, Spring Mail(Gmail), Docker Compose |

실행: `docker-compose up -d --build` → `localhost:8080` · Swagger `localhost:8080/swagger-ui/index.html`

---

## 패키지 구조 한눈에

```
com.campuson.backend
├── global/        # 공통 인프라 (응답 규격, 예외, JWT, 시큐리티, 설정, Swagger)
├── user/          # 회원 (가입 / 이메일 인증 / 로그인)
├── token/         # 인증 토큰 (리프레시 토큰 / 이메일 확인 코드)
├── reservation/   # 예약 (생성 / 조회 / 연장 / 종료 / 취소 / 스케줄러)
└── room/          # 강의실 (정보 / 가용 시간 조회)
```

각 도메인은 `controller(api) → service → repository → entity` 계층과 `dto(request/response)`로 구성됩니다.

---

## 기능별 정리 (도메인)

### 1. 회원 — `user`

회원가입 시 학교 이메일로만 가입 가능하며, 이메일 인증을 거쳐야 로그인할 수 있습니다.

**API** (`/auth/user`)

| 메서드 | 경로 | 기능 |
|--------|------|------|
| POST | `/auth/user/signup` | 회원가입 (중복·이메일 도메인 검증 후 인증코드 발송) |
| POST | `/auth/user/verify-email` | 이메일 인증코드 확인 |
| POST | `/auth/user/login` | 로그인 → JWT 발급 |

**파일**

| 파일 | 설명 |
|------|------|
| `user/controller/UserController.java` | 회원 API 엔드포인트 |
| `user/api/UserApi.java` | Swagger 문서용 인터페이스 |
| `user/service/UserService.java` | `signup` / `verifyEmail` / `login` / `isAdmin` 비즈니스 로직 |
| `user/domain/User.java` | 회원 엔티티 (email, loginId, password, name, role, emailVerified) |
| `user/domain/UserRole.java` | 권한 열거형 (`USER`, `ADMIN`) |
| `user/repository/UserRepository.java` | 이메일/로그인ID 조회·중복 검사 |
| `user/dto/request/*` | `SignupRequest`, `VerifyEmailRequest`, `LoginRequest` |
| `user/dto/response/LoginResponse.java` | 로그인 응답(토큰 등) |

### 2. 인증 토큰 — `token`

로그인 유지를 위한 **리프레시 토큰**과, 이메일 인증용 **확인 코드**를 관리합니다.

**API** (`/auth/token`)

| 메서드 | 경로 | 기능 |
|--------|------|------|
| POST | `/auth/token/refresh` | 리프레시 토큰으로 액세스 토큰 재발급 |
| POST | `/auth/token/logout` | 로그아웃 (리프레시 토큰 삭제) |

**파일**

| 파일 | 설명 |
|------|------|
| `token/controller/TokenController.java` · `token/api/TokenApi.java` | 토큰 API + Swagger 문서 |
| `token/service/TokenService.java` | 리프레시(`refresh`) / 로그아웃(`logout`) |
| `token/service/ConfirmationTokenService.java` | 이메일 인증코드 발급(`issue`) / 확인(`confirm`) |
| `token/entity/RefreshToken.java` | 리프레시 토큰 (userId, refreshToken, expiredAt) |
| `token/entity/ConfirmationToken.java` | 이메일 인증코드 (userId, code, expiredAt) |
| `token/entity/Token.java` | 토큰 공통 추상 타입 |
| `token/repository/*` | `RefreshTokenRepository`, `ConfirmationTokenRepository` (userId 조회·삭제, 만료 토큰 정리) |
| `token/dto/*` | `TokenRequest`, `TokenResponse` |

### 3. 예약 — `reservation`

강의실을 시간대별로 예약하고, 연장/종료/취소하며, 시간이 지난 예약은 스케줄러가 자동 처리합니다.

**API** (`/api/reservations`)

| 메서드 | 경로 | 기능 |
|--------|------|------|
| POST | `/api/reservations` | 예약 생성 (인원·시간 충돌·정책 검증) |
| GET | `/api/reservations/me` | 내 예약 목록 (탭·상태별 필터) |
| GET | `/api/reservations/{id}` | 예약 상세 |
| PATCH | `/api/reservations/{id}/extend` | 이용 시간 연장 |
| PATCH | `/api/reservations/{id}/end` | 이용 종료 |
| PATCH | `/api/reservations/{id}/cancel` | 예약 취소 |

> 참고: 팀원(참여자) 추가/삭제 기능은 미사용으로 결정되어 제거되었습니다.

**파일**

| 파일 | 설명 |
|------|------|
| `reservation/controller/ReservationController.java` | 예약 API 엔드포인트 |
| `reservation/service/ReservationService.java` | 생성·목록·상세·연장·종료·취소 핵심 로직 |
| `reservation/service/AvailabilityService.java` | 강의실 가용 시간대 계산 |
| `reservation/scheduler/ReservationScheduler.java` | 미체크인 자동 취소(NO_SHOW), 종료시각 도달 자동 완료(COMPLETED) |
| `reservation/ReservationPolicy.java` | 시간 충돌·버퍼 등 예약 정책 규칙 |
| `reservation/entity/Reservation.java` | 예약 엔티티 (room, userId, 날짜/시간, 목적, 인원, 상태, 연장횟수, 체크인시각) |
| `reservation/entity/ReservationStatus.java` | 상태 열거형 (아래 상태 흐름 참고) |
| `reservation/entity/ReservationTab.java` | 목록 탭 (`UPCOMING`, `IN_USE`, `PAST`) ↔ 상태 매핑 |
| `reservation/repository/ReservationRepository.java` | 사용자·상태·강의실+날짜 기준 조회 |
| `reservation/dto/request/*` | `CreateReservationRequest`, `ExtendReservationRequest` |
| `reservation/dto/response/*` | `CreateReservationResponse`, `MyReservationsResponse`, `ReservationSummaryResponse`, `ReservationDetailResponse`, `ExtendReservationResponse`, `StatusResponse`, `AvailabilityResponse` |

**예약 상태 흐름**

```
RESERVED ──체크인(QR+GPS)──▶ CHECKED_IN ──종료/시각도달──▶ COMPLETED
   │
   └ 미체크인(유예 초과) ──▶ NO_SHOW
RESERVED ──사용자 취소──▶ CANCELLED
```

### 4. 강의실 — `room`

예약 대상 강의실 정보와, 특정 날짜의 예약 가능 시간대를 제공합니다. GPS 좌표·반경과 QR 토큰은 체크인 검증용입니다.

**API** (`/api/rooms`)

| 메서드 | 경로 | 기능 |
|--------|------|------|
| GET | `/api/rooms/{roomId}/availability` | 해당 강의실의 날짜별 가용 시간대 조회 |

**파일**

| 파일 | 설명 |
|------|------|
| `reservation/controller/RoomAvailabilityController.java` | 가용 시간 조회 API |
| `room/entity/Room.java` | 강의실 (이름/건물/층/호수, 수용인원, 위경도·허용반경, qrToken) |
| `room/repository/RoomRepository.java` | 강의실 조회 (비관적 락 조회 `findWithLockById` 포함) |

---

## 공통 인프라 — `global`

모든 도메인이 공유하는 규격과 보안·설정입니다.

| 영역 | 파일 | 설명 |
|------|------|------|
| 응답 규격 | `global/response/*` | 성공/실패 통일 응답 (`ResponseBody` sealed → `SuccessResponseBody`/`FailedResponseBody`), `ResponseUtil`, 페이징 `GlobalPageResponse` |
| 예외 처리 | `global/exception/*` | `BusinessException` + 에러코드 `ExceptionType` + 전역 핸들러 `GlobalExceptionHandler` |
| JWT | `global/jwt/*` | 토큰 발급/검증 (`TokenProvider`, `JwtHandler`, `JwtAuthenticationFilter`, `JwtAuthentication` 등)과 JWT 전용 예외들 |
| 시큐리티 | `global/config/security/*` | `SecurityConfig`(경로별 인가·JWT 필터), `CorsConfig`, `JwtConfig`, `PasswordEncoderConfig` |
| Swagger | `global/config/swagger/*` | API 문서 설정 및 응답 커스터마이징 |
| 설정 | `global/config/*` | `JpaAuditingConfig`(생성/수정시각 자동기록), `SchedulingConfig`(스케줄러 활성화) |
| 공통 엔티티 | `global/base/BaseEntity.java` | `createdAt` / `updatedAt` / `deletedAt` 공통 필드 |

**인증 없이 접근 가능한 경로**: 회원가입·이메일인증·로그인·토큰갱신, Swagger. 그 외 모든 API는 JWT 필요.

---

## 에러 코드

| 접두 | 영역 | 예시 |
|------|------|------|
| `C` | 공통 | `C001` 예상치 못한 서버 에러, `C002` 바인딩 에러 |
| `S` | 인증/보안 | `S002` 인증 필요, `S004` JWT 만료, `S005` JWT 오류 |
| `U` | 회원 | `U001` 이메일 중복, `U003` 로그인 실패, `U004` 이메일 미인증, `U006` 학교 이메일 아님 |
| `T` | 토큰 | `T001` 리프레시 토큰 없음, `T004` 인증코드 만료, `T005` 인증코드 불일치 |
| `R` | 예약 | `R001` 예약 없음, `R002` 강의실 없음, `R003` 본인 예약 아님 (외 시간충돌·인원·연장 등) |

---

## 참고 (아직 미구현/확장 여지)

- **체크인 API 부재**: 상태 흐름상 `RESERVED → CHECKED_IN`(QR+GPS)이 있으나, 이를 수행하는 엔드포인트는 아직 없습니다. (`Room`의 위경도·반경·`qrToken`은 이 기능을 위한 준비된 필드)
- **메일 발송**: `MAIL_USERNAME`, `MAIL_APP_PASSWORD`(Gmail 앱 비밀번호)를 `.env`에 채워야 이메일 인증코드가 실제 발송됩니다.
