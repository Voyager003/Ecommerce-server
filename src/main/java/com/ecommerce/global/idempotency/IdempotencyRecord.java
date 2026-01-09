package com.ecommerce.global.idempotency;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord {

    @Id
    @Column(length = 50)
    private String idempotencyKey;

    @Column(nullable = false, length = 50)
    private String resourceType;

    private Long resourceId;

    @Lob
    private String responseBody;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IdempotencyRecord(String idempotencyKey, String resourceType) {
        this.idempotencyKey = idempotencyKey;
        this.resourceType = resourceType;
        this.expiresAt = LocalDateTime.now().plusHours(24);
        this.createdAt = LocalDateTime.now();
    }

    public void complete(Long resourceId, String responseBody) {
        this.resourceId = resourceId;
        this.responseBody = responseBody;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isCompleted() {
        return resourceId != null;
    }
}
