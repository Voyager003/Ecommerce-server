package com.ecommerce.domain.order.dto;

import com.ecommerce.domain.order.domain.ShippingInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShippingInfoResponse {

    private String recipientName;
    private String recipientPhone;
    private String zipCode;
    private String address1;
    private String address2;
    private String deliveryMessage;

    public static ShippingInfoResponse from(ShippingInfo info) {
        if (info == null) {
            return null;
        }
        return ShippingInfoResponse.builder()
                .recipientName(info.getRecipientName())
                .recipientPhone(info.getRecipientPhone())
                .zipCode(info.getZipCode())
                .address1(info.getAddress1())
                .address2(info.getAddress2())
                .deliveryMessage(info.getDeliveryMessage())
                .build();
    }
}
