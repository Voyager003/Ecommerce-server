package com.ecommerce.domain.member.application;

import com.ecommerce.domain.member.dao.MemberRepository;
import com.ecommerce.domain.member.dao.RefreshTokenRepository;
import com.ecommerce.domain.member.domain.Member;
import com.ecommerce.domain.member.domain.RefreshToken;
import com.ecommerce.domain.member.dto.LoginRequest;
import com.ecommerce.domain.member.dto.TokenResponse;
import com.ecommerce.domain.member.exception.MemberException;
import com.ecommerce.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(MemberException::notFound);

        if (member.isLocked()) {
            throw MemberException.accountLocked();
        }

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            member.recordLoginFailure();
            throw MemberException.invalidPassword();
        }

        member.recordLoginSuccess();
        return createTokenResponse(member);
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw MemberException.invalidRefreshToken();
        }

        if (!jwtTokenProvider.isRefreshToken(refreshTokenValue)) {
            throw MemberException.invalidRefreshToken();
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(MemberException::invalidRefreshToken);

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw MemberException.invalidRefreshToken();
        }

        Long memberId = jwtTokenProvider.getMemberId(refreshTokenValue);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberException::notFound);

        if (!member.isActive()) {
            throw MemberException.accountLocked();
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getEmail());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenValidity / 1000);
        refreshToken.updateToken(newRefreshToken, expiresAt);

        return TokenResponse.of(newAccessToken, newRefreshToken, jwtTokenProvider.getAccessTokenValidity());
    }

    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    private TokenResponse createTokenResponse(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getEmail());
        String refreshTokenValue = jwtTokenProvider.createRefreshToken(member.getId(), member.getEmail());

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenValidity / 1000);

        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        token -> token.updateToken(refreshTokenValue, expiresAt),
                        () -> refreshTokenRepository.save(
                                new RefreshToken(member.getId(), refreshTokenValue, expiresAt)
                        )
                );

        return TokenResponse.of(accessToken, refreshTokenValue, jwtTokenProvider.getAccessTokenValidity());
    }
}
