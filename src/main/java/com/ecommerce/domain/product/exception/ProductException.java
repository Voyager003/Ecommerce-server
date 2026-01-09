package com.ecommerce.domain.product.exception;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;

public class ProductException extends BusinessException {

    public ProductException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ProductException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static ProductException notFound() {
        return new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
    }

    public static ProductException notAvailable() {
        return new ProductException(ErrorCode.PRODUCT_NOT_AVAILABLE);
    }

    public static ProductException categoryNotFound() {
        return new ProductException(ErrorCode.CATEGORY_NOT_FOUND);
    }

    public static ProductException optionRequired() {
        return new ProductException(ErrorCode.PRODUCT_OPTION_REQUIRED);
    }

    public static ProductException optionNotFound() {
        return new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
    }
}
