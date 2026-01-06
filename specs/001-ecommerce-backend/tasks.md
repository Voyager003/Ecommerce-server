# Tasks: E-commerce Backend System

**Input**: 설계 문서 `/specs/001-ecommerce-backend/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**테스트**: Constitution에 명시된 테스트 필수 원칙에 따라 각 기능에 테스트 태스크 포함

**구성**: User Story별로 그룹화하여 독립적 구현 및 테스트 가능

## 형식: `[ID] [P?] [Story] 설명`

- **[P]**: 병렬 실행 가능 (파일이 다르고 의존성 없음)
- **[Story]**: 해당 태스크가 속한 User Story (예: US1, US2, US3)
- 설명에 정확한 파일 경로 포함

## 경로 규칙

- **기본 경로**: `src/main/java/com/ecommerce/`
- **테스트 경로**: `src/test/java/com/ecommerce/`
- **리소스**: `src/main/resources/`

---

## Phase 1: 프로젝트 초기 설정

**목적**: 프로젝트 구조 생성 및 기본 설정

- [x] T001 Spring Boot 4.0 프로젝트 생성 및 build.gradle 의존성 설정
- [x] T002 [P] 프로젝트 디렉토리 구조 생성 (domain/, global/, infra/)
- [x] T003 [P] application.yml 환경별 설정 파일 작성 (local, prod)
- [x] T004 [P] JpaConfig 설정 클래스 생성 `global/config/JpaConfig.java`
- [x] T005 [P] WebConfig 설정 클래스 생성 `global/config/WebConfig.java`

---

## Phase 2: 공통 인프라 (Foundational)

**목적**: 모든 User Story에서 사용하는 핵심 인프라 - 이 단계 완료 전 User Story 작업 불가

**⚠️ CRITICAL**: 이 단계가 완료되어야 User Story 구현 시작 가능

### 공통 엔티티 및 유틸리티

- [ ] T006 [P] BaseTimeEntity 생성 (createdAt, updatedAt 자동 관리) `domain/model/BaseTimeEntity.java`
- [ ] T007 [P] Money 값 객체 생성 `domain/model/Money.java`
- [ ] T008 [P] ApiResponse 공통 응답 클래스 생성 `global/common/ApiResponse.java`
- [ ] T009 [P] PageRequest/PageResponse 페이징 클래스 생성 `global/common/PageRequest.java`, `global/common/PageResponse.java`

### 예외 처리 체계

- [ ] T010 [P] ErrorCode enum 정의 (회원, 상품, 주문, 결제, 쿠폰 에러 코드) `global/error/ErrorCode.java`
- [ ] T011 [P] BusinessException 예외 클래스 생성 `global/error/BusinessException.java`
- [ ] T012 [P] ErrorResponse 응답 클래스 생성 `global/error/ErrorResponse.java`
- [ ] T013 GlobalExceptionHandler 구현 `global/error/GlobalExceptionHandler.java`

### JWT 인증 인프라

- [ ] T014 [P] JwtTokenProvider 구현 (토큰 생성/검증) `global/security/JwtTokenProvider.java`
- [ ] T015 [P] CustomUserDetails 구현 `global/security/CustomUserDetails.java`
- [ ] T016 JwtAuthenticationFilter 구현 `global/security/JwtAuthenticationFilter.java`
- [ ] T017 SecurityConfig 설정 (BCrypt, 인증 필터 체인) `global/config/SecurityConfig.java`

### 멱등성 인프라

- [ ] T018 [P] IdempotencyRecord 엔티티 생성 `global/idempotency/IdempotencyRecord.java`
- [ ] T019 [P] IdempotencyRepository 생성 `global/idempotency/IdempotencyRepository.java`
- [ ] T020 IdempotencyService 구현 `global/idempotency/IdempotencyService.java`

### Mock PG 클라이언트

- [ ] T021 [P] PgClient 인터페이스 정의 `infra/pg/PgClient.java`
- [ ] T022 [P] PgResponse 응답 클래스 생성 `infra/pg/PgResponse.java`
- [ ] T023 MockPgClient 구현 (성공/실패/타임아웃 시나리오) `infra/pg/MockPgClient.java`

**체크포인트**: 공통 인프라 완료 - User Story 구현 시작 가능

---

## Phase 3: User Story 1 - 회원 가입 및 인증 (Priority: P1) 🎯 MVP

**목표**: 고객이 이메일과 비밀번호로 회원 가입을 하고, 로그인하여 서비스를 이용

**독립 테스트**: 회원 가입 후 로그인하여 토큰을 발급받고, 본인 정보를 조회할 수 있으면 성공

### 테스트 (US1)

- [ ] T024 [P] [US1] MemberService 단위 테스트 작성 `test/domain/member/application/MemberServiceTest.java`
- [ ] T025 [P] [US1] AuthController 통합 테스트 작성 `test/domain/member/api/AuthControllerTest.java`
- [ ] T026 [P] [US1] 로그인 실패 잠금 시나리오 테스트 작성 `test/domain/member/application/LoginLockTest.java`

### 엔티티 및 Repository (US1)

- [ ] T027 [P] [US1] MemberStatus enum 생성 (ACTIVE, LOCKED, WITHDRAWN) `domain/member/domain/MemberStatus.java`
- [ ] T028 [P] [US1] MemberGrade enum 생성 (BRONZE, SILVER, GOLD, PLATINUM) `domain/member/domain/MemberGrade.java`
- [ ] T029 [P] [US1] Member 엔티티 생성 `domain/member/domain/Member.java`
- [ ] T030 [P] [US1] Address 엔티티 생성 `domain/member/domain/Address.java`
- [ ] T031 [P] [US1] RefreshToken 엔티티 생성 `domain/member/domain/RefreshToken.java`
- [ ] T032 [P] [US1] MemberRepository 생성 `domain/member/dao/MemberRepository.java`
- [ ] T033 [P] [US1] AddressRepository 생성 `domain/member/dao/AddressRepository.java`
- [ ] T034 [P] [US1] RefreshTokenRepository 생성 `domain/member/dao/RefreshTokenRepository.java`

### DTO (US1)

- [ ] T035 [P] [US1] SignupRequest 생성 (유효성 검증 포함) `domain/member/dto/SignupRequest.java`
- [ ] T036 [P] [US1] LoginRequest 생성 `domain/member/dto/LoginRequest.java`
- [ ] T037 [P] [US1] TokenResponse 생성 `domain/member/dto/TokenResponse.java`
- [ ] T038 [P] [US1] MemberResponse 생성 `domain/member/dto/MemberResponse.java`
- [ ] T039 [P] [US1] AddressRequest/AddressResponse 생성 `domain/member/dto/AddressRequest.java`, `domain/member/dto/AddressResponse.java`

### 예외 (US1)

- [ ] T040 [P] [US1] MemberException 클래스 생성 `domain/member/exception/MemberException.java`

### Service (US1)

- [ ] T041 [US1] MemberService 구현 (회원가입, 정보조회, 수정, 탈퇴) `domain/member/application/MemberService.java`
- [ ] T042 [US1] AuthService 구현 (로그인, 토큰 갱신, 로그아웃, 계정 잠금) `domain/member/application/AuthService.java`
- [ ] T043 [US1] AddressService 구현 (배송지 CRUD, 최대 5개 검증) `domain/member/application/AddressService.java`

### Controller (US1)

- [ ] T044 [US1] AuthController 구현 (가입, 로그인, 토큰 갱신, 로그아웃) `domain/member/api/AuthController.java`
- [ ] T045 [US1] MemberController 구현 (내 정보 조회/수정, 비밀번호 변경, 탈퇴) `domain/member/api/MemberController.java`
- [ ] T046 [US1] AddressController 구현 (배송지 CRUD) `domain/member/api/AddressController.java`

**체크포인트**: User Story 1 완료 - 회원 가입/로그인/토큰 발급 독립 테스트 가능

---

## Phase 4: User Story 2 - 상품 조회 및 탐색 (Priority: P1)

**목표**: 고객이 판매 중인 상품을 검색하고, 카테고리별로 필터링하며, 상세 정보를 확인

**독립 테스트**: 상품 목록 조회 후 특정 상품의 상세 정보(옵션, 재고 상태)를 확인할 수 있으면 성공

### 테스트 (US2)

- [ ] T047 [P] [US2] ProductService 단위 테스트 작성 `test/domain/product/application/ProductServiceTest.java`
- [ ] T048 [P] [US2] ProductController 통합 테스트 작성 `test/domain/product/api/ProductControllerTest.java`

### 엔티티 및 Repository (US2)

- [ ] T049 [P] [US2] ProductStatus enum 생성 (DRAFT, ACTIVE, INACTIVE, DELETED) `domain/product/domain/ProductStatus.java`
- [ ] T050 [P] [US2] Category 엔티티 생성 (Self Reference 계층 구조) `domain/product/domain/Category.java`
- [ ] T051 [P] [US2] Product 엔티티 생성 `domain/product/domain/Product.java`
- [ ] T052 [P] [US2] ProductOption 엔티티 생성 `domain/product/domain/ProductOption.java`
- [ ] T053 [P] [US2] CategoryRepository 생성 `domain/product/dao/CategoryRepository.java`
- [ ] T054 [P] [US2] ProductRepository 생성 (검색, 필터링, 정렬 쿼리) `domain/product/dao/ProductRepository.java`
- [ ] T055 [P] [US2] ProductOptionRepository 생성 `domain/product/dao/ProductOptionRepository.java`

### DTO (US2)

- [ ] T056 [P] [US2] ProductResponse, ProductDetailResponse 생성 `domain/product/dto/ProductResponse.java`, `domain/product/dto/ProductDetailResponse.java`
- [ ] T057 [P] [US2] ProductOptionResponse 생성 `domain/product/dto/ProductOptionResponse.java`
- [ ] T058 [P] [US2] CategoryResponse 생성 `domain/product/dto/CategoryResponse.java`
- [ ] T059 [P] [US2] ProductSearchRequest 생성 (키워드, 카테고리, 가격 범위, 정렬) `domain/product/dto/ProductSearchRequest.java`

### 예외 (US2)

- [ ] T060 [P] [US2] ProductException 클래스 생성 `domain/product/exception/ProductException.java`

### Service (US2)

- [ ] T061 [US2] CategoryService 구현 (카테고리 조회) `domain/product/application/CategoryService.java`
- [ ] T062 [US2] ProductService 구현 (목록, 상세, 검색, 필터링) `domain/product/application/ProductService.java`

### Controller (US2)

- [ ] T063 [US2] CategoryController 구현 `domain/product/api/CategoryController.java`
- [ ] T064 [US2] ProductController 구현 (목록, 상세, 검색) `domain/product/api/ProductController.java`

**체크포인트**: User Story 2 완료 - 상품 목록/상세 조회 독립 테스트 가능

---

## Phase 5: User Story 3 - 주문 및 결제 (Priority: P1)

**목표**: 고객이 상품을 선택하여 주문하고, 결제를 완료하여 구매를 확정

**독립 테스트**: 상품 선택 → 주문 생성 → 결제 완료 → 주문 상태 PAID 확인 흐름이 성공하면 완료

### 테스트 (US3)

- [ ] T065 [P] [US3] OrderService 단위 테스트 작성 `test/domain/order/application/OrderServiceTest.java`
- [ ] T066 [P] [US3] PaymentService 단위 테스트 작성 `test/domain/payment/application/PaymentServiceTest.java`
- [ ] T067 [P] [US3] 주문-결제 통합 테스트 작성 `test/integration/OrderPaymentIntegrationTest.java`
- [ ] T068 [P] [US3] 멱등성 테스트 작성 (동일 멱등키 중복 요청) `test/domain/payment/application/IdempotencyTest.java`

### 주문 엔티티 및 Repository (US3)

- [ ] T069 [P] [US3] OrderStatus enum 생성 (State Machine 패턴 적용) `domain/order/domain/OrderStatus.java`
- [ ] T070 [P] [US3] Order 엔티티 생성 `domain/order/domain/Order.java`
- [ ] T071 [P] [US3] OrderItem 엔티티 생성 `domain/order/domain/OrderItem.java`
- [ ] T072 [P] [US3] OrderStatusHistory 엔티티 생성 `domain/order/domain/OrderStatusHistory.java`
- [ ] T073 [P] [US3] OrderRepository 생성 `domain/order/dao/OrderRepository.java`
- [ ] T074 [P] [US3] OrderItemRepository 생성 `domain/order/dao/OrderItemRepository.java`
- [ ] T075 [P] [US3] OrderStatusHistoryRepository 생성 `domain/order/dao/OrderStatusHistoryRepository.java`

### 결제 엔티티 및 Repository (US3)

- [ ] T076 [P] [US3] PaymentMethod enum 생성 (CREDIT_CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT) `domain/payment/domain/PaymentMethod.java`
- [ ] T077 [P] [US3] PaymentStatus enum 생성 (State Machine 패턴 적용) `domain/payment/domain/PaymentStatus.java`
- [ ] T078 [P] [US3] Payment 엔티티 생성 `domain/payment/domain/Payment.java`
- [ ] T079 [P] [US3] PaymentHistory 엔티티 생성 `domain/payment/domain/PaymentHistory.java`
- [ ] T080 [P] [US3] PaymentRepository 생성 `domain/payment/dao/PaymentRepository.java`
- [ ] T081 [P] [US3] PaymentHistoryRepository 생성 `domain/payment/dao/PaymentHistoryRepository.java`

### 주문 DTO (US3)

- [ ] T082 [P] [US3] OrderCreateRequest 생성 `domain/order/dto/OrderCreateRequest.java`
- [ ] T083 [P] [US3] OrderItemRequest 생성 `domain/order/dto/OrderItemRequest.java`
- [ ] T084 [P] [US3] OrderResponse, OrderDetailResponse 생성 `domain/order/dto/OrderResponse.java`, `domain/order/dto/OrderDetailResponse.java`
- [ ] T085 [P] [US3] OrderCancelRequest 생성 `domain/order/dto/OrderCancelRequest.java`

### 결제 DTO (US3)

- [ ] T086 [P] [US3] PaymentRequest 생성 (멱등키 포함) `domain/payment/dto/PaymentRequest.java`
- [ ] T087 [P] [US3] PaymentResponse 생성 `domain/payment/dto/PaymentResponse.java`
- [ ] T088 [P] [US3] PaymentCancelRequest 생성 `domain/payment/dto/PaymentCancelRequest.java`

### 예외 (US3)

- [ ] T089 [P] [US3] OrderException 클래스 생성 `domain/order/exception/OrderException.java`
- [ ] T090 [P] [US3] PaymentException 클래스 생성 `domain/payment/exception/PaymentException.java`

### Service (US3)

- [ ] T091 [US3] OrderNumberGenerator 구현 (ORD-YYYYMMDD-NNNNNN 형식) `domain/order/application/OrderNumberGenerator.java`
- [ ] T092 [US3] OrderService 구현 (생성, 조회, 취소, 상태 변경) `domain/order/application/OrderService.java`
- [ ] T093 [US3] PaymentService 구현 (결제 요청, 취소, 멱등성 처리) `domain/payment/application/PaymentService.java`
- [ ] T094 [US3] OrderCancelScheduler 구현 (30분 미결제 자동 취소) `domain/order/application/OrderCancelScheduler.java`

### Controller (US3)

- [ ] T095 [US3] OrderController 구현 (생성, 조회, 취소, 확정) `domain/order/api/OrderController.java`
- [ ] T096 [US3] PaymentController 구현 (결제 요청, 조회, 취소, 동기화) `domain/payment/api/PaymentController.java`

**체크포인트**: User Story 3 완료 - 주문 생성 → 결제 → 상태 변경 독립 테스트 가능

---

## Phase 6: User Story 4 - 재고 관리 (Priority: P2)

**목표**: 시스템이 상품 재고를 정확하게 관리하여 주문 시 재고 부족 문제를 방지하고, 동시성 보장

**독립 테스트**: 동시에 100명이 재고 1개 상품을 주문했을 때 정확히 1명만 성공하면 완료

### 테스트 (US4)

- [ ] T097 [P] [US4] InventoryService 단위 테스트 작성 `test/domain/inventory/application/InventoryServiceTest.java`
- [ ] T098 [P] [US4] 동시성 테스트 작성 (100명 동시 주문) `test/domain/inventory/application/InventoryConcurrencyTest.java`

### 엔티티 및 Repository (US4)

- [ ] T099 [P] [US4] InventoryEventType enum 생성 (INCOMING, RESERVED, CONFIRMED, RELEASED, RESTORED) `domain/inventory/domain/InventoryEventType.java`
- [ ] T100 [P] [US4] Inventory 엔티티 생성 (@Version 낙관적 락 적용) `domain/inventory/domain/Inventory.java`
- [ ] T101 [P] [US4] InventoryHistory 엔티티 생성 `domain/inventory/domain/InventoryHistory.java`
- [ ] T102 [P] [US4] InventoryRepository 생성 `domain/inventory/dao/InventoryRepository.java`
- [ ] T103 [P] [US4] InventoryHistoryRepository 생성 `domain/inventory/dao/InventoryHistoryRepository.java`

### DTO (US4)

- [ ] T104 [P] [US4] InventoryResponse 생성 `domain/inventory/dto/InventoryResponse.java`
- [ ] T105 [P] [US4] InventoryHistoryResponse 생성 `domain/inventory/dto/InventoryHistoryResponse.java`

### 예외 (US4)

- [ ] T106 [P] [US4] InventoryException 클래스 생성 `domain/inventory/exception/InventoryException.java`

### Service (US4)

- [ ] T107 [US4] InventoryService 구현 (예약, 확정, 복원, 낙관적 락 재시도) `domain/inventory/application/InventoryService.java`

### Controller (US4)

- [ ] T108 [US4] InventoryController 구현 (Admin 재고 조회) `domain/inventory/api/InventoryController.java`

### 통합 (US4)

- [ ] T109 [US4] OrderService에 InventoryService 연동 (주문 생성 시 재고 예약) `domain/order/application/OrderService.java`
- [ ] T110 [US4] PaymentService에 InventoryService 연동 (결제 완료 시 재고 확정) `domain/payment/application/PaymentService.java`

**체크포인트**: User Story 4 완료 - 재고 예약/확정/복원 및 동시성 독립 테스트 가능

---

## Phase 7: User Story 5 - 쿠폰 적용 (Priority: P2)

**목표**: 고객이 보유한 쿠폰을 주문 시 적용하여 할인 혜택을 받을 수 있음

**독립 테스트**: 쿠폰 적용 후 할인된 금액으로 결제가 완료되면 성공

### 테스트 (US5)

- [ ] T111 [P] [US5] CouponService 단위 테스트 작성 `test/domain/coupon/application/CouponServiceTest.java`
- [ ] T112 [P] [US5] 선착순 쿠폰 동시성 테스트 작성 `test/domain/coupon/application/CouponConcurrencyTest.java`
- [ ] T113 [P] [US5] 쿠폰 적용 주문 통합 테스트 작성 `test/integration/CouponOrderIntegrationTest.java`

### 엔티티 및 Repository (US5)

- [ ] T114 [P] [US5] CouponType enum 생성 (FIXED_AMOUNT, PERCENTAGE, FREE_SHIPPING) `domain/coupon/domain/CouponType.java`
- [ ] T115 [P] [US5] CouponStatus enum 생성 (AVAILABLE, USED, EXPIRED) `domain/coupon/domain/CouponStatus.java`
- [ ] T116 [P] [US5] Coupon 엔티티 생성 (@Version 비관적 락 지원) `domain/coupon/domain/Coupon.java`
- [ ] T117 [P] [US5] MemberCoupon 엔티티 생성 `domain/coupon/domain/MemberCoupon.java`
- [ ] T118 [P] [US5] CouponRepository 생성 (비관적 락 쿼리 포함) `domain/coupon/dao/CouponRepository.java`
- [ ] T119 [P] [US5] MemberCouponRepository 생성 `domain/coupon/dao/MemberCouponRepository.java`

### DTO (US5)

- [ ] T120 [P] [US5] CouponResponse, CouponDetailResponse 생성 `domain/coupon/dto/CouponResponse.java`, `domain/coupon/dto/CouponDetailResponse.java`
- [ ] T121 [P] [US5] MemberCouponResponse 생성 `domain/coupon/dto/MemberCouponResponse.java`
- [ ] T122 [P] [US5] ApplicableCouponResponse 생성 (주문 적용 가능 쿠폰) `domain/coupon/dto/ApplicableCouponResponse.java`

### 예외 (US5)

- [ ] T123 [P] [US5] CouponException 클래스 생성 `domain/coupon/exception/CouponException.java`

### Service (US5)

- [ ] T124 [US5] CouponService 구현 (발급, 사용, 복원, 비관적 락) `domain/coupon/application/CouponService.java`
- [ ] T125 [US5] CouponCalculator 구현 (할인 금액 계산, 조건 검증) `domain/coupon/application/CouponCalculator.java`

### Controller (US5)

- [ ] T126 [US5] CouponController 구현 (발급 가능 쿠폰 조회, 발급) `domain/coupon/api/CouponController.java`
- [ ] T127 [US5] MemberCouponController 구현 (내 쿠폰 조회, 적용 가능 쿠폰) `domain/coupon/api/MemberCouponController.java`

### 통합 (US5)

- [ ] T128 [US5] OrderService에 CouponService 연동 (주문 생성 시 쿠폰 적용) `domain/order/application/OrderService.java`
- [ ] T129 [US5] MemberService에 CouponService 연동 (신규 가입 시 쿠폰 발급) `domain/member/application/MemberService.java`

**체크포인트**: User Story 5 완료 - 쿠폰 발급/적용/할인 계산 독립 테스트 가능

---

## Phase 8: User Story 6 - 회원 등급 및 혜택 (Priority: P3)

**목표**: 회원의 구매 금액에 따라 등급이 산정되고, 등급별 혜택 제공

**독립 테스트**: 등급 조건 충족 시 해당 등급 혜택(적립률, 무료배송)이 적용되면 성공

### 테스트 (US6)

- [ ] T130 [P] [US6] GradeService 단위 테스트 작성 `test/domain/member/application/GradeServiceTest.java`
- [ ] T131 [P] [US6] 등급별 혜택 통합 테스트 작성 `test/integration/GradeBenefitIntegrationTest.java`

### Service (US6)

- [ ] T132 [US6] GradeService 구현 (등급 계산, 혜택 조회) `domain/member/application/GradeService.java`
- [ ] T133 [US6] GradeScheduler 구현 (매월 1일 등급 재산정) `domain/member/application/GradeScheduler.java`
- [ ] T134 [US6] GradeBenefitProvider 구현 (등급별 적립률, 무료배송 조건) `domain/member/application/GradeBenefitProvider.java`

### 통합 (US6)

- [ ] T135 [US6] OrderService에 GradeBenefitProvider 연동 (배송비 계산) `domain/order/application/OrderService.java`
- [ ] T136 [US6] CouponService에 등급 승급 쿠폰 발급 연동 `domain/coupon/application/CouponService.java`

**체크포인트**: User Story 6 완료 - 등급 산정 및 혜택 적용 독립 테스트 가능

---

## Phase 9: 마무리 및 품질 개선

**목적**: 전체 시스템 안정화 및 문서화

- [ ] T137 [P] API 문서화 (OpenAPI 기반 Swagger UI 연동)
- [ ] T138 [P] 로깅 설정 최적화 (운영 환경 로그 레벨 조정)
- [ ] T139 [P] 성능 테스트 (상품 검색 1초, 결제 3초 목표 검증)
- [ ] T140 코드 리팩토링 및 중복 제거
- [ ] T141 보안 점검 (SQL Injection, XSS 방지 확인)
- [ ] T142 quickstart.md 검증 (빌드, 실행, 테스트 확인)

---

## 의존성 및 실행 순서

### Phase 의존성

- **Phase 1 (설정)**: 의존성 없음 - 즉시 시작 가능
- **Phase 2 (공통 인프라)**: Phase 1 완료 필요 - **모든 User Story 차단**
- **Phase 3-8 (User Stories)**: Phase 2 완료 후 시작 가능
  - 병렬 진행 가능 (팀원 배분 시)
  - 또는 우선순위 순서대로 순차 진행 (P1 → P2 → P3)
- **Phase 9 (마무리)**: 모든 User Story 완료 후

### User Story 의존성

- **US1 (회원)**: Phase 2 후 즉시 시작 가능 - 다른 스토리 의존 없음
- **US2 (상품)**: Phase 2 후 시작 가능 - 다른 스토리 의존 없음
- **US3 (주문/결제)**: US1 (회원), US2 (상품) 필요
- **US4 (재고)**: US2 (상품), US3 (주문) 필요
- **US5 (쿠폰)**: US1 (회원), US3 (주문) 필요
- **US6 (등급)**: US1 (회원), US3 (주문), US5 (쿠폰) 필요

### 각 User Story 내 순서

- 테스트 작성 → 실패 확인
- 엔티티/Repository 생성 (병렬 가능)
- DTO 생성 (병렬 가능)
- Service 구현
- Controller 구현
- 통합 연동

### 병렬 실행 기회

- Phase 1: T002-T005 병렬 가능
- Phase 2: T006-T009, T010-T012, T014-T015, T018-T019, T021-T022 각각 병렬 가능
- 각 User Story: 테스트, 엔티티, DTO 태스크들 병렬 가능

---

## 병렬 실행 예시: User Story 3

```bash
# 테스트 병렬 실행:
Task: "OrderService 단위 테스트 작성 test/domain/order/application/OrderServiceTest.java"
Task: "PaymentService 단위 테스트 작성 test/domain/payment/application/PaymentServiceTest.java"
Task: "멱등성 테스트 작성 test/domain/payment/application/IdempotencyTest.java"

