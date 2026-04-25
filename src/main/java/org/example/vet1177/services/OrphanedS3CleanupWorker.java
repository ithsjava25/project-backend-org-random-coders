package org.example.vet1177.services;

import org.example.vet1177.entities.OrphanedS3Object;
import org.example.vet1177.repository.OrphanedS3ObjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrphanedS3CleanupWorker {

    private static final Logger log = LoggerFactory.getLogger(OrphanedS3CleanupWorker.class);

    private final OrphanedS3ObjectRepository repository;
    private final OrphanedS3Processor processor; // Ny injektion

    private static final int MAX_RETRIES = 10;

    public OrphanedS3CleanupWorker(OrphanedS3ObjectRepository repository,
                                   OrphanedS3Processor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    @Scheduled(fixedDelay = 600000)
    public void retryDeletions() {
        List<OrphanedS3Object> pending = repository.findNextBatchToProcess(
                MAX_RETRIES,
                PageRequest.of(0, 20)
        );

        if (pending.isEmpty()) {
            return;
        }

        log.info("Starting background cleanup of {} orphaned S3 objects", pending.size());

        for (OrphanedS3Object orphan : pending) {
            processor.processOrphan(orphan, MAX_RETRIES);
        }
    }
}
