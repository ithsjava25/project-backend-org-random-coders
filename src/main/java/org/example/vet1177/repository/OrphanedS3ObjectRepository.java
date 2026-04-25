package org.example.vet1177.repository;

import org.example.vet1177.entities.OrphanedS3Object;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrphanedS3ObjectRepository extends JpaRepository<OrphanedS3Object, UUID> {

    @Query("""
       SELECT o FROM OrphanedS3Object o 
       WHERE o.retryCount < :maxRetries 
       ORDER BY o.lastAttemptAt ASC NULLS FIRST, o.createdAt ASC
       """)

    List<OrphanedS3Object> findNextBatchToProcess(@Param("maxRetries") int maxRetries, Pageable pageable);
}
