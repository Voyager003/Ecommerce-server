package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.dao.MemberRepository;
import com.ecommerce.domain.member.dao.RefreshTokenRepository;
import com.ecommerce.domain.member.domain.Member;
import com.ecommerce.domain.member.domain.RefreshToken;
import com.ecommerce.domain.member.dto.LoginRequest;
import com.ecommerce.domain.member.dto.TokenResponse;
import com.ecommerce.domain.member.exception.MemberException;
import com.ecommerce.global.error.ErrorCode;
import com.ecommerce.global.security.JwtTokenProvider;
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
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenValidity", 604800000L);
    }

    @Nested
    @DisplayName("로그인")
    class LoginTest {

        @Test
        @DisplayName("정상적으로 로그인에 성공한다")
        void login_Success() {
            // given
            LoginRequest request = createLoginRequest("test@example.com", "password1!");
            Member member = createMember(1L, "test@example.com", "테스트");

            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
            given(jwtTokenProvider.createAccessToken(anyLong(), anyString())).willReturn("accessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong(), anyString())).willReturn("refreshToken");
            given(jwtTokenProvider.getAccessTokenValidity()).willReturn(1800000L);
            given(refreshTokenRepository.findByMemberId(anyLong())).willReturn(Optional.empty());

            // when
            TokenResponse response = authService.login(request);

            // then
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
        void login_MemberNotFound_ThrowsException() {
            // given
            LoginRequest request = createLoginRequest("notfound@example.com", "password1!");
            given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("잠긴 계정으로 로그인 시 예외가 발생한다")
        void login_AccountLocked_ThrowsException() {
            // given
            LoginRequest request = createLoginRequest("test@example.com", "password1!");
            Member member = createLockedMember(1L, "test@example.com", "테스트");

            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LOCKED);
                    });
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 로그인 실패 횟수가 증가한다")
        void login_InvalidPassword_IncrementsFailedCount() {
            // given
            LoginRequest request = createLoginRequest("test@example.com", "wrongPassword");
            Member member = createMember(1L, "test@example.com", "테스트");

            given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
            given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
                    });

            assertThat(member.getFailedLoginCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshTest {

        @Test
        @DisplayName("정상적으로 토큰을 갱신한다")
        void refresh_Success() {
            // given
            String refreshTokenValue = "validRefreshToken";
            Member member = createMember(1L, "test@example.com", "테스트");
            RefreshToken refreshToken = new RefreshToken(1L, refreshTokenValue,
                    LocalDateTime.now().plusDays(7));

            given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
            given(jwtTokenProvider.isRefreshToken(anyString())).willReturn(true);
            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(refreshToken));
            given(jwtTokenProvider.getMemberId(anyString())).willReturn(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(jwtTokenProvider.createAccessToken(anyLong(), anyString())).willReturn("newAccessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong(), anyString())).willReturn("newRefreshToken");
            given(jwtTokenProvider.getAccessTokenValidity()).willReturn(1800000L);

            // when
            TokenResponse response = authService.refresh(refreshTokenValue);

            // then
            assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
            assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 갱신 시 예외가 발생한다")
        void refresh_InvalidToken_ThrowsException() {
            // given
            given(jwtTokenProvider.validateToken(anyString())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.refresh("invalidToken"))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
                    });
        }

        @Test
        @DisplayName("만료된 리프레시 토큰으로 갱신 시 예외가 발생한다")
        void refresh_ExpiredToken_ThrowsException() {
            // given
            String refreshTokenValue = "expiredRefreshToken";
            RefreshToken expiredToken = new RefreshToken(1L, refreshTokenValue,
                    LocalDateTime.now().minusDays(1));

            given(jwtTokenProvider.validateToken(anyString())).willReturn(true);
            given(jwtTokenProvider.isRefreshToken(anyString())).willReturn(true);
            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.of(expiredToken));

            // when & then
            assertThatThrownBy(() -> authService.refresh(refreshTokenValue))
                    .isInstanceOf(MemberException.class)
                    .satisfies(e -> {
                        MemberException ex = (MemberException) e;
                        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
                    });

            verify(refreshTokenRepository).delete(expiredToken);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutTest {

        @Test
        @DisplayName("정상적으로 로그아웃한다")
        void logout_Success() {
            // when
            authService.logout(1L);

            // then
            verify(refreshTokenRepository).deleteByMemberId(1L);
        }
    }

    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
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

    private Member createLockedMember(Long id, String email, String name) {
        Member member = createMember(id, email, name);
        ReflectionTestUtils.setField(member, "status",
                com.ecommerce.domain.member.domain.MemberStatus.LOCKED);
        ReflectionTestUtils.setField(member, "lockedUntil", LocalDateTime.now().plusMinutes(30));
        return member;
    }
}
