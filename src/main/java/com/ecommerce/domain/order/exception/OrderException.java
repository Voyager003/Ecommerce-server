package com.ecommerce.domain.order.exception;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;

public class OrderException extends BusinessException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OrderException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static OrderException notFound() {
        return new OrderException(ErrorCode.ORDER_NOT_FOUND);
    }

    public static OrderException invalidStatus() {
        return new OrderException(ErrorCode.INVALID_ORDER_STATUS);
    }

    public static OrderException cannotCancel() {
        return new OrderException(ErrorCode.CANNOT_CANCEL_ORDER);
    }

    public static OrderException amountMismatch() {
        return new OrderException(ErrorCode.ORDER_AMOUNT_MISMATCH);
    }

    public static OrderException alreadyPaid() {
        return new OrderException(ErrorCode.ORDER_ALREADY_PAID);
    }
}
