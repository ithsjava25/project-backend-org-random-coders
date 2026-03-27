package org.example.vet1177.repository;

import org.example.vet1177.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Alla kommentarer för ett ärende — sorterat äldst först
    List<Comment> findByMedicalRecordIdOrderByCreatedAtAsc(UUID recordId);

    // Alla kommentarer skrivna av en specifik användare
    List<Comment> findByAuthorId(UUID authorId);

    // Antal kommentarer på ett ärende
    long countByMedicalRecordId(UUID recordId);
}