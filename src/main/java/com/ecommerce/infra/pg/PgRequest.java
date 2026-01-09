package com.ecommerce.infra.pg;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PgRequest {

    private final String orderNumber;
    private final long amount;
    private final String cardNumber;
    private final String cardExpiry;
    private final String cardCvc;
    private final String paymentMethod;
}
