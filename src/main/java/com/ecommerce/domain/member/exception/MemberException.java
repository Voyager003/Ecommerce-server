package com.ecommerce.domain.member.exception;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;

public class MemberException extends BusinessException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MemberException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static MemberException notFound() {
        return new MemberException(ErrorCode.MEMBER_NOT_FOUND);
    }

    public static MemberException emailDuplicated() {
        return new MemberException(ErrorCode.EMAIL_DUPLICATED);
    }

    public static MemberException accountLocked() {
        return new MemberException(ErrorCode.ACCOUNT_LOCKED);
    }

    public static MemberException invalidPassword() {
        return new MemberException(ErrorCode.INVALID_PASSWORD);
    }

    public static MemberException invalidRefreshToken() {
        return new MemberException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    public static MemberException addressLimitExceeded() {
        return new MemberException(ErrorCode.ADDRESS_LIMIT_EXCEEDED);
    }

    public static MemberException addressNotFound() {
        return new MemberException(ErrorCode.ADDRESS_NOT_FOUND);
    }

    public static MemberException cannotDeleteDefaultAddress() {
        return new MemberException(ErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS);
    }

    public static MemberException cannotWithdrawWithPendingOrder() {
        return new MemberException(ErrorCode.CANNOT_WITHDRAW_WITH_PENDING_ORDER);
    }

    public static MemberException emailRecentlyWithdrawn() {
        return new MemberException(ErrorCode.EMAIL_RECENTLY_WITHDRAWN);
    }
}
