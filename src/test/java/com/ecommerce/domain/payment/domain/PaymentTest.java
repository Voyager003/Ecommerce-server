package com.ecommerce.domain.payment.domain;

import com.ecommerce.domain.model.Money;
import com.ecommerce.domain.payment.exception.PaymentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Nested
    @DisplayName("결제 승인")
    class ApproveTest {

        @Test
        @DisplayName("대기 상태에서 승인한다")
        void approve_FromPending_Success() {
            // given
            Payment payment = createPayment();

            // when
            payment.approve("TXN-12345");

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getPgTransactionId()).isEqualTo("TXN-12345");
            assertThat(payment.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 승인된 결제는 재승인할 수 없다")
        void approve_AlreadyApproved_ThrowsException() {
            // given
            Payment payment = createPayment();
            payment.approve("TXN-12345");

            // when & then
            assertThatThrownBy(() -> payment.approve("TXN-67890"))
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("결제 실패")
    class FailTest {

        @Test
        @DisplayName("결제 실패 시 실패 사유가 기록된다")
        void fail_RecordsReason() {
            // given
            Payment payment = createPayment();

            // when
            payment.fail("카드 한도 초과");

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureReason()).isEqualTo("카드 한도 초과");
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class CancelTest {

        @Test
        @DisplayName("승인된 결제를 취소한다")
        void cancel_FromApproved_Success() {
            // given
            Payment payment = createPayment();
            payment.approve("TXN-12345");

            // when
            payment.cancel();

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
            assertThat(payment.getCancelledAt()).isNotNull();
            assertThat(payment.getRefundedAmount().getAmount()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("대기 상태의 결제는 취소할 수 없다")
        void cancel_FromPending_ThrowsException() {
            // given
            Payment payment = createPayment();

            // when & then
            assertThatThrownBy(payment::cancel)
                    .isInstanceOf(PaymentException.class);
        }
    }

    @Nested
    @DisplayName("부분 환불")
    class PartialRefundTest {

        @Test
        @DisplayName("부분 환불을 진행한다")
        void refund_PartialAmount_Success() {
            // given
            Payment payment = createPayment();
            payment.approve("TXN-12345");

            // when
            payment.refund(Money.of(20000L));

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
            assertThat(payment.getRefundedAmount().getAmount()).isEqualTo(20000L);
            assertThat(payment.getRefundableAmount().getAmount()).isEqualTo(30000L);
        }

        @Test
        @DisplayName("전액 환불 시 상태가 REFUNDED로 변경된다")
        void refund_FullAmount_StatusBecomesRefunded() {
            // given
            Payment payment = createPayment();
            payment.approve("TXN-12345");

            // when
            payment.refund(Money.of(50000L));

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundedAmount().getAmount()).isEqualTo(50000L);
        }
    }

    private Payment createPayment() {
        return Payment.builder()
                .orderId(1L)
                .memberId(1L)
                .amount(Money.of(50000L))
                .method(PaymentMethod.CREDIT_CARD)
                .idempotencyKey("IDEM-12345")
                .build();
    }
}
