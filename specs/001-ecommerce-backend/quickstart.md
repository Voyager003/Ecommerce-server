# Quickstart Guide: E-commerce Backend System

**Branch**: `001-ecommerce-backend` | **Date**: 2026-01-06

## 프로젝트 초기화

### 1. 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| Language | Java | 25 |
| Framework | Spring Boot | 4.0 |
| ORM | Spring Data JPA | - |
| Database | MySQL (prod) / H2 (test) | 8.x / 2.x |
| Build Tool | Gradle | 8.x |
| Test | JUnit 5, AssertJ, Mockito | - |

### 2. 프로젝트 생성

```bash
# Spring Initializr 또는 IDE로 프로젝트 생성
# 필수 의존성: Spring Web, Spring Data JPA, Spring Security,
#             Validation, MySQL Driver, H2 Database, Lombok
```

### 3. Gradle 의존성 (build.gradle)

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.0.0'
    id 'io.spring.dependency-management' version '1.1.x'
}

java {
    sourceCompatibility = '25'
}

dependencies {
    // Spring
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.x'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.x'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.x'

    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'
    runtimeOnly 'com.h2database:h2'

    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

---

## 디렉토리 구조

```
src/main/java/com/ecommerce/
├── domain/
│   ├── member/          # 회원 도메인
│   ├── product/         # 상품 도메인
│   ├── inventory/       # 재고 도메인
│   ├── order/           # 주문 도메인
│   ├── payment/         # 결제 도메인
│   ├── coupon/          # 쿠폰 도메인
│   └── model/           # 공통 Embeddable
├── global/
│   ├── common/          # ApiResponse, PageRequest
│   ├── config/          # JpaConfig, SecurityConfig
│   ├── error/           # BusinessException, ErrorCode
│   └── security/        # JwtTokenProvider
└── infra/
    └── pg/              # MockPgClient
```

### 도메인 패키지 구조

```
domain/{도메인명}/
├── api/              # REST Controller
├── application/      # Service (트랜잭션)
├── dao/              # Repository
├── domain/           # Entity, VO, Enum
├── dto/              # Request, Response
└── exception/        # 도메인 예외
```

---

## 핵심 설정

### 1. application.yml

```yaml
spring:
  profiles:
    active: local

---
# Local Profile
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:h2:mem:ecommerce;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 100

jwt:
  secret: local-dev-secret-key-min-256-bits-required-here
  access-token-validity: 1800000   # 30분
  refresh-token-validity: 604800000 # 7일

---
# Production Profile
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

### 2. SecurityConfig

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/products/**").permitAll()
                .requestMatchers("/api/v1/categories/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### 3. JpaConfig

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

---

## 공통 컴포넌트

### 1. BaseTimeEntity

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### 2. ApiResponse

```java
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, data, "Created");
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(true, null, null);
    }
}
```

### 3. BusinessException

```java
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### 4. ErrorCode

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT("C001", "유효하지 않은 입력입니다", 400),
    UNAUTHORIZED("C002", "인증이 필요합니다", 401),
    FORBIDDEN("C003", "접근 권한이 없습니다", 403),
    NOT_FOUND("C004", "리소스를 찾을 수 없습니다", 404),

    // Member
    MEMBER_NOT_FOUND("M001", "회원을 찾을 수 없습니다", 404),
    EMAIL_DUPLICATED("M002", "이미 사용 중인 이메일입니다", 409),
    ACCOUNT_LOCKED("M003", "계정이 잠금되었습니다", 423),
    INVALID_PASSWORD("M004", "비밀번호가 일치하지 않습니다", 400),

    // Product
    PRODUCT_NOT_FOUND("P001", "상품을 찾을 수 없습니다", 404),
    PRODUCT_NOT_AVAILABLE("P002", "구매할 수 없는 상품입니다", 400),

    // Inventory
    INSUFFICIENT_STOCK("I001", "재고가 부족합니다", 400),
    STOCK_CONFLICT("I002", "재고 충돌이 발생했습니다", 409),

    // Order
    ORDER_NOT_FOUND("O001", "주문을 찾을 수 없습니다", 404),
    INVALID_ORDER_STATUS("O002", "유효하지 않은 주문 상태입니다", 400),

    // Payment
    PAYMENT_FAILED("PAY001", "결제에 실패했습니다", 402),
    DUPLICATE_PAYMENT("PAY002", "이미 처리된 결제입니다", 409),
    PAYMENT_NOT_FOUND("PAY003", "결제 정보를 찾을 수 없습니다", 404),

    // Coupon
    COUPON_NOT_FOUND("CP001", "쿠폰을 찾을 수 없습니다", 404),
    COUPON_NOT_AVAILABLE("CP002", "사용할 수 없는 쿠폰입니다", 400),
    COUPON_ALREADY_ISSUED("CP003", "이미 발급받은 쿠폰입니다", 409),
    COUPON_EXHAUSTED("CP004", "쿠폰이 모두 소진되었습니다", 410);

    private final String code;
    private final String message;
    private final int status;
}
```

### 5. GlobalExceptionHandler

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity
            .badRequest()
            .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
    }
}
```

---

## 빠른 시작 명령어

```bash
# 빌드
./gradlew build

# 로컬 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 테스트
./gradlew test

# H2 Console 접속 (로컬)
# http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:ecommerce
```

---

## 개발 순서 (권장)

1. **Phase 1**: 공통 인프라 구축
   - global/ 패키지 (config, error, security)
   - domain/model/ (BaseTimeEntity, Money)

2. **Phase 2**: 회원 도메인
   - Member Entity, Repository
   - 회원가입/로그인/JWT 인증

3. **Phase 3**: 상품 도메인
   - Product, Category, ProductOption Entity
   - 상품 목록/상세 조회

4. **Phase 4**: 재고 도메인
   - Inventory, InventoryHistory Entity
   - 낙관적 락 구현

5. **Phase 5**: 주문 도메인
   - Order, OrderItem Entity
   - 주문 생성/조회/취소
   - 상태 머신 구현

6. **Phase 6**: 결제 도메인
   - Payment, PaymentHistory Entity
   - Mock PG 연동
   - 멱등성 처리

7. **Phase 7**: 쿠폰 도메인
   - Coupon, MemberCoupon Entity
   - 비관적 락 구현

---

## 참고 문서

- [spec.md](./spec.md) - 요구사항 명세
- [plan.md](./plan.md) - 구현 계획
- [research.md](./research.md) - 기술 결정 사항
- [data-model.md](./data-model.md) - 데이터 모델
- [contracts/](./contracts/) - API 명세 (OpenAPI 3.1)
