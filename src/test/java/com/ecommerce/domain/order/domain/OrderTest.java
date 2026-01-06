package com.ecommerce.domain.order.domain;

import com.ecommerce.domain.model.Money;
import com.ecommerce.domain.order.exception.OrderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Nested
    @DisplayName("주문 상태 전이")
    class StatusTransitionTest {

        @Test
        @DisplayName("결제 대기 상태에서 결제 완료로 전이한다")
        void markAsPaid_FromPending_Success() {
            // given
            Order order = createOrder();
            addOrderItem(order);

            // when
            order.markAsPaid();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 완료 상태에서 상품 준비중으로 전이한다")
        void startPreparing_FromPaid_Success() {
            // given
            Order order = createOrder();
            addOrderItem(order);
            order.markAsPaid();

            // when
            order.startPreparing();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        }

        @Test
        @DisplayName("배송중 상태에서 배송 완료로 전이한다")
        void deliver_FromShipped_Success() {
            // given
            Order order = createOrder();
            addOrderItem(order);
            order.markAsPaid();
            order.startPreparing();
            order.ship();

            // when
            order.deliver();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class CancelTest {

        @Test
        @DisplayName("결제 대기 상태에서 취소한다")
        void cancel_FromPending_Success() {
            // given
            Order order = createOrder();
            addOrderItem(order);

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 완료 상태에서 취소한다")
        void cancel_FromPaid_Success() {
            // given
            Order order = createOrder();
            addOrderItem(order);
            order.markAsPaid();

            // when
            order.cancel();

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("배송중 상태에서는 취소할 수 없다")
        void cancel_FromShipped_ThrowsException() {
            // given
            Order order = createOrder();
            addOrderItem(order);
            order.markAsPaid();
            order.startPreparing();
            order.ship();

            // when & then
            assertThatThrownBy(order::cancel)
                    .isInstanceOf(OrderException.class);
        }
    }

    @Nested
    @DisplayName("주문 금액 계산")
    class CalculateAmountTest {

        @Test
        @DisplayName("주문 상품 추가 시 총 금액이 계산된다")
        void addOrderItem_RecalculatesTotalAmount() {
            // given
            Order order = createOrder();

            OrderItem item1 = OrderItem.builder()
                    .productId(1L)
                    .productName("상품1")
                    .unitPrice(Money.of(10000L))
                    .quantity(2)
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .productId(2L)
                    .productName("상품2")
                    .unitPrice(Money.of(20000L))
                    .quantity(1)
                    .build();

            // when
            order.addOrderItem(item1);
            order.addOrderItem(item2);

            // then
            assertThat(order.getTotalAmount().getAmount()).isEqualTo(40000L);
        }

        @Test
        @DisplayName("50000원 이상 주문 시 배송비가 무료다")
        void addOrderItem_FreeDeliveryOver50000() {
            // given
            Order order = createOrder();

            OrderItem item = OrderItem.builder()
                    .productId(1L)
                    .productName("고가 상품")
                    .unitPrice(Money.of(60000L))
                    .quantity(1)
                    .build();

            // when
            order.addOrderItem(item);

            // then
            assertThat(order.getDeliveryFee().getAmount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("50000원 미만 주문 시 배송비 3000원이 부과된다")
        void addOrderItem_DeliveryFee3000Under50000() {
            // given
            Order order = createOrder();

            OrderItem item = OrderItem.builder()
                    .productId(1L)
                    .productName("저가 상품")
                    .unitPrice(Money.of(30000L))
                    .quantity(1)
                    .build();

            // when
            order.addOrderItem(item);

            // then
            assertThat(order.getDeliveryFee().getAmount()).isEqualTo(3000L);
        }
    }

    private Order createOrder() {
        ShippingInfo shippingInfo = ShippingInfo.builder()
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .zipCode("12345")
                .address1("서울시 강남구")
                .address2("101동 1001호")
                .build();

        return Order.builder()
                .memberId(1L)
                .shippingInfo(shippingInfo)
                .build();
    }

    private void addOrderItem(Order order) {
        OrderItem item = OrderItem.builder()
                .productId(1L)
                .productName("테스트 상품")
                .unitPrice(Money.of(10000L))
                .quantity(1)
                .build();
        order.addOrderItem(item);
    }
}
