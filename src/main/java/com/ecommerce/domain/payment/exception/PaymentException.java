package com.ecommerce.domain.payment.exception;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;

public class PaymentException extends BusinessException {

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PaymentException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static PaymentException failed() {
        return new PaymentException(ErrorCode.PAYMENT_FAILED);
    }

    public static PaymentException duplicatePayment() {
        return new PaymentException(ErrorCode.DUPLICATE_PAYMENT);
    }

    public static PaymentException notFound() {
        return new PaymentException(ErrorCode.PAYMENT_NOT_FOUND);
    }

    public static PaymentException invalidAmount() {
        return new PaymentException(ErrorCode.INVALID_PAYMENT_AMOUNT);
    }

    public static PaymentException timeout() {
        return new PaymentException(ErrorCode.PAYMENT_TIMEOUT);
    }

    public static PaymentException cannotCancel() {
        return new PaymentException(ErrorCode.CANNOT_CANCEL_PAYMENT);
    }

    public static PaymentException idempotencyKeyRequired() {
        return new PaymentException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
    }
}
