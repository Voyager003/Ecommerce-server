package com.ecommerce.global.idempotency;

import com.ecommerce.global.error.BusinessException;
import com.ecommerce.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    @Transactional
    public IdempotencyResult checkAndCreate(String idempotencyKey, String resourceType) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }

        Optional<IdempotencyRecord> existing = idempotencyRepository
                .findByIdempotencyKeyAndResourceType(idempotencyKey, resourceType);

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            if (record.isCompleted()) {
                return IdempotencyResult.duplicate(record.getResourceId(), record.getResponseBody());
            }
            // 진행 중인 요청이 있음
            return IdempotencyResult.inProgress();
        }

        // 새로운 레코드 생성
        IdempotencyRecord newRecord = new IdempotencyRecord(idempotencyKey, resourceType);
        idempotencyRepository.save(newRecord);
        return IdempotencyResult.newRequest();
    }

    @Transactional
    public void complete(String idempotencyKey, String resourceType, Long resourceId, String responseBody) {
        idempotencyRepository.findByIdempotencyKeyAndResourceType(idempotencyKey, resourceType)
                .ifPresent(record -> record.complete(resourceId, responseBody));
    }

    @Transactional
    public void delete(String idempotencyKey, String resourceType) {
        idempotencyRepository.findByIdempotencyKeyAndResourceType(idempotencyKey, resourceType)
                .ifPresent(idempotencyRepository::delete);
    }

    @Scheduled(cron = "0 0 * * * *") // 매시간 실행
    @Transactional
    public void cleanupExpiredRecords() {
        int deleted = idempotencyRepository.deleteExpiredRecords(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Deleted {} expired idempotency records", deleted);
        }
    }

    public record IdempotencyResult(
            Status status,
            Long resourceId,
            String responseBody
    ) {
        public enum Status {
            NEW_REQUEST,
            DUPLICATE,
            IN_PROGRESS
        }

        public static IdempotencyResult newRequest() {
            return new IdempotencyResult(Status.NEW_REQUEST, null, null);
        }

        public static IdempotencyResult duplicate(Long resourceId, String responseBody) {
            return new IdempotencyResult(Status.DUPLICATE, resourceId, responseBody);
        }

        public static IdempotencyResult inProgress() {
            return new IdempotencyResult(Status.IN_PROGRESS, null, null);
        }

        public boolean isNewRequest() {
            return status == Status.NEW_REQUEST;
        }

        public boolean isDuplicate() {
            return status == Status.DUPLICATE;
        }
    }
}
