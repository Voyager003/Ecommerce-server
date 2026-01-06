# Data Model: E-commerce Backend System

**Branch**: `001-ecommerce-backend` | **Date**: 2026-01-06

## Overview

6개 도메인의 엔티티, 관계, 검증 규칙, 상태 전이를 정의한다.

---

## 1. Member Domain

### 1.1 Member (회원)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 회원 ID |
| email | String(100) | UK, Not Null | 이메일 (로그인 ID) |
| password | String(255) | Not Null | 암호화된 비밀번호 |
| name | String(50) | Not Null | 이름 |
| phoneNumber | String(20) | Not Null | 휴대폰 번호 |
| grade | MemberGrade | Not Null | 회원 등급 (BRONZE, SILVER, GOLD, PLATINUM) |
| status | MemberStatus | Not Null | 회원 상태 (ACTIVE, LOCKED, WITHDRAWN) |
| failedLoginCount | Integer | Default 0 | 연속 로그인 실패 횟수 |
| lockedUntil | LocalDateTime | Nullable | 계정 잠금 해제 시각 |
| withdrawnAt | LocalDateTime | Nullable | 탈퇴 일시 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |
| updatedAt | LocalDateTime | Not Null | 수정 일시 |

**Validation Rules**:
- email: 이메일 형식, 시스템 전체 유일
- password: 최소 8자, 영문/숫자/특수문자 각 1개 이상
- phoneNumber: 숫자만, 10-11자리

**State Transitions (MemberStatus)**:
```
ACTIVE → LOCKED: 5회 연속 로그인 실패
LOCKED → ACTIVE: 잠금 시간 경과 후 로그인 성공
ACTIVE → WITHDRAWN: 탈퇴 요청 (진행 중 주문 없음)
```

### 1.2 Address (배송지)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 배송지 ID |
| memberId | Long | FK, Not Null | 회원 ID |
| name | String(50) | Not Null | 배송지명 (집, 회사 등) |
| recipientName | String(50) | Not Null | 수령인 이름 |
| phoneNumber | String(20) | Not Null | 수령인 연락처 |
| zipCode | String(10) | Not Null | 우편번호 |
| address1 | String(200) | Not Null | 기본 주소 |
| address2 | String(200) | Nullable | 상세 주소 |
| isDefault | Boolean | Default false | 기본 배송지 여부 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

**Validation Rules**:
- 회원당 최대 5개
- 기본 배송지는 회원당 1개만

### 1.3 RefreshToken (리프레시 토큰)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 토큰 ID |
| memberId | Long | FK, Not Null | 회원 ID |
| token | String(500) | UK, Not Null | 리프레시 토큰 |
| expiresAt | LocalDateTime | Not Null | 만료 일시 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

---

## 2. Product Domain

### 2.1 Category (카테고리)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 카테고리 ID |
| parentId | Long | FK, Nullable | 상위 카테고리 ID (null = 대분류) |
| name | String(50) | Not Null | 카테고리명 |
| depth | Integer | Not Null | 깊이 (1=대분류, 2=소분류) |
| displayOrder | Integer | Default 0 | 노출 순서 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

**Validation Rules**:
- 같은 레벨(parentId) 내 name 유일
- depth는 최대 2

### 2.2 Product (상품)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 상품 ID |
| categoryId | Long | FK, Not Null | 카테고리 ID |
| name | String(200) | Not Null | 상품명 |
| description | Text | Not Null | 상품 설명 |
| price | Long | Not Null | 기본 가격 (원) |
| imageUrl | String(500) | Not Null | 대표 이미지 URL |
| status | ProductStatus | Not Null | 상품 상태 |
| salesCount | Long | Default 0 | 판매 수량 |
| hasOptions | Boolean | Default false | 옵션 보유 여부 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |
| updatedAt | LocalDateTime | Not Null | 수정 일시 |

**State Transitions (ProductStatus)**:
```
DRAFT → ACTIVE: 필수 정보 완료
ACTIVE → INACTIVE: 판매 중지
INACTIVE → ACTIVE: 재고 1개 이상
ACTIVE/INACTIVE → DELETED: 미완료 주문 없음 (복구 불가)
```

### 2.3 ProductOption (상품 옵션)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 옵션 ID |
| productId | Long | FK, Not Null | 상품 ID |
| optionName | String(100) | Not Null | 옵션명 (색상: 빨강, 사이즈: L) |
| additionalPrice | Long | Default 0 | 추가 가격 |
| displayOrder | Integer | Default 0 | 노출 순서 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

