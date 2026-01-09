package com.ecommerce.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class AddressRequest {

    @NotBlank(message = "배송지명은 필수입니다")
    @Size(max = 50, message = "배송지명은 50자 이하여야 합니다")
    private String name;

    @NotBlank(message = "수령인 이름은 필수입니다")
    @Size(max = 50, message = "수령인 이름은 50자 이하여야 합니다")
    private String recipientName;

    @NotBlank(message = "수령인 연락처는 필수입니다")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "유효한 휴대폰 번호 형식이 아닙니다")
    private String phoneNumber;

    @NotBlank(message = "우편번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다")
    private String zipCode;

    @NotBlank(message = "기본 주소는 필수입니다")
    @Size(max = 200, message = "기본 주소는 200자 이하여야 합니다")
    private String address1;

    @Size(max = 200, message = "상세 주소는 200자 이하여야 합니다")
    private String address2;

    private boolean isDefault = false;
}
