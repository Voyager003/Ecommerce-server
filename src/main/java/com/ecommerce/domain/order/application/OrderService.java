package com.ecommerce.domain.order.application;

import com.ecommerce.domain.inventory.application.InventoryService;
import com.ecommerce.domain.model.Money;
import com.ecommerce.domain.order.dao.OrderRepository;
import com.ecommerce.domain.order.domain.Order;
import com.ecommerce.domain.order.domain.OrderItem;
import com.ecommerce.domain.order.domain.OrderStatus;
import com.ecommerce.domain.order.domain.ShippingInfo;
import com.ecommerce.domain.order.dto.*;
import com.ecommerce.domain.order.exception.OrderException;
import com.ecommerce.domain.product.application.ProductService;
import com.ecommerce.domain.product.domain.Product;
import com.ecommerce.domain.product.domain.ProductOption;
import com.ecommerce.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderCreateRequest request) {
        ShippingInfo shippingInfo = createShippingInfo(request.getShippingInfo());

        Order order = Order.builder()
                .memberId(memberId)
                .shippingInfo(shippingInfo)
                .couponId(request.getCouponId())
                .build();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productService.findByIdWithOptions(itemRequest.getProductId());

            if (!product.isPurchasable()) {
                throw OrderException.invalidStatus();
            }

            if (!inventoryService.hasStock(itemRequest.getProductId(),
                    itemRequest.getProductOptionId(), itemRequest.getQuantity())) {
                throw OrderException.invalidStatus();
            }

            Money unitPrice = product.getSellingPrice();
            String optionName = null;

            if (itemRequest.getProductOptionId() != null) {
                ProductOption option = product.getOptions().stream()
                        .filter(o -> o.getId().equals(itemRequest.getProductOptionId()))
                        .findFirst()
                        .orElseThrow(OrderException::invalidStatus);
                unitPrice = option.getTotalPrice();
                optionName = option.getName();
            }

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productOptionId(itemRequest.getProductOptionId())
                    .productName(product.getName())
                    .optionName(optionName)
                    .unitPrice(unitPrice)
                    .quantity(itemRequest.getQuantity())
                    .build();

            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }

    public OrderResponse getOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(OrderException::notFound);

        validateOrderOwner(order, memberId);
        return OrderResponse.from(order);
    }

    public OrderResponse getOrderByNumber(Long memberId, String orderNumber) {
        Order order = orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(OrderException::notFound);

        validateOrderOwner(order, memberId);
        return OrderResponse.from(order);
    }

    public PageResponse<OrderResponse> getOrders(Long memberId, int page, int size) {
        Page<Order> orderPage = orderRepository.findByMemberIdOrderByCreatedAtDesc(
                memberId, PageRequest.of(page, size));

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .toList();

        return PageResponse.of(content, page, size,
                orderPage.getTotalElements(), orderPage.getTotalPages());
    }

    @Transactional
    public OrderResponse cancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(OrderException::notFound);

        validateOrderOwner(order, memberId);

        if (!order.isCancellable()) {
            throw OrderException.cannotCancel();
        }

        for (OrderItem item : order.getOrderItems()) {
            inventoryService.restoreStock(
                    item.getProductId(),
                    item.getProductOptionId(),
                    item.getQuantity(),
                    order.getId()
            );
        }

        order.cancel();
        return OrderResponse.from(order);
    }

    @Transactional
    public void markAsPaid(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(OrderException::notFound);

        if (!order.isPending()) {
            throw OrderException.alreadyPaid();
        }

        for (OrderItem item : order.getOrderItems()) {
            inventoryService.deductStock(
                    item.getProductId(),
                    item.getProductOptionId(),
                    item.getQuantity(),
                    order.getId()
            );
        }

        order.markAsPaid();
    }

    public boolean hasPendingOrders(Long memberId) {
        List<OrderStatus> pendingStatuses = List.of(
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PAID,
                OrderStatus.PREPARING,
                OrderStatus.SHIPPED
        );
        return orderRepository.existsPendingOrder(memberId, pendingStatuses);
    }

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(OrderException::notFound);
    }

    private void validateOrderOwner(Order order, Long memberId) {
        if (!order.getMemberId().equals(memberId)) {
            throw OrderException.notFound();
        }
    }

    private ShippingInfo createShippingInfo(ShippingInfoRequest request) {
        return ShippingInfo.builder()
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .zipCode(request.getZipCode())
                .address1(request.getAddress1())
                .address2(request.getAddress2())
                .deliveryMessage(request.getDeliveryMessage())
                .build();
    }
}
