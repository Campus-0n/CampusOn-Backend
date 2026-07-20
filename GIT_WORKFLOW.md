# Git 작업 흐름 — 커밋 → 내 Fork Push → 원본에 PR

CampusOn 백엔드 기여 절차입니다. **Fork(내 저장소)에 push한 뒤, 원본 저장소에 Pull Request(PR)** 를 넣는 방식입니다.

## 현재 저장소 구성

| 이름 | 저장소 | 역할 |
|------|--------|------|
| `origin` | `donghyun-03/CampusOn-Backend` | **내 Fork** (여기로 push) |
| `upstream` | `Campus-0n/CampusOn-Backend` | **원본** (여기로 PR, 기본 브랜치 `main`) |

- 작업 브랜치: `feature/reservation`
- `.env`는 `.gitignore`에 등록되어 **커밋되지 않음(안전)**. `build/`, `.idea` 등도 무시됨.

---

## 0. 커밋 전 점검

```bash
# 무엇이 올라갈지 확인 (?? = 새 파일, M = 수정)
git status

# .env가 목록에 없어야 정상 (있으면 절대 커밋 금지)
git status | grep .env      # 아무것도 안 나오면 OK

# 가능하면 빌드가 통과하는지 먼저 확인
./gradlew build             # 또는 docker-compose up -d --build
```

이번에 올라갈 주요 변경: `reservation/`·`room/` 도메인 전체, 설정(`JpaAuditingConfig`, `SchedulingConfig`), 예약 규칙 수정(`ReservationPolicy`/`ExceptionType`/`SecurityConfig`/`docker-compose.yml`), 그리고 문서(`PROJECT_STRUCTURE.md`, `RESERVATION.md`).

---

## 1. 커밋하기 (md 파일 포함)

문서 md 파일도 그냥 일반 파일처럼 `git add`하면 커밋에 포함됩니다.

```bash
# (권장) 변경 전체를 스테이징 — .env 등 무시 대상은 자동 제외
git add .

# 또는 골라서 스테이징 (md 문서 포함해서)
git add src/main/java/com/campuson/backend/reservation \
        src/main/java/com/campuson/backend/room \
        src/main/java/com/campuson/backend/global \
        docker-compose.yml \
        PROJECT_STRUCTURE.md RESERVATION.md GIT_WORKFLOW.md

# 스테이징 결과 확인 (초록색 = 커밋될 것)
git status

# 커밋
git commit -m "feat(reservation): 예약 도메인 추가 및 예약/연장 시간 규칙 적용

- 예약 2시간 단위, 연장 1시간 단위 1회 제한
- 운영시간(09:00~22:00)·종료 :50 검증 추가
- 문서(PROJECT_STRUCTURE, RESERVATION) 추가"
```

> 문서를 코드와 분리하고 싶으면 `docs/` 폴더로 옮겨서 커밋해도 됩니다:
> `mkdir docs && git mv PROJECT_STRUCTURE.md RESERVATION.md docs/`

---

## 2. 내 Fork(origin)에 Push

```bash
# 최초 push는 -u 로 추적 브랜치 설정 (이후부터는 git push 만 하면 됨)
git push -u origin feature/reservation
```

이제 `github.com/donghyun-03/CampusOn-Backend`의 `feature/reservation` 브랜치에 올라갑니다.

---

## 3. (권장) 원본 최신 내용 반영

PR 전에 원본(`upstream/main`)의 최신 변경을 내 브랜치에 맞춰두면 충돌을 줄일 수 있습니다.

```bash
git fetch upstream
git rebase upstream/main        # 또는 git merge upstream/main
# 충돌이 나면 파일 수정 후: git add <파일> && git rebase --continue
git push --force-with-lease origin feature/reservation   # rebase 했으면 강제 push 필요
```

> `merge`를 썼다면 강제 push 없이 `git push`만 하면 됩니다. rebase는 히스토리를 다시 써서 `--force-with-lease`가 필요합니다.

---

## 4. 원본 저장소에 PR 생성

1. 브라우저에서 **내 Fork** 페이지 접속: `https://github.com/donghyun-03/CampusOn-Backend`
2. push 직후 뜨는 노란 배너의 **"Compare & pull request"** 클릭
   (안 보이면: 상단 **Pull requests → New pull request → compare across forks**)
3. PR 방향을 아래처럼 지정:

| 항목 | 값 |
|------|-----|
| **base repository** | `Campus-0n/CampusOn-Backend` |
| **base** | `main` |
| **head repository** | `donghyun-03/CampusOn-Backend` |
| **compare** | `feature/reservation` |

4. **제목**과 **설명** 작성 (아래 템플릿 참고) → **Create pull request**

**PR 설명 템플릿 예시**

```markdown
## 작업 내용
- 예약(reservation) 도메인 구현: 생성/목록/상세/연장/종료/취소
- 강의실 가용 시간 조회 API
- 예약 규칙: 2시간 단위 예약 / 1시간 단위 1회 연장 / 운영시간·종료 :50 검증

## 변경 파일
- reservation, room 패키지 전체
- ReservationPolicy, ExceptionType(R010~R012), SecurityConfig, docker-compose.yml
- 문서: PROJECT_STRUCTURE.md, RESERVATION.md

## 확인 사항
- [ ] 로컬 빌드/실행 확인 (`docker-compose up -d --build`)
- [ ] .env 등 민감정보 미포함
```

---

## 5. 리뷰 반영 (PR 올린 뒤 수정이 필요할 때)

같은 브랜치에 커밋해서 push하면 **PR에 자동으로 추가**됩니다. 새 PR을 다시 만들 필요 없습니다.

```bash
# 코드 수정 후
git add .
git commit -m "fix: 리뷰 반영 - ○○ 수정"
git push origin feature/reservation
```

---

## 자주 겪는 상황

| 상황 | 해결 |
|------|------|
| `.env`가 커밋되려 함 | `.gitignore`에 `.env` 있는지 확인. 이미 추적 중이면 `git rm --cached .env` |
| push 거부(rejected) | 원본이 앞서감 → `git fetch upstream && git rebase upstream/main` 후 `--force-with-lease` push |
| 잘못된 브랜치에서 작업함 | `git switch -c feature/reservation` 로 새 브랜치 만들어 옮기기 |
| 커밋 메시지 수정 | 아직 push 전이면 `git commit --amend` |

---

## 요약 (한 번에 보기)

```bash
git status                                   # 0) 점검 (.env 없어야 함)
git add .                                    # 1) 스테이징 (md 포함)
git commit -m "feat(reservation): ..."       #    커밋
git push -u origin feature/reservation       # 2) 내 fork에 push
# 3) GitHub에서 upstream(Campus-0n) main 으로 PR 생성
```
