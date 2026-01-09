package com.ecommerce.domain.member.dto;

import com.ecommerce.domain.member.domain.Member;
import com.ecommerce.domain.member.domain.MemberGrade;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponse {

    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private MemberGrade grade;
    private long totalPurchaseAmount;
    private LocalDateTime createdAt;

    public static MemberResponse from(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .grade(member.getGrade())
                .totalPurchaseAmount(member.getTotalPurchaseAmount())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
