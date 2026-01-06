package com.ecommerce.domain.member.domain;

import com.ecommerce.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int INITIAL_LOCK_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberGrade grade = MemberGrade.BRONZE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(nullable = false)
    private int failedLoginCount = 0;

    private LocalDateTime lockedUntil;

    private LocalDateTime withdrawnAt;

    @Column(nullable = false)
    private long totalPurchaseAmount = 0L;

    @Builder
    public Member(String email, String password, String name, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.grade = MemberGrade.BRONZE;
        this.status = MemberStatus.ACTIVE;
    }

    public void updateInfo(String name, String phoneNumber) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void recordLoginSuccess() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    public void recordLoginFailure() {
        this.failedLoginCount++;
        if (this.failedLoginCount >= MAX_LOGIN_ATTEMPTS) {
            int lockMinutes = calculateLockMinutes();
            this.lockedUntil = LocalDateTime.now().plusMinutes(lockMinutes);
            this.status = MemberStatus.LOCKED;
        }
    }

    private int calculateLockMinutes() {
        // 5회: 30분, 10회: 60분, 15회 이상: 24시간
        int lockCount = this.failedLoginCount / MAX_LOGIN_ATTEMPTS;
        return switch (lockCount) {
            case 1 -> INITIAL_LOCK_MINUTES;
            case 2 -> 60;
            default -> 24 * 60;
        };
    }

    public boolean isLocked() {
        if (this.status != MemberStatus.LOCKED) {
            return false;
        }
        if (this.lockedUntil != null && LocalDateTime.now().isAfter(this.lockedUntil)) {
            this.status = MemberStatus.ACTIVE;
            return false;
        }
        return true;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    public void addPurchaseAmount(long amount) {
        this.totalPurchaseAmount += amount;
    }

    public void recalculateGrade() {
        this.grade = MemberGrade.calculateGrade(this.totalPurchaseAmount);
    }
}
