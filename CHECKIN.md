# 체크인(Check-in) 기능 정리

CampusOn 백엔드의 **체크인 도메인** 문서입니다. (기능③ QR + GPS 인증)
예약 시작 시점에 사용자가 강의실 QR을 스캔하고 현재 위치를 함께 보내면, 서버가 **QR·GPS·시간·소유권**을 검증해 예약을 `CHECKED_IN`(이용 중)으로 전환합니다.

- 베이스 경로: `/api/reservations` (예약 리소스 하위 경로를 쓰지만 **예약 모듈과 분리된 별도 패키지**)
- 인증: **JWT 필요** (`Authentication`에서 `userId` 추출)
- 예약 도메인과의 관계: 예약 조회·상태전이는 `ReservationRepository`·`Reservation` 엔티티의 공개 API를 통해 사용 (예약 모듈 파일은 수정하지 않음)

---

## 파일 구성

```
checkin/
├── controller/
│   └── CheckInController.java        # POST /api/reservations/{id}/check-in
├── service/
│   └── CheckInService.java           # 4단계 검증 + 상태 전이
└── dto/
    ├── request/  CheckInRequest       # roomId, qrToken, latitude, longitude
    └── response/ CheckInResponse      # reservationId, status, checkedInAt, distanceMeters
```

공용 유틸/전역:
- `global/util/GeoUtil.java` — 위경도 거리 계산(Haversine)
- `global/exception/ExceptionType.java` — 체크인 에러 `R013`~`R015` 추가
- `global/exception/BusinessException.java` · `GlobalExceptionHandler.java` — 실패 응답에 **동적 메시지**(실제 거리) 노출 지원

관련: `reservation/entity/Reservation.java`(`checkIn()`·`validateOwner()`), `reservation/ReservationPolicy.java`(체크인 시간창·기본 반경), `room/entity/Room.java`(위경도·허용반경·`qrToken`)

---

## API

### 체크인 `POST /api/reservations/{id}/check-in`

**요청 바디 `CheckInRequest`**

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `roomId` | Long | 필수 | 스캔한 QR의 강의실 id. 예약의 `room_id` 와 일치해야 함 |
| `qrToken` | String | 필수(공백 불가) | 강의실 QR에서 읽은 토큰 |
| `latitude` | Double | 필수 | 현재 위도 |
| `longitude` | Double | 필수 | 현재 경도 |

```json
{
  "roomId": 3,
  "qrToken": "ROOM3-8f2a1c...",
  "latitude": 37.564321,
  "longitude": 126.998765
}
```

**성공 응답 `CheckInResponse`** — `reservationId`, `status`(=`CHECKED_IN`), `checkedInAt`, `distanceMeters`(강의실과의 실제 거리 m)

```json
{
  "success": "true",
  "data": {
    "reservationId": 12,
    "status": "CHECKED_IN",
    "checkedInAt": "2026-07-18T14:03:10",
    "distanceMeters": 12.4
  }
}
```

**실패 예 (반경 밖)** — `code` `R015`, 메시지에 실제 거리 포함

```json
{
  "success": "false",
  "code": "R015",
  "msg": "강의실 반경(50m) 밖입니다. 현재 거리 82.0m."
}
```

> 응답 봉투(`success`/`code`/`data`·`msg`)는 이 백엔드의 **공통 응답 포맷**(`ResponseBody`)을 그대로 따릅니다.

---

## 검증 로직 — `CheckInService.checkIn`

명세 순서대로 4가지 조건을 **모두** 통과해야 `RESERVED → CHECKED_IN` 으로 전환됩니다.

| 순서 | 검증 | 실패 코드 |
|------|------|-----------|
| 1 | **소유권** — 요청자가 예약자 본인인지 (`reservation.validateOwner`) | `R003` `NOT_RESERVATION_OWNER` |
| 2 | **체크인 시간창** — `[시작 −10분, 시작 +10분]` 안인지 (`ReservationPolicy.isWithinCheckInWindow`) | `R013` `CHECKIN_TIME_WINDOW` |
| 3 | **QR 일치** — `roomId == 예약.room_id` **및** `qrToken == Room.qrToken` | `R014` `CHECKIN_INVALID_QR` |
| 4 | **GPS 반경** — 강의실 좌표와의 거리 ≤ 허용 반경 | `R015` `CHECKIN_OUT_OF_RANGE` |

