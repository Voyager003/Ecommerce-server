package com.ecommerce.domain.member.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradeScheduler {

    private final GradeService gradeService;

    @Scheduled(cron = "0 0 0 1 * *")
    public void recalculateGradesMonthly() {
        log.info("Starting monthly grade recalculation...");

        try {
            int updatedCount = gradeService.recalculateAllGrades();
            log.info("Monthly grade recalculation completed. Updated {} members.", updatedCount);
        } catch (Exception e) {
            log.error("Failed to recalculate grades", e);
        }
    }
}
