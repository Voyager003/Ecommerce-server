package com.ecommerce.domain.payment.application;

import com.ecommerce.domain.order.application.OrderService;
import com.ecommerce.domain.order.domain.Order;
import com.ecommerce.domain.payment.dao.PaymentRepository;
import com.ecommerce.domain.payment.domain.Payment;
import com.ecommerce.domain.payment.dto.PaymentRequest;
import com.ecommerce.domain.payment.dto.PaymentResponse;
import com.ecommerce.domain.payment.exception.PaymentException;
import com.ecommerce.global.idempotency.IdempotencyService;
import com.ecommerce.global.idempotency.IdempotencyService.IdempotencyResult;
import com.ecommerce.infra.pg.PgClient;
import com.ecommerce.infra.pg.PgRequest;
import com.ecommerce.infra.pg.PgResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final String RESOURCE_TYPE = "PAYMENT";

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final PgClient pgClient;
    private final IdempotencyService idempotencyService;

    @Transactional
    public PaymentResponse processPayment(Long memberId, PaymentRequest request) {
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().isBlank()) {
            throw PaymentException.idempotencyKeyRequired();
        }

        IdempotencyResult idempotencyResult = idempotencyService.checkAndCreate(
                request.getIdempotencyKey(), RESOURCE_TYPE);

        if (idempotencyResult.isDuplicate()) {
            Payment existingPayment = paymentRepository.findById(idempotencyResult.resourceId())
                    .orElseThrow(PaymentException::notFound);
            return PaymentResponse.from(existingPayment);
        }

        Order order = orderService.findById(request.getOrderId());

        if (!order.getMemberId().equals(memberId)) {
            throw PaymentException.notFound();
        }

        if (!order.isPending()) {
            throw PaymentException.duplicatePayment();
        }

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .memberId(memberId)
                .amount(order.getFinalAmount())
                .method(request.getMethod())
                .idempotencyKey(request.getIdempotencyKey())
                .build();

        payment = paymentRepository.save(payment);

        try {
            PgRequest pgRequest = PgRequest.builder()
                    .orderNumber(order.getOrderNumber())
                    .amount(order.getFinalAmount().getAmount())
                    .cardNumber(request.getCardNumber())
                    .paymentMethod(request.getMethod().name())
                    .build();

            PgResponse pgResponse = pgClient.approve(pgRequest);

            if (pgResponse.isSuccess()) {
                payment.approve(pgResponse.getTransactionId());
                orderService.markAsPaid(order.getId());

                idempotencyService.complete(request.getIdempotencyKey(), RESOURCE_TYPE,
                        payment.getId(), payment.getPaymentNumber());

                log.info("결제 성공: paymentNumber={}, orderId={}, amount={}",
                        payment.getPaymentNumber(), order.getId(), payment.getAmount().getAmount());
            } else {
                payment.fail(pgResponse.getResponseMessage());
                idempotencyService.delete(request.getIdempotencyKey(), RESOURCE_TYPE);
                log.warn("결제 실패: orderId={}, reason={}", order.getId(), pgResponse.getResponseMessage());
            }
        } catch (Exception e) {
            payment.fail(e.getMessage());
            idempotencyService.delete(request.getIdempotencyKey(), RESOURCE_TYPE);
            log.error("결제 처리 중 오류 발생: orderId={}", order.getId(), e);
        }

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPayment(Long memberId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentException::notFound);

        if (!payment.getMemberId().equals(memberId)) {
            throw PaymentException.notFound();
        }

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrderId(Long memberId, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(PaymentException::notFound);

        if (!payment.getMemberId().equals(memberId)) {
            throw PaymentException.notFound();
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long memberId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentException::notFound);

        if (!payment.getMemberId().equals(memberId)) {
            throw PaymentException.notFound();
        }

        payment.cancel();

        log.info("결제 취소: paymentNumber={}", payment.getPaymentNumber());
        return PaymentResponse.from(payment);
    }
}
