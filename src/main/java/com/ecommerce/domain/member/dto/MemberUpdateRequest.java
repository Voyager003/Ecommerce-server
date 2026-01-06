package com.ecommerce.domain.member.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class MemberUpdateRequest {

    @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다")
    private String name;

    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "유효한 휴대폰 번호 형식이 아닙니다")
    private String phoneNumber;
}
