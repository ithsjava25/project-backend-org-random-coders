package org.example.vet1177.repository;

import org.example.vet1177.entities.OrphanedS3Object;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrphanedS3ObjectRepository extends JpaRepository<OrphanedS3Object, UUID> {

    List<OrphanedS3Object> findTop20ByRetryCountLessThanOrderByLastAttemptAtAsc(int maxRetries);
}