---

## 3. Inventory Domain

### 3.1 Inventory (재고)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 재고 ID |
| productId | Long | FK, Not Null | 상품 ID |
| productOptionId | Long | FK, Nullable | 옵션 ID (null = 옵션 없는 상품) |
| availableQuantity | Integer | Not Null, >= 0 | 가용 재고 |
| reservedQuantity | Integer | Default 0, >= 0 | 예약 재고 |
| version | Long | Not Null | 낙관적 락 버전 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |
| updatedAt | LocalDateTime | Not Null | 수정 일시 |

**Unique Constraint**: (productId, productOptionId)

### 3.2 InventoryHistory (재고 이력)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 이력 ID |
| inventoryId | Long | FK, Not Null | 재고 ID |
| eventType | InventoryEventType | Not Null | 이벤트 유형 |
| quantity | Integer | Not Null | 변동 수량 (음수 가능) |
| availableAfter | Integer | Not Null | 변동 후 가용 재고 |
| reservedAfter | Integer | Not Null | 변동 후 예약 재고 |
| orderId | Long | Nullable | 관련 주문 ID |
| reason | String(200) | Nullable | 변동 사유 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

**InventoryEventType**:
- INCOMING: 입고
- RESERVED: 예약 (주문 생성)
- CONFIRMED: 확정 차감 (결제 완료)
- RELEASED: 예약 해제 (주문 취소/결제 실패)
- RESTORED: 복원 (환불)

---

## 4. Order Domain

### 4.1 Order (주문)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 주문 ID |
| orderNumber | String(30) | UK, Not Null | 주문번호 (ORD-YYYYMMDD-NNNNNN) |
| memberId | Long | FK, Not Null | 회원 ID |
| status | OrderStatus | Not Null | 주문 상태 |
| totalProductAmount | Long | Not Null | 상품 금액 합계 |
| shippingFee | Long | Not Null | 배송비 |
| discountAmount | Long | Default 0 | 할인 금액 |
| totalAmount | Long | Not Null | 최종 결제 금액 |
| recipientName | String(50) | Not Null | 수령인 이름 |
| recipientPhone | String(20) | Not Null | 수령인 연락처 |
| zipCode | String(10) | Not Null | 우편번호 |
| address1 | String(200) | Not Null | 기본 주소 |
| address2 | String(200) | Nullable | 상세 주소 |
| memberCouponId | Long | FK, Nullable | 사용 쿠폰 ID |
| idempotencyKey | String(50) | UK, Nullable | 멱등키 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |
| updatedAt | LocalDateTime | Not Null | 수정 일시 |

**State Transitions (OrderStatus)**:
```
PENDING → PAID: 결제 성공
PENDING → CANCELLED: 결제 전 취소 / 결제 실패 / 30분 초과
PAID → PREPARING: 상품 준비 시작
PAID → CANCELLED: 환불 처리
PREPARING → CANCELLED: 환불 처리
PREPARING → SHIPPED: 배송 시작
SHIPPED → DELIVERED: 배송 완료
```

### 4.2 OrderItem (주문 항목)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 항목 ID |
| orderId | Long | FK, Not Null | 주문 ID |
| productId | Long | FK, Not Null | 상품 ID |
| productOptionId | Long | FK, Nullable | 옵션 ID |
| productName | String(200) | Not Null | 상품명 (스냅샷) |
| optionName | String(100) | Nullable | 옵션명 (스냅샷) |
| unitPrice | Long | Not Null | 단가 (기본가 + 옵션추가가) |
| quantity | Integer | Not Null | 수량 |
| totalPrice | Long | Not Null | 소계 (단가 x 수량) |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

### 4.3 OrderStatusHistory (주문 상태 이력)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 이력 ID |
| orderId | Long | FK, Not Null | 주문 ID |
| fromStatus | OrderStatus | Nullable | 이전 상태 |
| toStatus | OrderStatus | Not Null | 변경 상태 |
| reason | String(200) | Nullable | 변경 사유 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

---

## 5. Payment Domain

