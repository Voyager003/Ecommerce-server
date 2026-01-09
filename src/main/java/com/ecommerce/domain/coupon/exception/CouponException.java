package com.ecommerce.domain.coupon.exception;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;

public class CouponException extends BusinessException {

    public CouponException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CouponException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static CouponException notFound() {
        return new CouponException(ErrorCode.COUPON_NOT_FOUND);
    }

    public static CouponException notAvailable() {
        return new CouponException(ErrorCode.COUPON_NOT_AVAILABLE);
    }

    public static CouponException alreadyIssued() {
        return new CouponException(ErrorCode.COUPON_ALREADY_ISSUED);
    }

    public static CouponException alreadyUsed() {
        return new CouponException(ErrorCode.COUPON_ALREADY_USED);
    }

    public static CouponException expired() {
        return new CouponException(ErrorCode.COUPON_EXPIRED);
    }

    public static CouponException quantityExceeded() {
        return new CouponException(ErrorCode.COUPON_QUANTITY_EXCEEDED);
    }

    public static CouponException minOrderAmountNotMet() {
        return new CouponException(ErrorCode.COUPON_MIN_ORDER_AMOUNT_NOT_MET);
    }

    public static CouponException cannotRestore() {
        return new CouponException(ErrorCode.COUPON_CANNOT_RESTORE);
    }
}
