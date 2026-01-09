package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.dao.MemberRepository;
import com.ecommerce.domain.member.domain.Member;
import com.ecommerce.domain.member.domain.MemberStatus;
import com.ecommerce.domain.member.dto.MemberResponse;
import com.ecommerce.domain.member.dto.MemberUpdateRequest;
import com.ecommerce.domain.member.dto.PasswordChangeRequest;
import com.ecommerce.domain.member.dto.SignupRequest;
import com.ecommerce.domain.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final int REJOIN_RESTRICTION_DAYS = 30;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        validateEmailNotDuplicated(request.getEmail());
        validateNotRecentlyWithdrawn(request.getEmail());

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Member savedMember = memberRepository.save(member);
        return MemberResponse.from(savedMember);
    }

    public MemberResponse getMember(Long memberId) {
        Member member = findMemberById(memberId);
        return MemberResponse.from(member);
    }

    @Transactional
    public MemberResponse updateMember(Long memberId, MemberUpdateRequest request) {
        Member member = findMemberById(memberId);
        member.updateInfo(request.getName(), request.getPhoneNumber());
        return MemberResponse.from(member);
    }

    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        Member member = findMemberById(memberId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw MemberException.invalidPassword();
        }

        member.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = findMemberById(memberId);
        member.withdraw();
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberException::notFound);
    }

    private void validateEmailNotDuplicated(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw MemberException.emailDuplicated();
        }
    }

    private void validateNotRecentlyWithdrawn(String email) {
        LocalDateTime withdrawnAfter = LocalDateTime.now().minusDays(REJOIN_RESTRICTION_DAYS);
        memberRepository.findRecentlyWithdrawnByEmail(email, MemberStatus.WITHDRAWN, withdrawnAfter)
                .ifPresent(member -> {
                    throw MemberException.emailRecentlyWithdrawn();
                });
    }
}