# 엔티티 병렬 실행:
Task: "OrderStatus enum 생성 domain/order/domain/OrderStatus.java"
Task: "Order 엔티티 생성 domain/order/domain/Order.java"
Task: "PaymentStatus enum 생성 domain/payment/domain/PaymentStatus.java"
Task: "Payment 엔티티 생성 domain/payment/domain/Payment.java"
```

---

## 구현 전략

### MVP 우선 (User Story 1-3)

1. Phase 1: 프로젝트 설정 완료
2. Phase 2: 공통 인프라 완료 (CRITICAL)
3. Phase 3: User Story 1 완료 → 독립 테스트 → 검증
4. Phase 4: User Story 2 완료 → 독립 테스트 → 검증
5. Phase 5: User Story 3 완료 → 독립 테스트 → 검증
6. **MVP 배포 가능 시점**

### 점진적 확장

7. Phase 6: User Story 4 (재고 동시성) → 테스트 → 배포
8. Phase 7: User Story 5 (쿠폰) → 테스트 → 배포
9. Phase 8: User Story 6 (등급) → 테스트 → 배포
10. Phase 9: 마무리 → 최종 배포

### 병렬 팀 전략

다수 개발자 참여 시:

1. 팀 전체: Phase 1-2 완료
2. Phase 2 완료 후:
   - 개발자 A: US1 (회원)
   - 개발자 B: US2 (상품)
   - 개발자 C: US3 준비 (US1, US2 완료 대기)
3. US1, US2 완료 후:
   - 개발자 A: US4 (재고)
   - 개발자 B: US5 (쿠폰)
   - 개발자 C: US3 (주문/결제)
4. 각 스토리 독립 완료 및 통합

---

## 참고 사항

- [P] 태스크 = 파일이 다르고 의존성 없음
- [Story] 라벨 = 해당 User Story 추적용
- 각 User Story는 독립적으로 완료 및 테스트 가능
- 테스트 실패 확인 후 구현 시작
- 태스크 또는 논리적 그룹 완료 후 커밋
- 체크포인트에서 독립 검증 가능
- 회피: 모호한 태스크, 동일 파일 충돌, 스토리 간 독립성 파괴
