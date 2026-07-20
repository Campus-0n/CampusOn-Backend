# 예약(Reservation) 기능 정리

CampusOn 백엔드의 **예약 도메인** 문서입니다.
강의실을 시간대별로 예약하고, 연장·종료·취소하며, 시간이 지난 예약은 스케줄러가 자동으로 상태를 바꿉니다.

- 베이스 경로: `/api/reservations` (예약), `/api/rooms` (가용 시간 조회)
- 인증: 모든 예약 API는 **JWT 필요** (`Authentication`에서 `userId` 추출)

---

## 파일 구성

```
reservation/
├── controller/
│   ├── ReservationController.java        # 예약 CRUD·상태변경 API
│   └── RoomAvailabilityController.java    # 강의실 가용 시간 조회 API
├── service/
│   ├── ReservationService.java           # 예약 핵심 로직
│   └── AvailabilityService.java          # 가용 시간대 계산
├── scheduler/
│   └── ReservationScheduler.java         # 60초마다 자동 상태 전환
├── ReservationPolicy.java                # 예약 정책 규칙·상수
├── entity/
│   ├── Reservation.java                  # 예약 엔티티
│   ├── ReservationStatus.java            # 상태 열거형
│   └── ReservationTab.java               # 목록 탭 ↔ 상태 매핑
├── repository/
│   └── ReservationRepository.java        # 조회 쿼리
└── dto/
    ├── request/  CreateReservationRequest, ExtendReservationRequest
    └── response/ CreateReservationResponse, MyReservationsResponse,
                  ReservationSummaryResponse, ReservationDetailResponse,
                  ExtendReservationResponse, StatusResponse, AvailabilityResponse
```

관련: `room/entity/Room.java`, `room/repository/RoomRepository.java` (예약 대상 강의실)

---

## API 목록

### 예약 `/api/reservations`

| 메서드 | 경로 | 기능 | 성공 응답 |
|--------|------|------|-----------|
| POST | `/api/reservations` | 예약 생성 | `201` `CreateReservationResponse` |
| GET | `/api/reservations/me` | 내 예약 목록 (탭·상태 필터) | `200` `MyReservationsResponse` |
| GET | `/api/reservations/{id}` | 예약 상세 | `200` `ReservationDetailResponse` |
| PATCH | `/api/reservations/{id}/extend` | 이용 시간 연장 | `200` `ExtendReservationResponse` |
| PATCH | `/api/reservations/{id}/end` | 이용 종료 | `200` `StatusResponse` |
| PATCH | `/api/reservations/{id}/cancel` | 예약 취소 | `200` `StatusResponse` |

`GET /me` 쿼리 파라미터: `tab`(`UPCOMING`/`IN_USE`/`PAST`), `status`(선택) — 둘 다 선택값.

### 강의실 가용 시간 `/api/rooms`

| 메서드 | 경로 | 기능 |
|--------|------|------|
| GET | `/api/rooms/{roomId}/availability?date=YYYY-MM-DD` | 해당 강의실의 날짜별 가용 시간대 |

---

## 요청 / 응답 DTO

**CreateReservationRequest** (예약 생성)

| 필드 | 타입 | 제약 |
|------|------|------|
| `roomId` | Long | 필수 |
| `reservationDate` | LocalDate `yyyy-MM-dd` | 필수 |
| `startTime` | LocalTime `HH:mm` | 필수 |
| `endTime` | LocalTime `HH:mm` | 필수 |
| `purpose` | String | 필수(공백 불가) |
| `headcount` | int | 양수 |

**ExtendReservationRequest**: `newEndTime`(LocalTime, 필수)

**주요 응답 필드**

- `CreateReservationResponse` — reservationId, roomId, status, 날짜/시작/종료, createdAt
- `ReservationSummaryResponse`(목록 아이템) — reservationId, roomName, building, floor, 날짜/시간, status, displayLabel, remainingMinutes, extensionCount *(탭별로 null 필드는 응답에서 제외)*
- `ReservationDetailResponse` — 위 정보 + `RoomSummary`(roomId, name, capacity), purpose, headcount, checkedInAt, remainingMinutes, nextReservationStartTime
- `ExtendReservationResponse` — reservationId, endTime, extensionCount
- `StatusResponse` — reservationId, status
- `AvailabilityResponse` — roomId, date, `slots[]`(startTime, endTime, available)

