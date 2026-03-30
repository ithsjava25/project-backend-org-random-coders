package org.example.vet1177.services;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.CommentPolicy;
import org.example.vet1177.repository.CommentRepository;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final CommentPolicy commentPolicy;

    public CommentService(
            CommentRepository commentRepository,
            MedicalRecordRepository medicalRecordRepository,
            CommentPolicy commentPolicy) {
        this.commentRepository = commentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.commentPolicy = commentPolicy;
    }

    public Comment create(UUID recordId, String body, User currentUser) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", recordId));

        commentPolicy.canCreate(currentUser, record);  // ← en rad istället för switch

        Comment comment = new Comment();
        comment.setMedicalRecord(record);
        comment.setAuthor(currentUser);
        comment.setBody(body);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getByRecord(UUID recordId, User currentUser) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", recordId));

        commentPolicy.canView(currentUser, record);  // ← en rad

        return commentRepository.findByMedicalRecordIdOrderByCreatedAtAsc(recordId);
    }

    public Comment update(UUID commentId, String body, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        commentPolicy.canUpdate(currentUser, comment);  // ← en rad

        comment.setBody(body);
        return commentRepository.save(comment);
    }

    public void delete(UUID commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        commentPolicy.canDelete(currentUser, comment);  // ← en rad

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public long countByRecord(UUID recordId) {
        return commentRepository.countByMedicalRecordId(recordId);
    }
}