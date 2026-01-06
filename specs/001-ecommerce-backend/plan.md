# Implementation Plan: E-commerce Backend System

**Branch**: `001-ecommerce-backend` | **Date**: 2026-01-06 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-ecommerce-backend/spec.md`

## Summary

E-commerce 플랫폼의 백엔드 시스템을 구축한다. 회원, 상품, 재고, 주문, 결제, 쿠폰 6개 도메인으로 구성되며, 멱등성(Idempotency), 가용성(Availability), 동시성(Concurrency) 3대 핵심 역량을 보장한다.

Java 25 + Spring Boot 4.0 + Spring Data JPA 기반으로 Layered Architecture + DDD 도메인형 디렉토리 구조를 채택한다.

## Technical Context

**Language/Version**: Java 25
**Primary Dependencies**: Spring Boot 4.0, Spring Data JPA, Spring Security
**Storage**: MySQL (prod), H2 (test)
**Testing**: JUnit 5, AssertJ, Mockito
**Target Platform**: Linux server (Docker 컨테이너 환경)
**Project Type**: Single project (Backend API only)
**Performance Goals**: 상품 검색 1초 이내, 결제 처리 3초 이내
**Constraints**: 동시 주문 100건 재고 정합성 100%, 중복 결제 0%
**Scale/Scope**: 일일 10,000 주문 처리 가능

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 원칙 | 준수 여부 | 구현 방안 |
|------|-----------|-----------|
| **2.1 멱등성** | PASS | 결제/주문 API에 Idempotency Key 적용 |
| **2.2 가용성** | PASS | PG 장애 시 UNKNOWN 상태 처리, 재시도/동기화 로직 |
| **2.3 동시성** | PASS | 재고 차감에 낙관적 락(@Version), 쿠폰 발급에 비관적 락 |
| **3.1 Layered + DDD** | PASS | 도메인별 패키지 구조 (api/application/dao/domain/dto/exception) |
| **3.2 계층별 책임** | PASS | api(검증), application(트랜잭션), domain(비즈니스), dao(데이터) |
| **3.3 의존성 방향** | PASS | domain은 다른 계층에 의존하지 않음 |
| **4.1 테스트 필수** | PASS | given/when/then 형식, @DisplayName 한글 설명 |
| **4.2 원자적 커밋** | PASS | 기능 + 테스트를 하나의 커밋 단위로 |
| **4.3 예외 처리** | PASS | BusinessException + ErrorCode enum |
| **5.1 트랜잭션 경계** | PASS | application 계층에서 @Transactional 관리 |
| **5.2 상태 변경 추적** | PASS | 주문/결제/재고 상태 이력 기록 |
| **6.1 RESTful 설계** | PASS | 자원 중심 URI, HTTP 메서드 의미 준수 |
| **6.3 API 버전 관리** | PASS | /api/v1/... 형식 |
| **7.1 쿼리 최적화** | PASS | Fetch Join, BatchSize, readOnly 트랜잭션 |
| **8.1 인증/인가** | PASS | JWT 토큰 기반 인증, 본인 데이터만 접근 |
| **8.2 민감 정보** | PASS | 비밀번호 암호화, PG 토큰 활용 |

**Gate Result**: ALL PASS - Phase 0 진행 가능

## Project Structure

### Documentation (this feature)

```text
specs/001-ecommerce-backend/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (OpenAPI specs)
│   ├── member-api.yaml
│   ├── product-api.yaml
│   ├── order-api.yaml
│   ├── payment-api.yaml
│   └── coupon-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
src/main/java/com/ecommerce/
├── domain/
│   ├── member/
│   │   ├── api/              # MemberController
│   │   ├── application/      # MemberService
│   │   ├── dao/              # MemberRepository
│   │   ├── domain/           # Member, Address, MemberGrade
│   │   ├── dto/              # MemberRequest, MemberResponse
│   │   └── exception/        # MemberException
│   ├── product/
│   │   ├── api/              # ProductController, CategoryController
│   │   ├── application/      # ProductService, CategoryService
│   │   ├── dao/              # ProductRepository, CategoryRepository
│   │   ├── domain/           # Product, ProductOption, Category, ProductStatus
│   │   ├── dto/              # ProductRequest, ProductResponse
│   │   └── exception/        # ProductException
│   ├── inventory/
│   │   ├── api/              # InventoryController (Admin)
│   │   ├── application/      # InventoryService
│   │   ├── dao/              # InventoryRepository, InventoryHistoryRepository
│   │   ├── domain/           # Inventory, InventoryHistory, InventoryEventType
│   │   ├── dto/              # InventoryRequest, InventoryResponse
│   │   └── exception/        # InventoryException
│   ├── order/
│   │   ├── api/              # OrderController
│   │   ├── application/      # OrderService
│   │   ├── dao/              # OrderRepository, OrderItemRepository
│   │   ├── domain/           # Order, OrderItem, OrderStatus
│   │   ├── dto/              # OrderRequest, OrderResponse
│   │   └── exception/        # OrderException
│   ├── payment/
│   │   ├── api/              # PaymentController
│   │   ├── application/      # PaymentService
│   │   ├── dao/              # PaymentRepository, PaymentHistoryRepository
│   │   ├── domain/           # Payment, PaymentHistory, PaymentStatus, PaymentMethod
│   │   ├── dto/              # PaymentRequest, PaymentResponse
│   │   └── exception/        # PaymentException
│   ├── coupon/
│   │   ├── api/              # CouponController
│   │   ├── application/      # CouponService
│   │   ├── dao/              # CouponRepository, MemberCouponRepository
│   │   ├── domain/           # Coupon, MemberCoupon, CouponType, CouponStatus
│   │   ├── dto/              # CouponRequest, CouponResponse
│   │   └── exception/        # CouponException
│   └── model/                # 공통 Embeddable, BaseEntity
│       ├── Money.java
│       ├── BaseEntity.java
│       └── BaseTimeEntity.java
├── global/
│   ├── common/               # 공통 Request/Response
│   │   ├── ApiResponse.java
│   │   ├── PageRequest.java
│   │   └── PageResponse.java
│   ├── config/               # Spring 설정
│   │   ├── JpaConfig.java
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── error/                # 예외 핸들링
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   ├── ErrorResponse.java
│   │   └── GlobalExceptionHandler.java
│   ├── security/             # JWT 인증
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetails.java
│   └── util/
│       └── IdempotencyKeyValidator.java
└── infra/
    └── pg/                   # Mock PG 연동
        ├── MockPgClient.java
        └── PgResponse.java

src/main/resources/
├── application.yml
├── application-local.yml
└── application-prod.yml

src/test/java/com/ecommerce/
├── domain/
│   ├── member/
│   ├── product/
│   ├── inventory/
│   ├── order/
│   ├── payment/
│   └── coupon/
└── integration/
```

**Structure Decision**: Backend API Only (Single project). 도메인별 패키지 구조로 6개 도메인(member, product, inventory, order, payment, coupon)을 독립적으로 구성.

## Complexity Tracking

> Constitution Check ALL PASS - 위반 사항 없음