---

## 도메인 규칙 — `ReservationPolicy`

| 상수 | 값 | 의미 |
|------|-----|------|
| `OPERATING_START` / `OPERATING_END` | 09:00 / 22:00 | 운영 시간 |
| `END_BUFFER_MINUTES` | 10분 | 예약 종료 후 다음 예약을 막는 버퍼 |
| `CHECKIN_GRACE_MINUTES` | 10분 | 예약 시작시각 이후 이 시간까지 미체크인 시 NO_SHOW |
| `CHECKIN_PRE_ALLOW_MINUTES` | 10분 | 예약 시작시각 전 체크인 허용 시간 |
| `RESERVATION_UNIT_HOURS` | 2시간 | **예약 시간 단위** (정시 시작, 예: 09:00~10:50) |
| `EXTENSION_UNIT_HOURS` | 1시간 | **연장 시간 단위** |
| `MAX_EXTENSION_COUNT` | 1회 | 최대 연장 횟수 |
| `REQUIRED_END_MINUTE` | 50분 | 종료시각은 **무조건** 정시 50분 |

- `conflicts(...)` : 두 시간대가 **버퍼 포함** 겹치는지 판정
- `isWithinOperatingHours(...)` : 시작·종료가 09:00~22:00 안인지 검증
- `isValidEndMinute(...)` : 종료시각이 :50인지 검증
- `isValidReservationBlock(...)` : 정시(:00) 시작 + 2시간 단위인지 검증
- `isValidExtension(...)` : 연장이 1시간 단위인지 검증

### 예약·연장 시간 규칙 (검증)

**예약 생성**(`create`)은 아래를 모두 만족해야 합니다 (모두 서버에서 검증):

- **운영시간**: 시작·종료 모두 `09:00 ~ 22:00` 안 (`R010`)
- **종료 :50**: 종료시각은 무조건 정시 50분 (`R005`)
- **2시간 단위**: 정시(:00) 시작 + 종료는 2시간 뒤의 `:50` (예: `09:00~10:50`) (`R011`)
- 시간대 충돌 없음(버퍼 10분, `R004`), 인원 `1 ~ 정원`(`R006`)
- ⇒ 선택 가능한 시작시각: **09:00 ~ 20:00** (종료 최대 21:50)

**연장**(`extend`):

- **1시간 단위 · 1회만**: 새 종료시각 = 현재 종료 `+ 1시간` (`R012`), 최대 1회(`R007`)
- 체크인(CHECKED_IN) 상태에서만(`R008`), 운영시간(22:00) 초과 불가(`R010`), 다음 예약과 충돌 없음(`R004`)
- 예: `09:00~10:50` 예약 → 연장 시 `09:00~11:50`

---

## 상태 흐름 & 자동 처리

**상태(`ReservationStatus`)**

```
RESERVED ──체크인(QR+GPS)──▶ CHECKED_IN ──종료/시각도달──▶ COMPLETED
   │
   └ 미체크인(유예 초과) ──▶ NO_SHOW
RESERVED ──사용자 취소──▶ CANCELLED
```

**목록 탭(`ReservationTab`) ↔ 상태**

| 탭 | 포함 상태 |
|----|-----------|
| `UPCOMING` | RESERVED |
| `IN_USE` | CHECKED_IN |
| `PAST` | COMPLETED, CANCELLED, NO_SHOW |

**스케줄러(`ReservationScheduler`)** — `@Scheduled(fixedRate = 60_000)` 60초마다:

- `markNoShows` : RESERVED이면서 (시작시각 + 유예)를 지나도 미체크인 → **NO_SHOW**
- `markCompleted` : CHECKED_IN이면서 종료시각 도달 → **COMPLETED**

---

## 서비스 로직 요약 — `ReservationService`

