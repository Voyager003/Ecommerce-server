package com.ecommerce.domain.member.api;

import com.ecommerce.domain.member.application.GradeService;
import com.ecommerce.domain.member.application.MemberService;
import com.ecommerce.domain.member.dto.GradeBenefitResponse;
import com.ecommerce.domain.member.dto.MemberResponse;
import com.ecommerce.domain.member.dto.MemberUpdateRequest;
import com.ecommerce.domain.member.dto.PasswordChangeRequest;
import com.ecommerce.global.common.ApiResponse;
import com.ecommerce.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final GradeService gradeService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MemberResponse response = memberService.getMember(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MemberUpdateRequest request) {
        MemberResponse response = memberService.updateMember(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request) {
        memberService.changePassword(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.withdraw(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @GetMapping("/me/grade")
    public ResponseEntity<ApiResponse<GradeBenefitResponse>> getMyGrade(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        var benefits = gradeService.getGradeBenefits(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.ok(GradeBenefitResponse.from(benefits)));
    }
}
