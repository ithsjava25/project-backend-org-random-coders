package org.example.vet1177.services;

import org.example.vet1177.entities.OrphanedS3Object;
import org.example.vet1177.repository.OrphanedS3ObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OrphanedS3CleanupWorker {

    private static final Logger log = LoggerFactory.getLogger(OrphanedS3CleanupWorker.class);

    private final OrphanedS3ObjectRepository repository;
    private final FileStorageService fileStorageService;

    private static final int MAX_RETRIES = 10;

    public OrphanedS3CleanupWorker(OrphanedS3ObjectRepository repository,
                                   FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Körs var 10:e minut (600 000 millisekunder).
     * fixedDelay innebär att nästa körning börjar 10 minuter efter att den förra avslutades.
     */
    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void retryDeletions() {
        // Hämta upp till 20 objekt som behöver raderas
        List<OrphanedS3Object> pending = repository.findTop20ByRetryCountLessThanOrderByLastAttemptAtAsc(MAX_RETRIES);

        if (pending.isEmpty()) {
            return;
        }

        log.info("Starting background cleanup of {} orphaned S3 objects", pending.size());

        for (OrphanedS3Object orphan : pending) {
            try {
                // Försök radera från S3/MinIO
                fileStorageService.delete(orphan.getS3Key());

                repository.delete(orphan);
                log.info("Successfully cleaned up orphaned S3 object: {}", orphan.getS3Key());

            } catch (Exception e) {
                orphan.setRetryCount(orphan.getRetryCount() + 1);
                orphan.setLastAttemptAt(Instant.now());

                String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error during retry";
                orphan.setLastError(errorMessage.substring(0, Math.min(errorMessage.length(), 1024)));

                repository.save(orphan);
                log.warn("Retry failed for S3 object {}. Attempt {}/{}",
                        orphan.getS3Key(), orphan.getRetryCount(), MAX_RETRIES);
            }
        }
    }
}
