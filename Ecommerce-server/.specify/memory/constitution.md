<!--
SYNC IMPACT REPORT
==================
Version Change: N/A → 1.0.0 (Initial Constitution)
Bump Rationale: MAJOR - First constitution establishment

Added Sections:
- 1. 프로젝트 비전
- 2. 핵심 역량 (멱등성, 가용성, 동시성)
- 3. 아키텍처 원칙 (Layered + DDD)
- 4. 코드 품질 기준
- 5. 데이터 일관성 원칙
- 6. API 설계 원칙
- 7. 성능 원칙
- 8. 보안 원칙
- 9. 문서화 원칙
- 10. 금지 사항
- 부록: 기술 스택

Modified Principles: N/A (initial)
Removed Sections: N/A (initial)

Templates Requiring Updates:
- plan-template.md: ✅ Compatible (Constitution Check uses dynamic reference)
- spec-template.md: ✅ Compatible (Test-first aligned)
- tasks-template.md: ✅ Compatible (Phase structure aligned)

Follow-up TODOs: None
==================
-->

# E-commerce Project Constitution

> 이 문서는 E-commerce 프로젝트의 모든 개발 의사결정을 안내하는 핵심 원칙입니다.
> 모든 명세, 계획, 구현은 이 원칙을 준수해야 합니다.

---

## 1. 프로젝트 비전

**"고객이 안전하고 빠르게 결제를 완료할 수 있는 신뢰할 수 있는 E-commerce 시스템"**

우리는 트랜잭션의 신뢰성과 데이터의 일관성을 유지하면서도 응답 속도를 최적화하는 백엔드 시스템을 구축한다.

---

## 2. 핵심 역량 (Core Competencies)

모든 기능 구현 시 다음 세 가지 역량을 반드시 고려한다:

### 2.1 멱등성 (Idempotency)

- 동일한 요청을 여러 번 보내도 결과는 한 번 처리한 것과 동일해야 한다
- 결제, 주문 생성 등 상태 변경 API는 멱등키(Idempotency Key)를 활용한다
- 중복 요청으로 인한 데이터 불일치를 원천 차단한다

### 2.2 가용성 (Availability)

- 시스템은 장애 상황에서도 핵심 기능을 유지해야 한다
- 외부 서비스(PG사 등) 장애 시 적절한 폴백 전략을 갖춘다
- 예외 상황은 명확히 정의하고 복구 가능한 상태로 관리한다

### 2.3 동시성 (Concurrency)

- 동시 요청 시 데이터 정합성을 보장한다
- 재고 차감, 쿠폰 사용 등 경쟁 조건이 발생하는 로직은 락 전략을 명시한다
- 낙관적 락(`@Version`)을 기본으로 하되, 필요 시 비관적 락 또는 분산 락을 적용한다

---

## 3. 아키텍처 원칙

### 3.1 Layered Architecture + DDD

- 도메인별 패키지 구조를 채택한다
- 각 도메인은 독립적으로 동작하며, 도메인 간 의존성을 최소화한다
- 계층 간 책임을 명확히 분리한다

### 3.2 계층별 책임

| 계층 | 디렉토리 | 책임 |
|------|----------|------|
| Presentation | `api` | HTTP 요청/응답 처리, 입력 검증 |
| Application | `application` | 비즈니스 유스케이스 조합, 트랜잭션 관리 |
| Domain | `domain` | 핵심 비즈니스 로직, 도메인 규칙 |
| Infrastructure | `dao`, `infra` | 데이터 접근, 외부 시스템 연동 |

### 3.3 의존성 방향

```
api → application → domain ← dao/infra
```

- 도메인 계층은 다른 계층에 의존하지 않는다
- 인프라 계층이 도메인 인터페이스를 구현한다

---

## 4. 코드 품질 기준

### 4.1 테스트 필수

- 모든 기능은 테스트 코드와 함께 구현한다
- 테스트가 성공한 상태에서만 커밋한다
- 테스트 형식:

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

### 4.2 원자적 커밋

- 하나의 커밋 = 하나의 완결된 작업 단위
- 커밋 메시지: `타입(도메인명): 한글로 작업 내용 설명`
- 예: `feat(Payment): 결제 승인 로직 및 테스트 코드 추가`

### 4.3 예외 처리

