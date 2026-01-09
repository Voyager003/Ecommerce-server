package com.ecommerce.infra.pg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Profile("!prod")
public class MockPgClient implements PgClient {

    private final Map<String, PgResponse> transactions = new ConcurrentHashMap<>();

    @Override
    public PgResponse approve(PgRequest request) {
        log.info("Mock PG 결제 요청: orderNumber={}, amount={}", request.getOrderNumber(), request.getAmount());

        String cardNumber = request.getCardNumber();
        if (cardNumber == null || cardNumber.length() < 4) {
            return PgResponse.failure("1001", "유효하지 않은 카드 번호");
        }

        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);

        // 카드번호 끝자리로 시나리오 분기
        return switch (lastFourDigits) {
            case "1111" -> {
                log.info("Mock PG: 결제 실패 시나리오");
                yield PgResponse.failure("2001", "잔액 부족");
            }
            case "2222" -> {
                log.info("Mock PG: 타임아웃 시나리오");
                simulateTimeout();
                yield PgResponse.timeout();
            }
            case "3333" -> {
                log.info("Mock PG: 카드 한도 초과 시나리오");
                yield PgResponse.failure("2002", "카드 한도 초과");
            }
            case "4444" -> {
                log.info("Mock PG: 분실/도난 카드 시나리오");
                yield PgResponse.failure("3001", "분실/도난 카드");
            }
            default -> {
                String transactionId = generateTransactionId();
                log.info("Mock PG: 결제 성공, transactionId={}", transactionId);
                PgResponse response = PgResponse.success(transactionId);
                transactions.put(transactionId, response);
                yield response;
            }
        };
    }

    @Override
    public PgResponse cancel(String transactionId, long amount) {
        log.info("Mock PG 결제 취소: transactionId={}, amount={}", transactionId, amount);

        if (!transactions.containsKey(transactionId)) {
            return PgResponse.failure("4001", "거래 내역 없음");
        }

        PgResponse response = PgResponse.cancelled(transactionId);
        transactions.remove(transactionId);
        return response;
    }

    @Override
    public PgResponse inquiry(String transactionId) {
        log.info("Mock PG 거래 조회: transactionId={}", transactionId);

        PgResponse response = transactions.get(transactionId);
        if (response == null) {
            return PgResponse.failure("4001", "거래 내역 없음");
        }
        return response;
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private void simulateTimeout() {
        try {
            Thread.sleep(100); // 실제 타임아웃 대신 짧은 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
