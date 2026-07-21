# Postman API 테스트 가이드

CampusOn 백엔드 API를 Postman으로 처음부터 끝까지 검증하는 순서입니다.

- **Base URL**: `http://localhost:8080`
- **응답 형식**: 성공 `{ "success": "true", "data": {...} }` / 실패 `{ "success": "false", "code": "R011", "msg": "..." }`
- **인증**: 로그인으로 받은 토큰을 헤더 `Authorization: Bearer <accessToken>` 에 넣어야 함 (예약/체크인 등)
- **가입 이메일**: `@kumoh.ac.kr` 도메인만
- ⚠️ **현재 버전은 회원가입 시 실제 메일 발송이 성공해야 합니다.** 메일 설정이 안 되어 있으면 아래 **1-B(메일 없이 테스트 유저 만들기)** 로 진행하세요.

---

## 0. 앱 실행 & 준비

```powershell
docker-compose up -d --build
docker logs -f campuson-backend    # "Started BackendApplication" 뜨면 준비 완료
```

> `docker-compose.yml`의 backend `TZ: Asia/Seoul` 확인 (체크인 시간창이 정확하려면 필수).

### 0-1. 테스트용 강의실 넣기 (필수)

```powershell
docker exec -i campuson-mysql mysql -uroot -pNk2907! campuson -e "INSERT INTO room (name, building, floor, room_number, capacity, latitude, longitude, allowed_radius_meters, qr_token) VALUES ('공학관 401','공학관','4','401',10,36.14,128.39,50,'TESTQR123');"
docker exec -i campuson-mysql mysql -uroot -pNk2907! campuson -e "SELECT id, name FROM room;"
```

---

## 1. 로그인 토큰 확보

### 1-A. 정식 흐름 (메일 발송이 되는 경우)

1. `POST {{baseUrl}}/auth/user/signup`
   ```json
   { "email": "tester@kumoh.ac.kr", "loginId": "tester1", "password": "test1234", "name": "테스터" }
   ```
2. 인증 코드 확인 — 메일함 또는 DB:
   ```powershell
   docker exec -i campuson-mysql mysql -uroot -pNk2907! campuson -e "SELECT c.code FROM confirmation_token c JOIN user u ON u.id=c.user_id ORDER BY c.id DESC LIMIT 1;"
   ```
3. `POST {{baseUrl}}/auth/user/verify-email`  `{ "email": "tester@kumoh.ac.kr", "code": "위 코드" }`
4. `POST {{baseUrl}}/auth/user/login`  `{ "loginId": "tester1", "password": "test1234" }` → `data.accessToken`

> 회원가입이 `500`이면 Gmail 앱 비밀번호(16자)가 유효하지 않은 것 → 메일 대신 **1-B**로.

### 1-B. 메일 없이 테스트 유저 만들기 (권장 – 검증용)

이메일 인증을 건너뛰고, **이미 인증된 유저**를 DB에 바로 넣습니다. 비밀번호는 `test1234` (bcrypt 해시 적용됨):

```powershell
docker exec -i campuson-mysql mysql -uroot -pNk2907! campuson -e "INSERT INTO user (email, login_id, password, name, role, email_verified) VALUES ('tester@kumoh.ac.kr','tester1','$2a$10$Mn9svYUI4la6Co6M4ugq9uKgE78tbtCgA6UA8LtIiGdGfNJtKxK6a','테스터','USER',1);"
```

그다음 바로 로그인:
- `POST {{baseUrl}}/auth/user/login`  `{ "loginId": "tester1", "password": "test1234" }` → `data.accessToken`

**토큰 자동 저장** (로그인 요청 Scripts → Post-response):
```javascript
const res = pm.response.json();
pm.environment.set("accessToken", res.data.accessToken);
pm.environment.set("refreshToken", res.data.refreshToken);
```

---

## 2. 공통 헤더 (예약/체크인 요청)

```
Authorization: Bearer {{accessToken}}
Content-Type: application/json
```

---

## 3. 예약 흐름

### 3-1. 가용 시간 조회 — `GET {{baseUrl}}/api/rooms/1/availability?date=2026-07-25`

### 3-2. 예약 생성 — `POST {{baseUrl}}/api/reservations`
```json
{ "roomId": 1, "reservationDate": "2026-07-25", "startTime": "09:00", "endTime": "10:50", "purpose": "스터디", "headcount": 4 }
```
- 규칙: 2시간 단위(정시 시작·:50 종료), 운영시간 09:00~22:00, 인원 ≤ 정원. 성공 시 `201` + `reservationId`.

| 바꿀 값 | 기대 | 
|---|---|
| `endTime:"10:30"` | R005 | 
| `endTime:"11:50"` | R011 | 
| `startTime:"08:00"` | R010 | 
| 같은 시간 재예약 | R004 | 

### 3-3. 내 예약 목록 — `GET {{baseUrl}}/api/reservations/me?tab=UPCOMING`
### 3-4. 예약 상세 — `GET {{baseUrl}}/api/reservations/1`

## 4. 상태 흐름

```
RESERVED ──취소──▶ CANCELLED
RESERVED ──체크인──▶ CHECKED_IN ──연장(+1h·1회)──▶ CHECKED_IN ──종료──▶ COMPLETED
```
`cancel`·`check-in`은 RESERVED에서만, `extend`·`end`는 CHECKED_IN에서만.

### 4-1. 취소 — `PATCH {{baseUrl}}/api/reservations/1/cancel`  → `CANCELLED`

### 4-2. 체크인 — `POST {{baseUrl}}/api/reservations/1/check-in`

시간창이 **시작 ±10분**이라 예약을 "지금·RESERVED"로 옮긴 뒤 10분 내 호출:
```powershell
docker exec -i campuson-mysql mysql -uroot -pNk2907! campuson -e "UPDATE reservation SET reservation_date=CURDATE(), start_time=CURTIME(), end_time=ADDTIME(CURTIME(),'01:50:00'), status='RESERVED', checked_in_at=NULL WHERE id=1;"
```
Body:
```json
{ "roomId": 1, "qrToken": "TESTQR123", "latitude": 36.14, "longitude": 128.39 }
```
- ✅ `CHECKED_IN`, `distanceMeters≈0` · 에러: QR 틀림 R014, 반경 밖 R015, 시간창 밖 R013

### 4-3. 연장 — `PATCH {{baseUrl}}/api/reservations/1/extend`
- 상세로 현재 `endTime` 확인 → `{ "newEndTime": "endTime + 1시간" }` · ✅ `extensionCount=1` · 2회째 R007

### 4-4. 종료 — `PATCH {{baseUrl}}/api/reservations/1/end`  → `COMPLETED`

---

## 5. 토큰 (선택)
- 재발급 `POST {{baseUrl}}/auth/token/refresh`  `{ "accessToken":"{{accessToken}}", "refreshToken":"{{refreshToken}}" }`
- 로그아웃 `POST {{baseUrl}}/auth/token/logout`  (Bearer 필요)

## 참고
- 강의실 생성 API가 없어 강의실은 DB INSERT로 준비.
- 체크인 시간창(±10분) 때문에 테스트 시 예약을 "지금"으로 옮기는 UPDATE 필요.
- 브라우저에서 클릭으로 검증하려면 같은 폴더의 **`api-tester.html`** 사용 (아래 HTML 검증 방법 참고).
