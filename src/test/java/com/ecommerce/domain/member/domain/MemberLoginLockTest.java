package com.ecommerce.domain.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MemberLoginLockTest {

    @Nested
    @DisplayName("로그인 실패 횟수 기록")
    class RecordLoginFailureTest {

        @Test
        @DisplayName("로그인 실패 시 실패 횟수가 증가한다")
        void recordLoginFailure_IncrementsCount() {
            // given
            Member member = createMember();

            // when
            member.recordLoginFailure();

            // then
            assertThat(member.getFailedLoginCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("5회 연속 실패 시 계정이 30분간 잠긴다")
        void recordLoginFailure_5Times_LocksFor30Minutes() {
            // given
            Member member = createMember();

            // when
            for (int i = 0; i < 5; i++) {
                member.recordLoginFailure();
            }

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.LOCKED);
            assertThat(member.getLockedUntil()).isAfter(LocalDateTime.now());
            assertThat(member.getLockedUntil()).isBefore(LocalDateTime.now().plusMinutes(31));
        }

        @Test
        @DisplayName("10회 연속 실패 시 계정이 60분간 잠긴다")
        void recordLoginFailure_10Times_LocksFor60Minutes() {
            // given
            Member member = createMember();

            // when
            for (int i = 0; i < 10; i++) {
                member.recordLoginFailure();
            }

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.LOCKED);
            assertThat(member.getLockedUntil()).isAfter(LocalDateTime.now().plusMinutes(59));
        }

        @Test
        @DisplayName("15회 이상 연속 실패 시 계정이 24시간 잠긴다")
        void recordLoginFailure_15Times_LocksFor24Hours() {
            // given
            Member member = createMember();

            // when
            for (int i = 0; i < 15; i++) {
                member.recordLoginFailure();
            }

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.LOCKED);
            assertThat(member.getLockedUntil()).isAfter(LocalDateTime.now().plusHours(23));
        }
    }

    @Nested
    @DisplayName("로그인 성공 기록")
    class RecordLoginSuccessTest {

        @Test
        @DisplayName("로그인 성공 시 실패 횟수와 잠금이 초기화된다")
        void recordLoginSuccess_ResetsFailedCount() {
            // given
            Member member = createMember();
            member.recordLoginFailure();
            member.recordLoginFailure();
            member.recordLoginFailure();

            // when
            member.recordLoginSuccess();

            // then
            assertThat(member.getFailedLoginCount()).isEqualTo(0);
            assertThat(member.getLockedUntil()).isNull();
        }
    }

    @Nested
    @DisplayName("잠금 상태 확인")
    class IsLockedTest {

        @Test
        @DisplayName("ACTIVE 상태의 회원은 잠금 상태가 아니다")
        void isLocked_ActiveMember_ReturnsFalse() {
            // given
            Member member = createMember();

            // when & then
            assertThat(member.isLocked()).isFalse();
        }

        @Test
        @DisplayName("LOCKED 상태이고 잠금 시간이 남아있으면 잠금 상태이다")
        void isLocked_LockedWithTimeRemaining_ReturnsTrue() {
            // given
            Member member = createMember();
            for (int i = 0; i < 5; i++) {
                member.recordLoginFailure();
            }

            // when & then
            assertThat(member.isLocked()).isTrue();
        }

        @Test
        @DisplayName("LOCKED 상태이지만 잠금 시간이 지나면 자동으로 ACTIVE로 변경된다")
        void isLocked_LockExpired_BecomesActiveAndReturnsFalse() {
            // given
            Member member = createMember();
            for (int i = 0; i < 5; i++) {
                member.recordLoginFailure();
            }

            // 잠금 시간을 과거로 설정
            ReflectionTestUtils.setField(member, "lockedUntil", LocalDateTime.now().minusMinutes(1));

            // when & then
            assertThat(member.isLocked()).isFalse();
            assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class WithdrawTest {

        @Test
        @DisplayName("회원 탈퇴 시 상태가 WITHDRAWN으로 변경되고 탈퇴 시간이 기록된다")
        void withdraw_ChangesStatusAndRecordsTime() {
            // given
            Member member = createMember();

            // when
            member.withdraw();

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
            assertThat(member.getWithdrawnAt()).isNotNull();
            assertThat(member.isActive()).isFalse();
        }
    }

    private Member createMember() {
        return Member.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("테스트")
                .phoneNumber("010-1234-5678")
                .build();
    }
}
