package com.ecommerce.domain.coupon.domain;

import com.ecommerce.domain.coupon.exception.CouponException;
import com.ecommerce.domain.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_coupons",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "coupon_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status = CouponStatus.AVAILABLE;

    @Column(name = "used_order_id")
    private Long usedOrderId;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    @Builder
    public MemberCoupon(Long memberId, Coupon coupon) {
        this.memberId = memberId;
        this.coupon = coupon;
        this.status = CouponStatus.AVAILABLE;
        this.issuedAt = LocalDateTime.now();
    }

    public static MemberCoupon issue(Long memberId, Coupon coupon) {
        coupon.issue();
        return MemberCoupon.builder()
                .memberId(memberId)
                .coupon(coupon)
                .build();
    }

    public void use(Long orderId) {
        validateUsable();
        this.status = CouponStatus.USED;
        this.usedOrderId = orderId;
        this.usedAt = LocalDateTime.now();
    }

    public void restore() {
        if (this.status != CouponStatus.USED) {
            throw CouponException.cannotRestore();
        }
        this.status = CouponStatus.AVAILABLE;
        this.usedOrderId = null;
        this.usedAt = null;
    }

    public void expire() {
        if (this.status == CouponStatus.AVAILABLE) {
            this.status = CouponStatus.EXPIRED;
        }
    }

    private void validateUsable() {
        if (this.status == CouponStatus.USED) {
            throw CouponException.alreadyUsed();
        }
        if (this.status == CouponStatus.EXPIRED) {
            throw CouponException.expired();
        }
        if (coupon.isExpired()) {
            this.status = CouponStatus.EXPIRED;
            throw CouponException.expired();
        }
    }

    public boolean isUsable() {
        return this.status == CouponStatus.AVAILABLE && !coupon.isExpired();
    }

    public Long getCouponId() {
        return coupon.getId();
    }

    public String getCouponName() {
        return coupon.getName();
    }

    public CouponType getCouponType() {
        return coupon.getType();
    }
}
