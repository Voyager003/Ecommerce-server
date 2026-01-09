package com.ecommerce.infra.pg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PgResponse {

    private final boolean success;
    private final String transactionId;
    private final String responseCode;
    private final String responseMessage;
    private final LocalDateTime processedAt;

    public static PgResponse success(String transactionId) {
        return new PgResponse(
                true,
                transactionId,
                "0000",
                "승인 완료",
                LocalDateTime.now()
        );
    }

    public static PgResponse failure(String responseCode, String responseMessage) {
        return new PgResponse(
                false,
                null,
                responseCode,
                responseMessage,
                LocalDateTime.now()
        );
    }

    public static PgResponse cancelled(String transactionId) {
        return new PgResponse(
                true,
                transactionId,
                "0000",
                "취소 완료",
                LocalDateTime.now()
        );
    }

    public static PgResponse timeout() {
        return new PgResponse(
                false,
                null,
                "9999",
                "처리 시간 초과",
                LocalDateTime.now()
        );
    }
}
