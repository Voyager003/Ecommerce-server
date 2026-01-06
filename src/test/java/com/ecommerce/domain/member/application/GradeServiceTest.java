package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.dao.MemberRepository;
import com.ecommerce.domain.member.domain.Member;
import com.ecommerce.domain.member.domain.MemberGrade;
import com.ecommerce.domain.member.exception.MemberException;
import com.ecommerce.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @InjectMocks
    private GradeService gradeService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GradeBenefitProvider gradeBenefitProvider;

    @Nested
    @DisplayName("회원 등급 조회")
    class GetMemberGradeTest {

        @Test
        @DisplayName("회원의 등급을 조회한다")
        void getMemberGrade_Success() {
            // given
            Long memberId = 1L;
            Member member = createMember(memberId, MemberGrade.GOLD, 350000L);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            MemberGrade grade = gradeService.getMemberGrade(memberId);

            // then
            assertThat(grade).isEqualTo(MemberGrade.GOLD);
        }

        @Test
        @DisplayName("존재하지 않는 회원 조회 시 예외 발생")
        void getMemberGrade_NotFound_ThrowsException() {
            // given
            Long memberId = 999L;
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> gradeService.getMemberGrade(memberId))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("구매 금액 추가 및 등급 재계산")
    class AddPurchaseAndRecalculateTest {

        @Test
        @DisplayName("구매 금액 추가 후 등급이 업그레이드된다")
        void addPurchaseAndRecalculate_UpgradesGrade() {
            // given
            Long memberId = 1L;
            Member member = createMember(memberId, MemberGrade.BRONZE, 90000L);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            MemberGrade newGrade = gradeService.addPurchaseAndRecalculate(memberId, 20000L);

            // then
            assertThat(newGrade).isEqualTo(MemberGrade.SILVER);
            assertThat(member.getTotalPurchaseAmount()).isEqualTo(110000L);
        }

        @Test
        @DisplayName("등급 기준 미달 시 등급이 유지된다")
        void addPurchaseAndRecalculate_MaintainsGrade() {
            // given
            Long memberId = 1L;
            Member member = createMember(memberId, MemberGrade.BRONZE, 50000L);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            MemberGrade newGrade = gradeService.addPurchaseAndRecalculate(memberId, 10000L);

            // then
            assertThat(newGrade).isEqualTo(MemberGrade.BRONZE);
            assertThat(member.getTotalPurchaseAmount()).isEqualTo(60000L);
        }
    }

    @Nested
    @DisplayName("전체 등급 재계산")
    class RecalculateAllGradesTest {

        @Test
        @DisplayName("모든 활성 회원의 등급을 재계산한다")
        void recalculateAllGrades_Success() {
            // given
            Member member1 = createMember(1L, MemberGrade.BRONZE, 150000L);
            Member member2 = createMember(2L, MemberGrade.SILVER, 350000L);
            given(memberRepository.findByStatusActive()).willReturn(List.of(member1, member2));

            // when
            int updatedCount = gradeService.recalculateAllGrades();

            // then
            assertThat(updatedCount).isEqualTo(2);
            assertThat(member1.getGrade()).isEqualTo(MemberGrade.SILVER);
            assertThat(member2.getGrade()).isEqualTo(MemberGrade.GOLD);
        }

        @Test
        @DisplayName("변경할 등급이 없으면 0을 반환한다")
        void recalculateAllGrades_NoChanges() {
            // given
            Member member = createMember(1L, MemberGrade.SILVER, 150000L);
            given(memberRepository.findByStatusActive()).willReturn(List.of(member));

            // when
            int updatedCount = gradeService.recalculateAllGrades();

            // then
            assertThat(updatedCount).isEqualTo(0);
            assertThat(member.getGrade()).isEqualTo(MemberGrade.SILVER);
        }
    }

    @Nested
    @DisplayName("등급 업그레이드 가능 여부")
    class IsEligibleForUpgradeTest {

        @Test
        @DisplayName("등급 업그레이드 가능 시 true 반환")
        void isEligibleForUpgrade_True() {
            // given
            Long memberId = 1L;
            Member member = createMember(memberId, MemberGrade.BRONZE, 150000L);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            boolean eligible = gradeService.isEligibleForUpgrade(memberId);

            // then
            assertThat(eligible).isTrue();
        }

        @Test
        @DisplayName("등급 업그레이드 불가 시 false 반환")
        void isEligibleForUpgrade_False() {
            // given
            Long memberId = 1L;
            Member member = createMember(memberId, MemberGrade.SILVER, 150000L);
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            boolean eligible = gradeService.isEligibleForUpgrade(memberId);

            // then
            assertThat(eligible).isFalse();
        }
    }

    private Member createMember(Long id, MemberGrade grade, long totalPurchaseAmount) {
        Member member = Member.builder()
                .email("test@test.com")
                .password("password123!")
                .name("테스트")
                .phoneNumber("01012345678")
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "grade", grade);
        ReflectionTestUtils.setField(member, "totalPurchaseAmount", totalPurchaseAmount);
        return member;
    }
}
