package ru.kotov.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kotov.inventory.repository.ProcessedEventRepository;

import java.time.LocalDateTime;

@Service
public class ProcessedEventCleanupService {

    private static final Logger log = LoggerFactory.getLogger(ProcessedEventCleanupService.class);

    private static final int RETENTION_DAYS = 7;

    private final ProcessedEventRepository processedEventRepository;

    public ProcessedEventCleanupService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Scheduled(cron = "0 0 3 * * *") // ежедневно в 03:00
    @Transactional
    public void cleanupOldProcessedEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = processedEventRepository.deleteOldProcessedEvents(cutoff);
        log.info("Processed events cleanup: deleted {} records older than {} days (before {})",
                deleted, RETENTION_DAYS, cutoff);
    }
}
