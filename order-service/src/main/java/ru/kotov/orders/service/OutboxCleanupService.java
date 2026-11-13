package ru.kotov.orders.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.orders.repository.OutboxRepository;

import java.time.LocalDateTime;

@Service
public class OutboxCleanupService {

    private static final Logger log = LoggerFactory.getLogger(OutboxCleanupService.class);

    private static final int RETENTION_DAYS = 30;

    private final OutboxRepository outboxRepository;

    public OutboxCleanupService(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(cron = "0 0 2 * * *") // ежедневно в 02:00
    @Transactional
    public void cleanupOldPublishedEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = outboxRepository.deleteOldPublishedEvents(cutoff);
        log.info("Outbox cleanup: deleted {} events older than {} days (before {})",
                deleted, RETENTION_DAYS, cutoff);
    }
}
