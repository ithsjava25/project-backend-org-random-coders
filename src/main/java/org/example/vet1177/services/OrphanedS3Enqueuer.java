package org.example.vet1177.services;

import org.example.vet1177.entities.OrphanedS3Object;
import org.example.vet1177.repository.OrphanedS3ObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrphanedS3Enqueuer {

    private static final Logger log = LoggerFactory.getLogger(OrphanedS3Enqueuer.class);
    private final OrphanedS3ObjectRepository repository;

    public OrphanedS3Enqueuer(OrphanedS3ObjectRepository repository) {
        this.repository = repository;
    }

    /**
     * REQUIRES_NEW tvingar Spring att pausa den nuvarande transaktionen
     * och starta en helt ny, oberoende transaktion för just denna sparning.
     * Metoden är nu idempotent (find-or-create).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueue(String s3Key, String bucket, String reason) {

        // Förbered det trunkerade felmeddelandet
        String truncatedReason = null;
        if (reason != null) {
            truncatedReason = reason.length() > 1024
                    ? reason.substring(0, 1021) + "..."
                    : reason;
        }

        final String finalReason = truncatedReason;

        // "Upsert"-logik: Hitta befintlig eller skapa ny
        repository.findByS3Key(s3Key).ifPresentOrElse(
                existing -> {
                    log.debug("Orphaned S3 key already exists: {}. Updating error info.", s3Key);
                    existing.setLastError(finalReason);
                    existing.setLastAttemptAt(null); // Återställ så att Worker kan plocka upp den direkt
                    repository.save(existing);
                },
                () -> {
                    log.info("Enqueuing new orphaned S3 object: {}", s3Key);
                    OrphanedS3Object newOrphan = new OrphanedS3Object(s3Key, bucket);
                    newOrphan.setLastError(finalReason);
                    newOrphan.setLastAttemptAt(null);
                    repository.save(newOrphan);
                }
        );
    }
}