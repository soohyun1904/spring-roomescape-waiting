# 방 탈출 예약 시스템

## 실행 가이드

### 1. 토스페이먼츠 테스트 키 설정

[토스페이먼츠 개발자센터](https://developers.tosspayments.com/)에서 테스트 키를 발급받아 환경변수로 설정합니다.
테스트 키만 사용하므로 실제 출금은 발생하지 않습니다.
**시크릿 키는 절대 저장소에 커밋하지 않습니다** — `application.properties`는 `${TOSS_SECRET_KEY:}` 형태로 환경변수만 참조합니다.

```bash
export TOSS_SECRET_KEY=test_sk_xxxxxxxxxxxxxxxxxxxxxxxx
export TOSS_CLIENT_KEY=test_ck_xxxxxxxxxxxxxxxxxxxxxxxx
```

개인 키 발급 없이 바로 돌려보려면 토스 문서에 공개된 결제위젯용 테스트 키 쌍을 사용해도 됩니다
(문서 공개 키라 저장소에 적어도 무방하며, 이 키로 백엔드 ↔ 실제 토스 API 인증·승인 호출이 동작함을 확인했습니다):

```bash
export TOSS_CLIENT_KEY=test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm
export TOSS_SECRET_KEY=test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6
```

### 2. 백엔드 실행 — http://localhost:8080

```bash
./gradlew bootRun
```

(Docker를 쓰려면 위 환경변수를 설정한 뒤 `docker-compose up` — 백엔드만 기동됩니다.)

### 3. 프론트엔드 실행 — http://localhost:4173

저장소 상위 디렉토리의 `frontend/`(React + Vite)를 사용합니다.

**방법 A — 로컬 dev 서버 (개발 시):**

```bash
cd ../frontend
npm install
npm run dev
```

프론트 dev 서버가 `/api` 요청을 백엔드(`http://localhost:8080`)로 프록시합니다.

**방법 B — Docker (한 번에):**

```bash
TOSS_SECRET_KEY=... TOSS_CLIENT_KEY=... docker-compose up
```

프론트는 결제 UI가 포함된 `dalsu1904/spring-roomescape-member-front:latest` 이미지로 뜨고,
nginx가 `/api/*`를 backend 컨테이너로 프록시합니다. 프론트 코드를 수정했다면 이미지를 다시 빌드/푸시합니다:

```bash
cd ../frontend
docker build -t dalsu1904/spring-roomescape-member-front:latest .
docker push dalsu1904/spring-roomescape-member-front:latest
```

### 4. 결제 흐름 확인

1. http://localhost:4173 → 예약 페이지에서 이름/테마/날짜/시간을 선택해 예약 신청
2. 예약이 `결제 대기` 상태로 생성되고(서버가 orderId·금액 확정) 결제 페이지로 이동
3. 토스 결제위젯에서 결제 진행 — 테스트 환경이므로 실제 출금 없음
4. 인증 성공 → success 페이지가 백엔드 `POST /payments/confirm` 호출 → 예약이 `승인`으로 확정
5. 결제 실패/취소 시 fail 페이지가 정리 API를 호출해 결제 대기 예약을 정리

수동 테스트 팁:

- 테스트 환경이라도 **결제 수단의 인증창은 실물**입니다 — 카드 결제는 카드사 창(본인 인증), 네이버페이는 네이버 로그인이 필요합니다. 본인이 쓰기 편한 수단으로 진행하세요(출금은 되지 않음).
- 승인 완료 후 success 페이지에서 **새로고침**을 해보면 `ALREADY_PROCESSED_PAYMENT` 멱등 처리를 직접 확인할 수 있습니다(에러 대신 동일한 완료 화면).
- 결제창을 **X로 닫으면** fail 페이지로 이동해 결제 대기 예약이 정리되는 것을 확인할 수 있습니다(`PAY_PROCESS_CANCELED`, orderId 없음 → null 가드).

---

## 기능 목록

### 테마

- [x] 관리자가 테마를 추가/삭제할 수 있다.
- [x] 테마 내역은 이름, 설명, 썸네일 이미지 URL을 갖는다.
- [x] 예약 내역에는 테마 정보가 포함된다.

        공포:   11:00 12:00 13:00 … 17:00
        코미디: 11:00 12:00 13:00 … 17:00
        로맨스: 11:00 12:00            -> X 모두 같은 시간을 공유해야 함

### 예약

- [x] 사용자가 날짜와 테마를 선택하면 예약 가능한 시간 목록이 표시된다.
    - [x] 예약 가능한 시간이란 이전 예약 리스트에 동일한 날짜 + 테마의 APPROVED 예약이 없는 시간이다.
    - [x] 테마 예약이 더 이상 불가능해도 선택할 수 있다.
- [x] 예약할 때 이름을 입력받아야 하고, 예약 기록에 이름을 포함한다.
- [x] 테마가 다르다면 동일한 시간에 동일한 이름을 가진 예약을 할 수 있다.
- [x] 사용자는 자신의 이름으로 본인의 예약 목록을 조회할 수 있다.
- [x] 사용자는 본인의 예약 날짜/시간을 변경할 수 있다.
- [x] 지나간 날짜 및 시간에 대한 예약 생성 및 삭제는 불가능하다.
- [x] 같은 날짜+시간+테마에 이미 예약이 있으면 중복 예약을 거부한다.
- [x] 예약이 존재하는 시간을 삭제할 수 없다.
- [x] 유효하지 않은 입력값을 거부한다.
- [x] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
- [x] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
- [x] 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
- [x] 사용자는 본인의 대기를 취소할 수 있다.
- [x] APPROVED 예약이 취소/변경되면 첫 번째 대기자가 자동으로 APPROVED로 승급된다.

### 예약 조회

- [x] 사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
- [x] 대기에는 본인의 대기 순번도 함께 보여준다.

### 결제 (토스페이먼츠 연동)

- [x] 예약 생성 시 서버가 orderId(6~64자, 영숫자/`-`/`_`)를 생성하고 테마 가격으로 결제 금액을 확정해 주문을 저장한다.
- [x] 슬롯을 선점한 예약은 `결제 대기` 상태로 생성되고, 결제 승인이 성공해야 `승인`으로 확정된다.
- [x] 승인 요청 시 저장된 주문 금액과 요청 금액을 대조해 금액 위변조를 게이트웨이 호출 전에 차단한다.
- [x] 토스 에러 코드를 도메인 예외로 매핑해 상황별 상태코드/메시지로 응답한다(미정의 코드는 기본 예외 폴백).
- [x] 이미 승인된 결제의 중복 승인 요청(success 페이지 새로고침)은 성공과 동일하게 응답한다(멱등).
- [x] 결제 실패/취소 시 결제 대기 주문과 예약을 정리하고 첫 대기자를 승격한다(orderId 없는 취소도 안전).
- [x] `PaymentService`와 도메인 계층은 토스에 의존하지 않는다(포트 & 어댑터).

### 에러 응답

- [x] 서비스 정책 위반, 유효하지 않은 입력, 존재하지 않는 리소스 등에 대해 에러 응답을 반환한다.
- [x] 500(서버 에러)는 사용자에게 노출되지 않도록 한다.
- [x] 에러 응답 본문에는 다음의 정보를 담는다. (서버 - 프론트 응답)
    - [x] 발생 시간, 메시지(상황 설명, 발생 이유, 해결책을 포함)

### 인기 테마 조회

- [x] 최근 1주 동안 예약이 많았던 테마 상위 10개를 조회할 수 있다.
- [x] 조회 기준은 오늘을 제외한 이전 7일을 기준으로 한다.

### 구현 관련

- [x] `data.sql`에 기본 시간을 추가해둔다.

---

## API 명세

### 사용자 API

| 기능       | 메서드 / URL                   | 요청 본문                                      | 쿼리 파라미터                                            | 응답                                                                                                    |
|----------|-----------------------------|--------------------------------------------|----------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| 예약 목록 조회 | `GET /reservations`         | -                                          | `name` - optional                                  | `200 OK` <br> `{reservations: [{id, name, date, state, rank, time: {...}, theme: {...}}, ...]}`        |
| 예약 단건 조회 | `GET /reservations/{id}`    | -                                          | -                                                  | `200 OK` <br> `{id, name, date, state, rank, time: {...}, theme: {...}}`                               |
| 예약 추가    | `POST /reservations`        | `{name, date, timeId, themeId}`            | -                                                  | `201 Created` <br> `{id, name, date, state, rank, time: {...}, theme: {...}}`                          |
| 예약 취소    | `DELETE /reservations/{id}` | -                                          | `name`                                             | `204 No Content`                                                                                      |
| 예약 변경    | `PUT /reservations/{id}`    | `{name, date, timeId, themeId}`            | -                                                  | `200 OK` <br> `{id, name, date, state, rank, time: {...}, theme: {...}}`                               |
| 시간 목록 조회 | `GET /times`                | -                                          | -                                                  | `200 OK` <br> `{times: [{id, startAt}, ...]}`                                                         |
| 가용 시간 조회 | `GET /times/available`      | -                                          | `date`, `themeId`                                  | `200 OK` <br> `{times: [{id, startAt}, ...]}`                                                         |
| 테마 목록 조회 | `GET /themes`               | -                                          | -                                                  | `200 OK` <br> `{themes: [{id, name, description, thumbnailUrl}, ...]}`                                |
| 테마 단건 조회 | `GET /themes/{id}`          | -                                          | -                                                  | `200 OK` <br> `{id, name, description, thumbnailUrl}`                                                 |
| 인기 테마 조회 | `GET /themes/famous`        | -                                          | `days` - optional, `date` - optional, `limit` - optional | `200 OK` <br> `{themes: [{id, name, description, thumbnailUrl}, ...]}`                          |

### 어드민 API

| 기능    | 메서드 / URL                   | 요청 본문                              | 쿼리 파라미터 | 응답                                                              |
|-------|-----------------------------|------------------------------------|---------|-----------------------------------------------------------------|
| 시간 추가 | `POST /admin/times`         | `{startAt}`                        | -       | `201 Created` <br> `{id, startAt}`                              |
| 시간 삭제 | `DELETE /admin/times/{id}`  | -                                  | -       | `204 No Content`                                                |
| 테마 생성 | `POST /admin/themes`        | `{name, description, thumbnailUrl}` | -       | `201 Created` <br> `{id, name, description, thumbnailUrl}`      |
| 테마 삭제 | `DELETE /admin/themes/{id}` | -                                  | -       | `204 No Content`                                                ||

## 에러 응답 명세

### 상태 코드 분류 기준

| 상태 코드                      | 의미                  | 사용 예시                   |
|----------------------------|-----------------------|---------------------------|
| `400 Bad Request`          | 요청 형식 또는 필수값이 잘못됨   | 필수 필드 누락, 타입 불일치         |
| `401 Unauthorized`         | 요청 권한 없음             | 본인 예약이 아닌 예약 취소 시도       |
| `404 Not Found`            | 요청한 리소스가 존재하지 않음    | 존재하지 않는 예약 ID 조회         |
| `409 Conflict`             | 도메인 규칙 충돌            | 중복 예약, 참조 중인 시간/테마 삭제 시도 |
| `422 Unprocessable Entity` | 비즈니스 규칙 위반           | 과거 날짜 예약                 |

### 응답 본문 형식

Spring의 `ProblemDetail` (RFC 7807) 형식을 사용합니다.

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "에러 메시지",
  "instance": "/reservations/999"
}
```
