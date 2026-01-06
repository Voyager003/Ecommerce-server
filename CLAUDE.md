# E-commerce Server

## WHAT - 프로젝트 개요

E-commerce 결제 시스템 백엔드 서버. 안전하고 빠른 결제 처리를 목표로 하며, 트랜잭션 신뢰성과 데이터 일관성을 보장한다.

### 기술 스택
- Java 25 + Spring Boot 4.0
- Spring Data JPA + MySQL (prod) / H2 (test)
- Gradle

### 핵심 역량
- **멱등성**: 동일 요청의 중복 처리 방지
- **가용성**: 시스템 안정성 확보
- **동시성**: 동시 요청 처리 시 데이터 정합성 보장

---

## WHY - 아키텍처 설계 의도

**Layered Architecture + DDD (도메인형 디렉토리 구조)** 채택

```
src/main/java/com/ecommerce/
├── domain/          # 도메인별 패키지
│   ├── order/
│   ├── payment/
│   ├── product/
│   ├── member/
│   └── model/       # 공통 Embeddable, Enum
├── global/          # 전역 설정 및 공통 객체
│   ├── common/      # 공통 Request/Response
│   ├── config/      # Spring 설정
│   ├── error/       # 예외 핸들링
│   └── util/
└── infra/           # 외부 서비스 연동 (SMS, Email 등)
```

### 도메인 패키지 구조
| 디렉토리 | 역할 |
|----------|------|
| `api` | REST Controller |
| `application` | 서비스 계층 (트랜잭션 처리) |
| `dao` | Repository 및 조회 전용 구현체 |
| `domain` | Entity, VO, Enum |
| `dto` | Request/Response 객체 |
| `exception` | 도메인 예외 |

---

## HOW - 개발 가이드

### 빌드 및 실행
```bash
# 빌드
./gradlew build

# 로컬 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 테스트
./gradlew test
```

### 환경 프로파일
- `local`: 로컬 개발 환경
- `prod`: 프로덕션 환경

### 커밋 규칙

#### 원자적 커밋 (Atomic Commit)
하나의 커밋은 **하나의 완결된 작업 단위**를 의미한다.

| 작업 유형 | 커밋 단위 |
|----------|----------|
| 기능 추가 | 새로운 기능 코드 + 테스트 코드 성공 |
| 리팩토링 | 변경된 코드 + 테스트 코드 성공 |
| 버그 수정 | 수정된 코드 + 테스트 코드 성공 |

- 테스트가 실패한 상태로 커밋하지 않는다
- 하나의 커밋에 여러 기능을 섞지 않는다

#### 커밋 메시지 컨벤션
```
타입(도메인명): 한글로 작업 내용 설명
```

| 타입 | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 (기능 변경 없음) |
| `test` | 테스트 코드 추가/수정 |
| `docs` | 문서 수정 |
| `chore` | 빌드, 설정 등 기타 작업 |

- 예: `feat(Payment): 결제 승인 로직 및 테스트 코드 추가`
- 예: `refactor(Order): 주문 생성 로직 가독성 개선`

### 테스트 작성 규칙
```java
@Test
@DisplayName("한글로 테스트 설명")
void 메서드명_상황_기대결과() {
/*
* given : 테스트 준비
* when : 실행
* then : 검증
*/
}
```

---

## 문서 우선순위

```
1순위: Claude 시스템 프롬프트 (Anthropic 기본 지시)
↓
2순위: CLAUDE.md (프로젝트 전역 규칙)
↓
3순위: spec-kit (특정 작업 명세)
↓
4순위: 사용자의 실시간 지시
```

| 우선순위 | 대상 | 역할 |
|:--------:|------|------|
| 1 | 시스템 프롬프트 | Claude 기본 행동 규칙 |
| 2 | CLAUDE.md | 프로젝트 전체 적용 규칙 |
| 3 | spec-kit | 특정 기능 상세 명세 |
| 4 | 실시간 지시 | 현재 대화 요청 |

**충돌 시**: spec-kit은 CLAUDE.md 규칙을 **위반할 수 없음**. CLAUDE.md 규칙 내에서 상세화만 가능.

---

## 참고 문서
- `docs/spec-kit/`: 비즈니스 로직 명세서
- 구현 시 해당 spec 파일 참조
- spec-kit은 CLAUDE.md 규칙을 따르며, 상세 구현만 정의

## Recent Changes
- 001-ecommerce-backend: Added Java 25 + Spring Boot 4.0, Spring Data JPA, Spring Security