### 5.1 Payment (결제)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 결제 ID |
| orderId | Long | FK, UK, Not Null | 주문 ID (1:1) |
| method | PaymentMethod | Not Null | 결제 수단 |
| amount | Long | Not Null | 결제 금액 |
| status | PaymentStatus | Not Null | 결제 상태 |
| pgTransactionId | String(100) | Nullable | PG 거래 ID |
| idempotencyKey | String(50) | UK, Not Null | 멱등키 |
| approvedAt | LocalDateTime | Nullable | 승인 일시 |
| refundedAt | LocalDateTime | Nullable | 환불 일시 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |
| updatedAt | LocalDateTime | Not Null | 수정 일시 |

**PaymentMethod**: CREDIT_CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT

**State Transitions (PaymentStatus)**:
```
PENDING → APPROVED: PG 승인 성공
PENDING → FAILED: PG 승인 실패
PENDING → UNKNOWN: PG 응답 타임아웃
UNKNOWN → APPROVED: PG 조회 결과 성공
UNKNOWN → FAILED: PG 조회 결과 실패
APPROVED → REFUNDED: 환불 처리
```

### 5.2 PaymentHistory (결제 이력)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 이력 ID |
| paymentId | Long | FK, Not Null | 결제 ID |
| action | String(50) | Not Null | 액션 (APPROVE, CANCEL, INQUIRY) |
| requestAt | LocalDateTime | Not Null | 요청 시각 |
| responseAt | LocalDateTime | Nullable | 응답 시각 |
| pgResponseCode | String(10) | Nullable | PG 응답 코드 |
| pgResponseMessage | String(500) | Nullable | PG 응답 메시지 |
| idempotencyKey | String(50) | Not Null | 멱등키 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

---

## 6. Coupon Domain

### 6.1 Coupon (쿠폰 정책)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 쿠폰 ID |
| name | String(100) | Not Null | 쿠폰명 |
| type | CouponType | Not Null | 쿠폰 유형 |
| discountValue | Long | Not Null | 할인 값 (정액: 원, 정률: %) |
| maxDiscountAmount | Long | Nullable | 최대 할인 금액 (정률용) |
| minOrderAmount | Long | Default 0 | 최소 주문 금액 |
| totalQuantity | Integer | Nullable | 발급 수량 (null = 무제한) |
| issuedQuantity | Integer | Default 0 | 발급된 수량 |
| startDate | LocalDate | Not Null | 유효기간 시작일 |
| endDate | LocalDate | Not Null | 유효기간 종료일 |
| isActive | Boolean | Default true | 활성 여부 |
| version | Long | Not Null | 낙관적 락 버전 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

**CouponType**: FIXED_AMOUNT, PERCENTAGE, FREE_SHIPPING

### 6.2 MemberCoupon (회원 보유 쿠폰)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | 회원쿠폰 ID |
| memberId | Long | FK, Not Null | 회원 ID |
| couponId | Long | FK, Not Null | 쿠폰 ID |
| status | CouponStatus | Not Null | 사용 상태 |
| usedOrderId | Long | FK, Nullable | 사용한 주문 ID |
| issuedAt | LocalDateTime | Not Null | 발급 일시 |
| usedAt | LocalDateTime | Nullable | 사용 일시 |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

**CouponStatus**: AVAILABLE, USED, EXPIRED

**Unique Constraint**: (memberId, couponId) - 회원당 동일 쿠폰 1회 발급

---

## 7. Common

### 7.1 IdempotencyRecord (멱등성 레코드)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| idempotencyKey | String(50) | PK | 멱등키 |
| resourceType | String(50) | Not Null | 리소스 유형 (ORDER, PAYMENT) |
| resourceId | Long | Nullable | 생성된 리소스 ID |
| responseBody | Text | Nullable | 응답 본문 (JSON) |
| expiresAt | LocalDateTime | Not Null | 만료 일시 (24시간 후) |
| createdAt | LocalDateTime | Not Null | 생성 일시 |

---

## Entity Relationship Diagram (텍스트)

```
Member (1) ──── (N) Address
   │
   │ (1)
   ├──── (N) RefreshToken
   │
   │ (1)
   ├──── (N) Order ──── (N) OrderItem
   │         │              │
   │         │              ├──── Product (N:1)
   │         │              └──── ProductOption (N:1)
   │         │
   │         ├──── (1) Payment ──── (N) PaymentHistory
   │         │
   │         └──── OrderStatusHistory (1:N)
   │
   └──── (N) MemberCoupon ──── (N:1) Coupon

Category (1) ──── (N) Category (Self Reference: parent)
    │
    │ (1)
    └──── (N) Product ──── (N) ProductOption
                │
                │ (1)
                └──── (N) Inventory ──── (N) InventoryHistory
```
