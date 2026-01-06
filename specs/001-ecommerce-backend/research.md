# Research: E-commerce Backend System

**Branch**: `001-ecommerce-backend` | **Date**: 2026-01-06

## Overview

Technical Context에서 식별된 기술적 결정 사항과 best practices를 정리한다.

---

## 1. 인증/인가 전략

### Decision: JWT 기반 토큰 인증

**Rationale**:
- Stateless 아키텍처에 적합
- 수평 확장이 용이 (세션 저장소 불필요)
- Spring Security와 통합 용이

**Alternatives Considered**:
- Session 기반 인증: 세션 저장소 관리 필요, 수평 확장 시 Redis 등 추가 인프라 필요
- OAuth2: 외부 인증 제공자 의존, 현재 요구사항에 과도함

**Implementation**:
```
- Access Token: 30분 유효
- Refresh Token: 7일 유효
- Access Token 만료 시 Refresh Token으로 재발급
- Refresh Token은 DB에 저장하여 로그아웃/강제 만료 지원
```

---

## 2. 비밀번호 암호화

### Decision: BCrypt (Spring Security PasswordEncoder)

**Rationale**:
- 업계 표준 해시 알고리즘
- Salt 자동 생성
- Cost factor 조정으로 보안 수준 제어 가능

**Alternatives Considered**:
- Argon2: 더 최신이나 BCrypt로 충분, Spring 기본 지원
- PBKDF2: BCrypt보다 느리고 메모리 사용량 높음

**Implementation**:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // cost factor 12
}
```

---

## 3. 동시성 제어 전략

### 3.1 재고 차감: 낙관적 락 (@Version)

**Decision**: JPA @Version 기반 낙관적 락

**Rationale**:
- 대부분의 경우 충돌이 적음 (일반적인 주문 패턴)
- 락 대기 없이 성능 유지
- 충돌 시 OptimisticLockException 발생, 재시도 로직 구현

**Implementation**:
```java
@Entity
public class Inventory {
    @Version
    private Long version;

    private int availableQuantity;
    private int reservedQuantity;
}
```

**Fallback**: 동시성이 극단적으로 높은 경우(플래시 세일) 비관적 락 또는 분산 락 고려

### 3.2 쿠폰 발급: 비관적 락

**Decision**: SELECT FOR UPDATE (비관적 락)

**Rationale**:
- 선착순 발급은 정확한 수량 보장이 중요
- 동시 요청 시 순차 처리 필요

**Implementation**:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Coupon c WHERE c.id = :id")
Optional<Coupon> findByIdWithLock(@Param("id") Long id);
```

---

## 4. 멱등성 구현

### Decision: Idempotency Key + DB 저장

**Rationale**:
- 클라이언트가 생성한 UUID를 Idempotency Key로 사용
- 요청/응답을 DB에 저장하여 중복 요청 시 동일 응답 반환
- 네트워크 장애로 인한 중복 요청 방지

**Implementation**:
```java
@Entity
public class IdempotencyRecord {
    @Id
    private String idempotencyKey;
    private String requestHash;
    private String response;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // 24시간 후 만료
}
```

**Flow**:
1. 요청 수신 시 Idempotency Key로 기존 레코드 조회
2. 존재하면 저장된 응답 반환
3. 없으면 처리 후 레코드 저장

---

## 5. 주문번호 생성

### Decision: 날짜 + 시퀀스 (ORD-YYYYMMDD-NNNNNN)

**Rationale**:
- 가독성 (날짜 정보 포함)
- 정렬 용이
- 일별 시퀀스로 충돌 방지

**Implementation**:
```java
public String generateOrderNumber() {
    String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    long sequence = orderSequenceGenerator.next(date);
    return String.format("ORD-%s-%06d", date, sequence);
}
```

**Sequence Generation Options**:
- DB 시퀀스 (일별 리셋)
- Redis INCR (분산 환경)
- 단일 인스턴스: AtomicLong + 날짜별 리셋

---

## 6. 상태 전이 관리

### Decision: State Machine 패턴 적용

**Rationale**:
- 명확한 상태 전이 규칙 정의
- 허용되지 않은 전이 시 예외 발생
- 상태별 비즈니스 로직 캡슐화

**Implementation**:
```java
public enum OrderStatus {
    PENDING {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return target == PAID || target == CANCELLED;
        }
    },
    PAID {
        @Override
        public boolean canTransitionTo(OrderStatus target) {
            return target == PREPARING || target == CANCELLED;
        }
    },
    // ... 다른 상태들
}
```

