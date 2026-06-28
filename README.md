# Game-Shop

Spring Boot 기반의 게임 판매 및 라이브러리 관리 백엔드 프로젝트입니다.
사용자는 게임을 조회하고 구매할 수 있으며, 구매한 게임은 개인 라이브러리에서 확인할 수 있습니다.
관리자는 게임을 등록하고 상태를 관리할 수 있으며, QueryDSL 기반 검색 기능을 통해 게임 목록을 조건별로 조회할 수 있습니다.

---

## 1. 프로젝트 소개

`Game-Shop`은 Steam과 같은 게임 판매 플랫폼의 핵심 기능을 백엔드 중심으로 구현한 프로젝트입니다.

주요 목표는 다음과 같습니다.

* JWT 기반 회원 인증 구현
* 사용자와 관리자 권한 분리
* 게임 등록, 조회, 검색 기능 구현
* 게임 구매 및 라이브러리 기능 구현
* QueryDSL을 활용한 동적 검색 조건 처리
* Spring Security 기반 API 접근 제어
* 환경별 설정 분리 및 민감 정보 관리 개선

---

## 2. 기술 스택

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* QueryDSL
* JWT
* Bean Validation

### Database

* H2 Database

### Build Tool

* Gradle

### Test / API

* Postman

### Version Control

* Git
* GitHub

---

## 3. 주요 기능

### 회원 기능

* 회원가입
* 로그인
* JWT Access Token 발급
* 인증 사용자 정보 기반 API 접근

### 게임 기능

* 게임 목록 조회
* 게임 상세 조회
* 게임 등록
* 게임 상태 관리
* 게임 검색

### 검색 기능

QueryDSL을 사용해 다양한 조건을 조합하여 게임을 검색할 수 있습니다.

검색 조건 예시:

* 키워드
* 장르
* 플랫폼
* 최소 가격
* 최대 가격
* 정렬 조건

검색 API는 `GET` 요청의 Query Parameter를 사용합니다.

```http
GET /games/search?keyword=elden&genre=RPG&platform=PC&minPrice=10000&maxPrice=70000
```

Postman에서는 Body가 아니라 `Params` 탭에 검색 조건을 입력해야 합니다.

### 구매 기능

* 로그인한 사용자만 게임 구매 가능
* 이미 구매한 게임 중복 구매 방지
* 구매 완료 시 사용자 라이브러리에 게임 추가

### 라이브러리 기능

* 로그인한 사용자의 보유 게임 목록 조회
* 사용자 개인 데이터이므로 인증 필수 API로 관리

### 관리자 기능

* 관리자 권한으로 게임 등록
* 관리자 권한으로 게임 상태 관리
* 일반 사용자와 관리자 API 접근 권한 분리

---

## 4. 인증 및 인가 구조

본 프로젝트는 Spring Security와 JWT를 사용하여 인증 및 인가를 처리합니다.

로그인에 성공하면 서버는 Access Token을 발급합니다.
클라이언트는 이후 인증이 필요한 API 요청 시 아래와 같이 토큰을 전달합니다.

```http
Authorization: Bearer {accessToken}
```

API 접근 권한은 다음과 같이 분리했습니다.

| API                      | 접근 권한    |
| ------------------------ | -------- |
| `/auth/**`               | 전체 접근 가능 |
| `GET /games/**`          | 전체 접근 가능 |
| `POST /games/*/purchase` | 로그인 사용자  |
| `/library/**`            | 로그인 사용자  |
| `/admin/games/**`        | 관리자      |

게임 목록과 상세 조회는 비회원도 접근 가능하지만, 구매와 라이브러리 조회는 사용자 개인 데이터와 연결되므로 인증된 사용자만 접근할 수 있도록 설정했습니다.

---

## 5. API 명세

### Auth

| Method | URL            | 설명           | 인증  |
| ------ | -------------- | ------------ | --- |
| POST   | `/auth/signup` | 회원가입         | 불필요 |
| POST   | `/auth/login`  | 로그인 및 JWT 발급 | 불필요 |

### Game

| Method | URL                        | 설명       | 인증  |
| ------ | -------------------------- | -------- | --- |
| GET    | `/games`                   | 게임 목록 조회 | 불필요 |
| GET    | `/games/{gameId}`          | 게임 상세 조회 | 불필요 |
| GET    | `/games/search`            | 게임 검색    | 불필요 |
| POST   | `/games/{gameId}/purchase` | 게임 구매    | 필요  |

### Library

| Method | URL        | 설명         | 인증 |
| ------ | ---------- | ---------- | -- |
| GET    | `/library` | 내 라이브러리 조회 | 필요 |

### Admin Game

| Method | URL                            | 설명       | 인증  |
| ------ | ------------------------------ | -------- | --- |
| POST   | `/admin/games/create`          | 게임 등록    | 관리자 |
| PATCH  | `/admin/games/{gameId}/status` | 게임 상태 변경 | 관리자 |

---

## 6. 요청 검증

게임 등록 요청에는 Bean Validation을 적용했습니다.

검증 조건은 다음과 같습니다.

| 필드            | 검증 조건        |
| ------------- | ------------ |
| `title`       | 빈 값 불가       |
| `price`       | 필수 입력, 0원 이상 |
| `platform`    | 빈 값 불가       |
| `genre`       | 필수 입력        |
| `description` | 빈 값 불가       |