| 메서드 | 동작 |
|--------|------|
| `create` | 강의실 조회 → 인원 검증 → **시간 규칙 검증**(`validateReservationTime`: 운영시간·:50·2시간 단위) → 충돌 검증(`validateNoConflict`) → 저장 |
| `getMyReservations` | 탭/상태로 필터해 내 예약 목록 구성 (탭별 표시 필드 계산) |
| `getDetail` | 예약 상세 + 남은 시간·다음 예약 시작시각 계산 |
| `extend` | 소유자 확인 → **1시간 단위·1회·운영시간·:50·충돌 검증** → 종료시각 +1시간 |
| `end` | 이용 종료 처리(상태 전환) |
| `cancel` | 예약 취소(상태 전환) |

공통 헬퍼 `getOwnedReservation`으로 **본인 예약인지(`validateOwner`)** 확인 후 처리합니다.
`AvailabilityService.getAvailability`는 운영시간을 슬롯으로 나눠 활성 예약과 겹치는지로 `available`을 계산합니다.

---

## 조회 쿼리 — `ReservationRepository`

| 메서드 | 용도 |
|--------|------|
| `findByUserIdOrderByReservationDateDescStartTimeDesc` | 내 예약 전체(최신순) |
| `findByUserIdAndStatusInOrderBy...` | 내 예약 중 특정 상태들(최신순) |
| `findActiveByRoomAndDate` (`@Query`) | 같은 강의실+날짜의 활성 예약(RESERVED/CHECKED_IN) — 충돌·다음예약 계산 |
| `findByStatus` | 스케줄러용, 특정 상태 전체 조회 |

---

## 관련 에러 코드

| 코드 | 의미 |
|------|------|
| `R001` | 예약을 찾을 수 없습니다 |
| `R002` | 강의실을 찾을 수 없습니다 |
| `R003` | 본인의 예약이 아닙니다 |
| `R004` | 해당 시간대에 이미 예약이 있습니다 (시간 충돌) |
| `R005` | 종료시각은 정시 50분 단위여야 합니다 |
| `R006` | 이용 인원이 올바르지 않습니다 |
| `R007` | 연장 가능 횟수를 초과했습니다 |
| `R008` | 현재 상태에서 할 수 없는 동작입니다 |
| `R009` | 시작시각이 종료시각보다 앞서야 합니다 |
| `R010` | 운영시간(09:00~22:00) 내에서만 예약할 수 있습니다 |
| `R011` | 예약은 2시간 단위여야 합니다 (정시 시작, 예: 09:00~10:50) |
| `R012` | 연장은 1시간 단위로만 가능합니다 |

---

## 참고

- **체크인 API는 아직 없음**: 상태 흐름상 `RESERVED → CHECKED_IN`(QR+GPS)이 예정돼 있으나 이를 수행하는 엔드포인트는 미구현입니다. (`Room`의 위경도·허용반경·`qrToken`이 이 기능을 위한 필드)
- 팀원(참여자) 추가/삭제 기능은 미사용으로 결정되어 제거되었습니다.
- **가용 시간 조회는 1시간 슬롯 단위**로 응답합니다. 예약은 2시간 단위이므로, 프론트에서 연속한 두 슬롯(예: 09:00·10:00)을 묶어 `09:00~10:50`으로 요청해야 합니다. (필요 시 `AvailabilityService`를 2시간 슬롯으로 조정 가능)

---

## 변경 이력

**예약·연장 시간 규칙 도입** (이번 변경)

- 예약을 **2시간 단위**(정시 시작, 예: `09:00~10:50`)로만 생성하도록 제한 — `create`에서 검증
- 연장을 **1시간 단위 · 1회**만 허용 (`09:00~10:50` → `09:00~11:50`)
- **운영시간(09:00~22:00)** 검증을 `create`·`extend`에 추가
- **종료시각 :50** 규칙을 `create`에도 적용 (기존엔 `extend`에만 있었음)
- 신규 에러코드 `R010`(운영시간), `R011`(2시간 단위), `R012`(연장 1시간 단위) 추가

변경 파일: `ReservationPolicy`(상수·검증 메서드), `ReservationService.create`(검증 호출·`validateReservationTime`), `Reservation.extend`(1시간 단위·운영시간 검증), `ExceptionType`(R010~R012)
