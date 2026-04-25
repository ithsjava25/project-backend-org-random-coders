package org.example.vet1177.services;

import org.example.vet1177.entities.OrphanedS3Object;
import org.example.vet1177.repository.OrphanedS3ObjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrphanedS3Enqueuer {

    private final OrphanedS3ObjectRepository repository;

    public OrphanedS3Enqueuer(OrphanedS3ObjectRepository repository) {
        this.repository = repository;
    }

    /**
     * REQUIRES_NEW tvingar Spring att pausa den nuvarande transaktionen
     * och starta en helt ny, oberoende transaktion för just detta sparning.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enqueue(String s3Key, String bucket, String reason) {
        OrphanedS3Object orphan = new OrphanedS3Object(s3Key, bucket);

        // Vi sätter lastAttemptAt till nu direkt så att kön vet att den är ny
        orphan.setLastAttemptAt(null); // Eller Instant.now() beroende på hur vi vill att NULLS FIRST ska reagera

        if (reason != null) {
            // Säkerställ att vi inte kraschar på för långa felmeddelanden
            String truncatedReason = reason.length() > 1024
                    ? reason.substring(0, 1021) + "..."
                    : reason;
            orphan.setLastError(truncatedReason);
        }

        repository.save(orphan);
    }
}
