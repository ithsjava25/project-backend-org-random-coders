package org.example.vet1177.services;

import org.example.vet1177.entities.OrphanedS3Object;
import org.example.vet1177.repository.OrphanedS3ObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OrphanedS3Processor {

    private static final Logger log = LoggerFactory.getLogger(OrphanedS3Processor.class);
    private final OrphanedS3ObjectRepository repository;
    private final FileStorageService fileStorageService;

    public OrphanedS3Processor(OrphanedS3ObjectRepository repository,
                               FileStorageService fileStorageService) {
        this.repository = repository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOrphan(OrphanedS3Object orphan, int maxRetries) {
        try {
            // Försök radera från S3/MinIO
            fileStorageService.delete(orphan.getS3Key());

            // Ta bort från kön vid framgång
            repository.delete(orphan);
            log.info("Successfully cleaned up orphaned S3 object: {}", orphan.getS3Key());

        } catch (Exception e) {
            // Vid fel: uppdatera räknare och felmeddelande i en egen transaktion
            orphan.setRetryCount(orphan.getRetryCount() + 1);
            orphan.setLastAttemptAt(Instant.now());

            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error during retry";
            if (errorMessage.length() > 1024) {
                errorMessage = errorMessage.substring(0, 1021) + "...";
            }
            orphan.setLastError(errorMessage);

            repository.save(orphan);
            log.warn("Retry failed for S3 object {}. Attempt {}/{}",
                    orphan.getS3Key(), orphan.getRetryCount(), maxRetries);
        }
    }
}