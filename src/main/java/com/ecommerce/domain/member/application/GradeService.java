package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.dao.MemberRepository;
import com.ecommerce.domain.member.domain.Member;
import com.ecommerce.domain.member.domain.MemberGrade;
import com.ecommerce.domain.member.exception.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

    private final MemberRepository memberRepository;
    private final GradeBenefitProvider gradeBenefitProvider;

    public MemberGrade getMemberGrade(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
        return member.getGrade();
    }

    public GradeBenefitProvider.GradeBenefit getGradeBenefits(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
        return gradeBenefitProvider.getBenefitsWithProgress(
                member.getGrade(), member.getTotalPurchaseAmount());
    }

    @Transactional
    public MemberGrade addPurchaseAndRecalculate(Long memberId, long amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);

        MemberGrade previousGrade = member.getGrade();
        member.addPurchaseAmount(amount);
        member.recalculateGrade();

        MemberGrade newGrade = member.getGrade();

        if (newGrade != previousGrade) {
            log.info("Member {} grade upgraded: {} -> {}", memberId, previousGrade, newGrade);
        }

        return newGrade;
    }

    @Transactional
    public void recalculateGrade(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);
        member.recalculateGrade();
    }

    @Transactional
    public int recalculateAllGrades() {
        List<Member> members = memberRepository.findByStatusActive();
        int updatedCount = 0;

        for (Member member : members) {
            MemberGrade previousGrade = member.getGrade();
            member.recalculateGrade();

            if (member.getGrade() != previousGrade) {
                updatedCount++;
                log.info("Member {} grade changed: {} -> {}",
                        member.getId(), previousGrade, member.getGrade());
            }
        }

        log.info("Grade recalculation completed. Total: {}, Updated: {}", members.size(), updatedCount);
        return updatedCount;
    }

    public boolean isEligibleForUpgrade(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);

        MemberGrade currentGrade = member.getGrade();
        MemberGrade calculatedGrade = MemberGrade.calculateGrade(member.getTotalPurchaseAmount());

        return calculatedGrade.ordinal() > currentGrade.ordinal();
    }

    public long getAmountToNextGrade(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);

        GradeBenefitProvider.GradeBenefit benefits = gradeBenefitProvider.getBenefitsWithProgress(
                member.getGrade(), member.getTotalPurchaseAmount());

        return benefits.amountToNextGrade() != null ? benefits.amountToNextGrade() : 0L;
    }
}
