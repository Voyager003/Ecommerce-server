package com.ecommerce.domain.inventory.exception;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;

public class InventoryException extends BusinessException {

    public InventoryException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InventoryException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static InventoryException insufficientStock() {
        return new InventoryException(ErrorCode.INSUFFICIENT_STOCK);
    }

    public static InventoryException stockConflict() {
        return new InventoryException(ErrorCode.STOCK_CONFLICT);
    }

    public static InventoryException notFound() {
        return new InventoryException(ErrorCode.INVENTORY_NOT_FOUND);
    }
}