- 거리 계산: `GeoUtil.distanceMeters`(Haversine), 소수 첫째자리 반올림.
- 허용 반경: `Room.allowedRadiusMeters` 사용, 값이 없으면 `ReservationPolicy.DEFAULT_CHECKIN_RADIUS_METERS`(기본 50m).
- 상태 전이 후 검증: `Reservation.checkIn()` 이 `RESERVED` 가 아니면 `R008`(`INVALID_STATUS`) — 이미 체크인/노쇼/취소된 예약 방어.
시간창을 넘겨 미체크인 상태로 남은 예약은 예약 도메인의 스케줄러가 `NO_SHOW` 로 전환합니다.

### 강의실 '이용 중' 갱신 — 가용 여부는 어떻게 판정되나

명세의 "해당 강의실 상태 DB를 '이용 중'으로 갱신"은 이 시스템에서 **Room 에 상태 컬럼을 두는 방식이 아니라, 예약 상태로부터 파생(derive)** 하는 방식으로 구현되어 있습니다. 순서대로 보면:

1. **가용 여부 판정의 입력** — 강의실 가용 시간 조회(`AvailabilityService.getAvailability`)는 먼저 `ReservationRepository.findActiveByRoomAndDate` 로 해당 강의실·날짜의 **활성 예약**만 가져옵니다. 여기서 '활성'은 `RESERVED` **와** `CHECKED_IN` 두 상태입니다(`ACTIVE_STATUSES`). 즉 아직 시작 안 한 예약(`RESERVED`)이든 지금 이용 중인 예약(`CHECKED_IN`)이든 모두 그 시간대를 점유한 것으로 봅니다.

2. **슬롯별 available 계산** — 운영시간(09:00~22:00)을 1시간 슬롯으로 나눈 뒤, 각 슬롯이 활성 예약 중 하나라도 `ReservationPolicy.conflicts`(버퍼 10분 포함)로 겹치면 `available = false` 로 표시합니다. 겹치는 활성 예약이 없어야 `true`.

3. **체크인이 하는 일** — 체크인은 예약을 `RESERVED → CHECKED_IN` 으로 바꿀 뿐입니다. 두 상태 모두 `ACTIVE_STATUSES` 에 포함되므로, 체크인 전후로 **가용 판정 결과는 동일하게 '점유'** 로 유지됩니다. 다시 말해 체크인 순간 강의실이 "비어 있음 → 이용 중"으로 바뀌는 게 아니라, 예약이 잡힌 시점부터 이미 그 시간대는 막혀 있고, 체크인은 그 예약을 '실제 사용 중(CHECKED_IN)'으로 승격시키는 상태 표시입니다.

4. **결론** — 그래서 별도의 Room 상태 필드나 추가 UPDATE 없이 **`checkIn()` 상태 전이만으로 '이용 중' 갱신이 완료**됩니다. 프론트에서 '이용 중' 뱃지가 필요하면 예약의 `status == CHECKED_IN` 을, 강의실이 지금 점유됐는지는 위 가용성 응답의 `available` 을 그대로 쓰면 됩니다.

> 참고: 향후 정식 Room 병합 시 굳이 물리적 '이용중' 플래그를 두고 싶다면, 파생 방식과 이중 관리가 되지 않도록 어느 한쪽을 단일 출처(source of truth)로 정해야 합니다. 현재 단일 출처는 **예약 상태**입니다.

---

## 관련 에러 코드

| 코드 | enum | 의미 |
|------|------|------|
| `R003` | `NOT_RESERVATION_OWNER` | 본인의 예약이 아닙니다 |
| `R013` | `CHECKIN_TIME_WINDOW` | 체크인 가능 시간이 아닙니다 |
| `R014` | `CHECKIN_INVALID_QR` | 예약한 강의실의 QR이 아닙니다 |
| `R015` | `CHECKIN_OUT_OF_RANGE` | 강의실 반경 밖입니다 (메시지에 실제 거리 포함) |

---

## 확인 필요 (프론트/기획 합의)

- **요청 필드명**: 명세 JSON 예시·로직 설명은 `roomId`, Request Body 표는 `classroomId` 로 불일치. 기존 코드(`CreateReservationRequest.roomId`)·JSON 예시에 맞춰 **`roomId`** 로 구현. 표가 맞다면 `CheckInRequest` 필드명만 교체.
- **실패 응답의 거리**: 명세는 `data:{distanceMeters}` 로 거리를 내려주지만, 공통 응답 포맷 유지를 위해 **메시지 문자열**에 거리를 포함(`"현재 거리 82.0m."`). 프론트가 실패 `data` 를 파싱한다면 조정 필요.
