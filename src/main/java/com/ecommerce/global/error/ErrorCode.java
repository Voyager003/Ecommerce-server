package com.ecommerce.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT("C001", "유효하지 않은 입력입니다", 400),
    UNAUTHORIZED("C002", "인증이 필요합니다", 401),
    FORBIDDEN("C003", "접근 권한이 없습니다", 403),
    NOT_FOUND("C004", "리소스를 찾을 수 없습니다", 404),
    INTERNAL_SERVER_ERROR("C005", "서버 오류가 발생했습니다", 500),

    // Member
    MEMBER_NOT_FOUND("M001", "회원을 찾을 수 없습니다", 404),
    EMAIL_DUPLICATED("M002", "이미 사용 중인 이메일입니다", 409),
    ACCOUNT_LOCKED("M003", "계정이 잠금되었습니다", 423),
    INVALID_PASSWORD("M004", "비밀번호가 일치하지 않습니다", 400),
    INVALID_REFRESH_TOKEN("M005", "유효하지 않은 리프레시 토큰입니다", 401),
    ADDRESS_LIMIT_EXCEEDED("M006", "배송지는 최대 5개까지 등록 가능합니다", 400),
    ADDRESS_NOT_FOUND("M007", "배송지를 찾을 수 없습니다", 404),
    CANNOT_DELETE_DEFAULT_ADDRESS("M008", "기본 배송지는 삭제할 수 없습니다", 400),
    CANNOT_WITHDRAW_WITH_PENDING_ORDER("M009", "진행 중인 주문이 있어 탈퇴할 수 없습니다", 400),
    EMAIL_RECENTLY_WITHDRAWN("M010", "탈퇴 후 30일 이내 재가입 불가합니다", 400),

    // Product
    PRODUCT_NOT_FOUND("P001", "상품을 찾을 수 없습니다", 404),
    PRODUCT_NOT_AVAILABLE("P002", "구매할 수 없는 상품입니다", 400),
    CATEGORY_NOT_FOUND("P003", "카테고리를 찾을 수 없습니다", 404),
    PRODUCT_OPTION_REQUIRED("P004", "옵션 선택이 필요한 상품입니다", 400),
    PRODUCT_OPTION_NOT_FOUND("P005", "상품 옵션을 찾을 수 없습니다", 404),

    // Inventory
    INSUFFICIENT_STOCK("I001", "재고가 부족합니다", 400),
    STOCK_CONFLICT("I002", "재고 충돌이 발생했습니다. 다시 시도해주세요", 409),
    INVENTORY_NOT_FOUND("I003", "재고 정보를 찾을 수 없습니다", 404),

    // Order
    ORDER_NOT_FOUND("O001", "주문을 찾을 수 없습니다", 404),
    INVALID_ORDER_STATUS("O002", "유효하지 않은 주문 상태입니다", 400),
    CANNOT_CANCEL_ORDER("O003", "취소할 수 없는 주문입니다", 400),
    ORDER_AMOUNT_MISMATCH("O004", "주문 금액이 일치하지 않습니다", 400),
    ORDER_ALREADY_PAID("O005", "이미 결제된 주문입니다", 400),

    // Payment
    PAYMENT_FAILED("PAY001", "결제에 실패했습니다", 402),
    DUPLICATE_PAYMENT("PAY002", "이미 처리된 결제입니다", 409),
    PAYMENT_NOT_FOUND("PAY003", "결제 정보를 찾을 수 없습니다", 404),
    INVALID_PAYMENT_AMOUNT("PAY004", "결제 금액이 유효하지 않습니다", 400),
    PAYMENT_TIMEOUT("PAY005", "결제 처리 시간이 초과되었습니다", 408),
    CANNOT_CANCEL_PAYMENT("PAY006", "취소할 수 없는 결제입니다", 400),
    IDEMPOTENCY_KEY_REQUIRED("PAY007", "멱등키가 필요합니다", 400),

    // Coupon
    COUPON_NOT_FOUND("CP001", "쿠폰을 찾을 수 없습니다", 404),
    COUPON_NOT_AVAILABLE("CP002", "사용할 수 없는 쿠폰입니다", 400),
    COUPON_ALREADY_ISSUED("CP003", "이미 발급받은 쿠폰입니다", 409),
    COUPON_EXHAUSTED("CP004", "쿠폰이 모두 소진되었습니다", 410),
    COUPON_EXPIRED("CP005", "유효기간이 만료된 쿠폰입니다", 400),
    COUPON_MIN_ORDER_NOT_MET("CP006", "최소 주문 금액 조건을 충족하지 않습니다", 400),
    COUPON_ALREADY_USED("CP007", "이미 사용된 쿠폰입니다", 400);

    private final String code;
    private final String message;
    private final int status;
}