예시 요청:

```json
{
  "title": "Elden Ring",
  "price": 64800,
  "platform": "PC",
  "genre": "RPG",
  "description": "오픈월드 액션 RPG"
}
```

검증 실패 예시:

```json
{
  "title": "",
  "price": -1000,
  "platform": "PC",
  "genre": "RPG",
  "description": "오픈월드 액션 RPG"
}
```

위와 같은 요청은 `400 Bad Request`로 처리됩니다.

---

## 7. 환경 설정

환경별 설정을 분리했습니다.

```text
src/main/resources/
├─ application.yml
├─ application-local.yml
└─ application-prod.yml
```

### application.yml

공통 설정과 기본 profile을 관리합니다.

```yml
spring:
  profiles:
    default: local

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:3600000}
```

### application-local.yml

로컬 개발 환경 설정입니다.
H2 DB, H2 Console, SQL 로그, 로컬 JWT Secret 등을 관리합니다.

해당 파일은 민감 정보가 포함될 수 있으므로 Git에 포함하지 않습니다.

```gitignore
src/main/resources/application-local.yml
```

### application-prod.yml

운영 환경 설정 예시입니다.
운영 환경에서는 DB 접속 정보와 JWT Secret을 환경 변수로 주입받도록 구성했습니다.

```yml
spring:
  datasource:
    url: ${DB_URL}
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: false
        show_sql: false

  h2:
    console:
      enabled: false

jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:3600000}
```

---

## 8. 실행 방법

### 1. 프로젝트 클론

```bash
git clone https://github.com/lh991117/Game-Shop.git
cd Game-Shop
```

### 2. 로컬 설정 파일 생성

`src/main/resources/application-local.yml` 파일을 생성합니다.

예시:

```yml
spring:
  datasource:
    url: jdbc:h2:mem:gameshop;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create
    properties:
      hibernate:
        format_sql: true
        show_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

jwt:
  secret: local-dev-jwt-secret-key-for-game-shop-project-change-me-2026
  access-token-expiration: 3600000
```

### 3. 서버 실행

```bash
./gradlew bootRun
```

Windows 환경에서는 다음 명령어를 사용할 수 있습니다.

```bash
gradlew bootRun
```

### 4. H2 Console 접속

```text
http://localhost:8080/h2-console
```

---

## 9. 트러블슈팅

### 9.1 구매 및 라이브러리 API 인증 누락 문제 해결

기존 보안 설정에서는 `/games/**` 전체를 `permitAll()`로 열어두고 있었습니다.

```java
.requestMatchers("/games/**").permitAll()
```

이 설정은 게임 목록 조회뿐만 아니라 `POST /games/{gameId}/purchase` 구매 API까지 공개 접근 대상으로 포함할 수 있었습니다.

이를 해결하기 위해 HTTP Method 기준으로 접근 권한을 분리했습니다.

```java
.requestMatchers(HttpMethod.GET, "/games/**").permitAll()
.requestMatchers(HttpMethod.POST, "/games/*/purchase").authenticated()
.requestMatchers("/library/**").authenticated()
```

이를 통해 게임 조회 API는 비회원 접근을 허용하고, 구매 및 라이브러리 API는 로그인 사용자만 접근할 수 있도록 개선했습니다.

### 9.2 JWT Secret 설정 파일 노출 문제 해결

기존에는 `application.yml`에 JWT Secret 값이 직접 작성되어 있었습니다.

```yml
jwt:
  secret: 직접 작성된 secret 값
```

공개 GitHub 저장소에 JWT Secret이 포함되는 것은 보안상 위험하다고 판단하여, 환경 변수 기반 설정으로 변경했습니다.

```yml
jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:3600000}
```

로컬 개발 환경에서는 Git에 포함하지 않는 `application-local.yml`을 통해 secret을 관리하도록 분리했습니다.

---

## 10. 개선 사항

### Profile 기반 환경 설정 분리

기존에는 `application.yml`에 H2 DB, JPA ddl-auto, SQL 로그, H2 Console 등 로컬 개발 전용 설정이 함께 포함되어 있었습니다.

이를 개선하여 공통 설정은 `application.yml`, 로컬 개발 설정은 `application-local.yml`, 운영 환경 설정은 `application-prod.yml`로 분리했습니다.

이를 통해 운영 환경에서 개발용 설정이 잘못 적용될 위험을 줄이고, 환경별 설정 관리가 명확해졌습니다.

### 게임 생성 요청 검증 추가

게임 등록 요청 DTO에 Bean Validation을 적용했습니다.

* `title`: 빈 값 불가
* `price`: 필수 입력, 0원 이상
* `platform`: 빈 값 불가
* `genre`: 필수 입력
* `description`: 빈 값 불가

Postman을 통해 정상 요청과 실패 요청을 검증했으며, 잘못된 요청은 `400 Bad Request`로 처리되는 것을 확인했습니다.

---

## 11. 향후 개선 계획

* API 응답 형식 통일
* 예외 응답 구조 개선
* 테스트 코드 추가
* 구매 기능 동시성 제어 개선
* 검색 조건 및 정렬 기능 고도화
* README에 ERD 및 API 테스트 예시 추가
