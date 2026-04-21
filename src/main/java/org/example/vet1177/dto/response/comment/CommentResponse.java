package org.example.vet1177.dto.response.comment;

import org.example.vet1177.entities.Comment;
import org.example.vet1177.entities.CommentType;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID recordId,
        UUID authorId,
        String authorName,
        String body,
        CommentType type,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getMedicalRecord().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getName(),
                comment.getBody(),
                comment.getType(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