---

## 7. Mock PG 설계

### Decision: Interface + Mock 구현

**Rationale**:
- 실제 PG 연동 없이 다양한 시나리오 테스트
- 성공/실패/타임아웃 시뮬레이션
- 추후 실제 PG 연동 시 구현체만 교체

**Implementation**:
```java
public interface PgClient {
    PgResponse approve(PaymentRequest request);
    PgResponse cancel(String transactionId);
    PgResponse inquiry(String transactionId);
}

@Component
@Profile("local")
public class MockPgClient implements PgClient {
    // 카드번호 끝자리로 시나리오 분기
    // 0000: 성공, 1111: 실패, 2222: 타임아웃
}
```

---

## 8. 예외 처리 체계

### Decision: ErrorCode enum + BusinessException

**Rationale**:
- 일관된 에러 코드 관리
- 다국어 메시지 지원 가능
- API 응답 형식 통일

**Implementation**:
```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Member
    MEMBER_NOT_FOUND("M001", "회원을 찾을 수 없습니다"),
    EMAIL_DUPLICATED("M002", "이미 사용 중인 이메일입니다"),
    ACCOUNT_LOCKED("M003", "계정이 잠금되었습니다"),

    // Order
    ORDER_NOT_FOUND("O001", "주문을 찾을 수 없습니다"),
    INVALID_ORDER_STATUS("O002", "유효하지 않은 주문 상태입니다"),

    // Inventory
    INSUFFICIENT_STOCK("I001", "재고가 부족합니다"),

    // Payment
    PAYMENT_FAILED("P001", "결제에 실패했습니다"),
    DUPLICATE_PAYMENT("P002", "이미 처리된 결제입니다"),

    // Coupon
    COUPON_NOT_AVAILABLE("C001", "사용할 수 없는 쿠폰입니다"),
    COUPON_EXPIRED("C002", "유효기간이 만료된 쿠폰입니다");

    private final String code;
    private final String message;
}
```

---

## 9. 페이징 처리

### Decision: Spring Data Pageable + Slice

**Rationale**:
- Spring Data JPA 기본 지원
- Slice: 다음 페이지 존재 여부만 확인 (COUNT 쿼리 제거로 성능 향상)
- Page: 전체 카운트 필요한 경우

**Implementation**:
```java
// 목록 조회 (Slice)
Slice<Product> findByStatus(ProductStatus status, Pageable pageable);

// 검색 결과 (Page - 총 건수 표시 필요)
Page<Product> searchByKeyword(String keyword, Pageable pageable);
```

---

## 10. 주문 자동 취소 (30분 타임아웃)

### Decision: Spring Scheduler + 배치 처리

**Rationale**:
- 단순하고 구현 용이
- 분산 환경에서는 ShedLock 활용

**Implementation**:
```java
@Scheduled(fixedRate = 60000) // 1분마다 실행
public void cancelExpiredOrders() {
    LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(30);
    List<Order> expiredOrders = orderRepository
        .findByStatusAndCreatedAtBefore(OrderStatus.PENDING, expirationTime);

    for (Order order : expiredOrders) {
        orderService.cancelOrder(order.getId(), "자동 취소: 결제 시간 초과");
    }
}
```

**Alternative** (추후 확장):
- 메시지 큐 (RabbitMQ/Kafka) + Delayed Message
- Redis TTL + Keyspace Notification

---

## Summary

| 영역 | 결정 | 핵심 이유 |
|------|------|-----------|
| 인증 | JWT | Stateless, 수평 확장 용이 |
| 비밀번호 | BCrypt | 업계 표준, Spring 기본 지원 |
| 재고 동시성 | 낙관적 락 | 충돌 빈도 낮음, 성능 우선 |
| 쿠폰 동시성 | 비관적 락 | 정확한 수량 보장 필수 |
| 멱등성 | Idempotency Key | 중복 결제 방지 |
| 주문번호 | 날짜+시퀀스 | 가독성, 정렬 용이 |
| 상태관리 | State Machine | 명확한 전이 규칙 |
| PG 연동 | Mock | 테스트 용이성 |
| 예외처리 | ErrorCode enum | 일관성, 관리 용이 |
| 자동취소 | Scheduler | 단순, 구현 용이 |
