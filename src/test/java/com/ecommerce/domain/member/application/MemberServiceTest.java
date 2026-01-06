package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.dao.MemberRepository;
import com.ecommerce.domain.member.domain.Member;
import com.ecommerce.domain.member.domain.MemberStatus;
import com.ecommerce.domain.member.dto.MemberResponse;
import com.ecommerce.domain.member.dto.MemberUpdateRequest;
import com.ecommerce.domain.member.dto.PasswordChangeRequest;
import com.ecommerce.domain.member.dto.SignupRequest;
import com.ecommerce.domain.member.exception.MemberException;
import com.ecommerce.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("회원가입")
    class SignupTest {

        private SignupRequest request;

        @BeforeEach
        void setUp() {
            request = createSignupRequest("test@example.com", "password1!", "테스트", "010-1234-5678");
        }

        @Test
        @DisplayName("정상적으로 회원가입에 성공한다")
        void signup_Success() {
            // given
            given(memberRepository.existsByEmail(anyString())).willReturn(false);
            given(memberRepository.findRecentlyWithdrawnByEmail(anyString(), any(), any()))
                    .willReturn(Optional.empty());
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");

            Member savedMember = createMember(1L, "test@example.com", "테스트");
            given(memberRepository.save(any(Member.class))).willReturn(savedMember);

            // when
            MemberResponse response = memberService.signup(request);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getName()).isEqualTo("테스트");
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 가입 시 예외가 발생한다")
        void signup_EmailDuplicated_ThrowsException() {
            // given
            given(memberRepository.existsByEmail(anyString())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.signup(request))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EMAIL_DUPLICATED);
                    });
        }

        @Test
        @DisplayName("30일 이내에 탈퇴한 이메일로 가입 시 예외가 발생한다")
        void signup_RecentlyWithdrawn_ThrowsException() {
            // given
            given(memberRepository.existsByEmail(anyString())).willReturn(false);
            Member withdrawnMember = createMember(1L, "test@example.com", "테스트");
            given(memberRepository.findRecentlyWithdrawnByEmail(anyString(), eq(MemberStatus.WITHDRAWN), any()))
                    .willReturn(Optional.of(withdrawnMember));

            // when & then
            assertThatThrownBy(() -> memberService.signup(request))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EMAIL_RECENTLY_WITHDRAWN);
                    });
        }
    }

    @Nested
    @DisplayName("회원 정보 조회")
    class GetMemberTest {

        @Test
        @DisplayName("회원 정보를 정상적으로 조회한다")
        void getMember_Success() {
            // given
            Member member = createMember(1L, "test@example.com", "테스트");
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when
            MemberResponse response = memberService.getMember(1L);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 회원 조회 시 예외가 발생한다")
        void getMember_NotFound_ThrowsException() {
            // given
            given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMember(999L))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                    });
        }
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateMemberTest {

        @Test
        @DisplayName("회원 정보를 정상적으로 수정한다")
        void updateMember_Success() {
            // given
            Member member = createMember(1L, "test@example.com", "테스트");
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            MemberUpdateRequest request = createMemberUpdateRequest("수정된이름", "010-9999-8888");

            // when
            MemberResponse response = memberService.updateMember(1L, request);

            // then
            assertThat(response.getName()).isEqualTo("수정된이름");
            assertThat(response.getPhoneNumber()).isEqualTo("010-9999-8888");
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class ChangePasswordTest {

        @Test
        @DisplayName("비밀번호를 정상적으로 변경한다")
        void changePassword_Success() {
            // given
            Member member = createMember(1L, "test@example.com", "테스트");
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
            given(passwordEncoder.encode(anyString())).willReturn("newEncodedPassword");

            PasswordChangeRequest request = createPasswordChangeRequest("currentPassword", "newPassword1!");

            // when
            memberService.changePassword(1L, request);

            // then
            verify(passwordEncoder).encode("newPassword1!");
        }

        @Test
        @DisplayName("현재 비밀번호가 일치하지 않으면 예외가 발생한다")
        void changePassword_InvalidCurrentPassword_ThrowsException() {
            // given
            Member member = createMember(1L, "test@example.com", "테스트");
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            PasswordChangeRequest request = createPasswordChangeRequest("wrongPassword", "newPassword1!");

            // when & then
            assertThatThrownBy(() -> memberService.changePassword(1L, request))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
                    });
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class WithdrawTest {

        @Test
        @DisplayName("회원 탈퇴가 정상적으로 처리된다")
        void withdraw_Success() {
            // given
            Member member = createMember(1L, "test@example.com", "테스트");
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when
            memberService.withdraw(1L);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        }
    }

    private SignupRequest createSignupRequest(String email, String password, String name, String phoneNumber) {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "phoneNumber", phoneNumber);
        return request;
    }

    private MemberUpdateRequest createMemberUpdateRequest(String name, String phoneNumber) {
        MemberUpdateRequest request = new MemberUpdateRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "phoneNumber", phoneNumber);
        return request;
    }

    private PasswordChangeRequest createPasswordChangeRequest(String currentPassword, String newPassword) {
        PasswordChangeRequest request = new PasswordChangeRequest();
        ReflectionTestUtils.setField(request, "currentPassword", currentPassword);
        ReflectionTestUtils.setField(request, "newPassword", newPassword);
        return request;
    }

    private Member createMember(Long id, String email, String name) {
        Member member = Member.builder()
                .email(email)
                .password("encodedPassword")
                .name(name)
                .phoneNumber("010-1234-5678")
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "createdAt", LocalDateTime.now());
        return member;
    }
}