- 비즈니스 예외는 `BusinessException`을 상속한다
- 모든 예외는 `ErrorCode` enum으로 관리한다
- 예외 메시지는 사용자와 개발자 모두 이해할 수 있도록 작성한다

---

## 5. 데이터 일관성 원칙

### 5.1 트랜잭션 경계

- 트랜잭션은 `application` 계층에서 관리한다
- 하나의 트랜잭션에서 여러 도메인을 수정하는 것을 최소화한다
- 도메인 간 정합성이 필요한 경우 이벤트 기반 처리를 고려한다

### 5.2 상태 변경 추적

- 주문, 결제 등 핵심 엔티티는 상태 변경 이력을 남긴다
- 상태 전이는 명확한 규칙을 따른다 (허용되지 않은 전이는 예외 발생)

### 5.3 데이터 검증

- 입력 검증: `api` 계층에서 Bean Validation 활용
- 비즈니스 검증: `domain` 계층에서 도메인 규칙 검증
- 중복 검증보다 적절한 계층에서 한 번 검증

---

## 6. API 설계 원칙

### 6.1 RESTful 설계

- 자원 중심의 URI 설계
- HTTP 메서드의 의미에 맞게 사용 (GET, POST, PUT, PATCH, DELETE)
- 적절한 HTTP 상태 코드 반환

### 6.2 응답 일관성

- 성공/실패 응답 형식을 통일한다
- 에러 응답에는 에러 코드, 메시지, 타임스탬프를 포함한다

### 6.3 버전 관리

- API 버전은 URI에 명시한다 (예: `/api/v1/...`)

---

## 7. 성능 원칙

### 7.1 쿼리 최적화

- N+1 문제를 방지한다 (Fetch Join, BatchSize 활용)
- 조회 전용 로직은 `@Transactional(readOnly = true)` 적용

### 7.2 응답 속도

- 불필요한 데이터 조회를 피한다
- 페이징 처리를 기본으로 한다

---

## 8. 보안 원칙

### 8.1 인증/인가

- 인증된 사용자만 주문, 결제 기능에 접근할 수 있다
- 본인 데이터만 조회/수정할 수 있도록 권한을 검증한다

### 8.2 민감 정보

- 비밀번호는 암호화하여 저장한다
- 결제 정보는 직접 저장하지 않고 PG사 토큰을 활용한다

---

## 9. 문서화 원칙

### 9.1 Spec-Kit 활용

- 새로운 기능은 `/speckit.specify` → `/speckit.plan` → `/speckit.tasks` 순서로 명세화한다
- 명세에는 WHAT(무엇을)과 WHY(왜)를 명확히 기술한다
- 기술 스택은 `/speckit.plan` 단계에서 결정한다

### 9.2 우선순위

```
1순위: Constitution (이 문서)
2순위: CLAUDE.md (프로젝트 전역 규칙)
3순위: spec-kit 명세 (기능별 상세)
```

---

## 10. 금지 사항

- 테스트 없이 커밋
- 하나의 커밋에 여러 기능 혼합
- 도메인 계층에서 인프라 직접 의존
- 트랜잭션 범위를 벗어난 지연 로딩
- 하드코딩된 설정값 (환경 변수 또는 설정 파일 활용)
- 검증 없는 외부 입력 사용

---

## 부록: 기술 스택

| 항목 | 선택 |
|------|------|
| Language | Java 25 |
| Framework | Spring Boot 4.0 |
| ORM | Spring Data JPA |
| Database | MySQL (prod), H2 (test) |
| Build Tool | Gradle |
| Architecture | Layered + DDD |

---

## Governance

이 Constitution은 프로젝트의 근간입니다. 모든 의사결정은 이 원칙에 부합해야 하며,
원칙과 충돌하는 구현은 허용되지 않습니다.

### 수정 절차

1. Constitution 수정은 문서화되어야 한다
2. 수정 시 버전을 증가시킨다 (MAJOR.MINOR.PATCH)
   - MAJOR: 원칙 삭제 또는 근본적 변경
   - MINOR: 새 원칙/섹션 추가
   - PATCH: 문구 수정, 명확화
3. 모든 PR은 Constitution 준수 여부를 검증해야 한다

**Version**: 1.0.0 | **Ratified**: 2026-01-06 | **Last Amended**: 2026-01-06
