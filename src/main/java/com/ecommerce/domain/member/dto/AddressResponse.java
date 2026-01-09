package com.ecommerce.domain.member.dto;

import com.ecommerce.domain.member.domain.Address;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressResponse {

    private Long id;
    private String name;
    private String recipientName;
    private String phoneNumber;
    private String zipCode;
    private String address1;
    private String address2;
    private boolean isDefault;

    public static AddressResponse from(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .name(address.getName())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .zipCode(address.getZipCode())
                .address1(address.getAddress1())
                .address2(address.getAddress2())
                .isDefault(address.isDefault())
                .build();
    }
}
