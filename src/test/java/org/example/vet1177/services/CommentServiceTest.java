package org.example.vet1177.services;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.CommentPolicy;
import org.example.vet1177.repository.CommentRepository;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private CommentPolicy commentPolicy;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private CommentService commentService;

    private User currentUser;
    private MedicalRecord record;
    private Comment comment;
    private UUID recordId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        currentUser = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);
        record = new MedicalRecord();
        record.setId(recordId);

        comment = new Comment();
        comment.setBody("En kommentar.");
        comment.setAuthor(currentUser);
        comment.setMedicalRecord(record);
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldSaveAndReturnComment() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.create(recordId, "En kommentar.", currentUser);

        assertThat(result).isEqualTo(comment);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void create_shouldCallPolicyCanCreate() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentService.create(recordId, "En kommentar.", currentUser);

        verify(commentPolicy).canCreate(currentUser, record);
    }

    @Test
    void create_shouldLogActivity() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        commentService.create(recordId, "En kommentar.", currentUser);

        verify(activityLogService).log(ActivityType.COMMENT_ADDED, "Kommentar skapad", currentUser, record);
    }

    @Test
    void create_whenRecordNotFound_shouldThrowResourceNotFoundException() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(recordId, "En kommentar.", currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getByRecord
    // -------------------------------------------------------------------------

    @Test
    void getByRecord_shouldReturnCommentsForRecord() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(commentRepository.findByMedicalRecordIdOrderByCreatedAtAsc(recordId)).thenReturn(List.of(comment));

        List<Comment> result = commentService.getByRecord(recordId, currentUser);

        assertThat(result).containsExactly(comment);
    }

    @Test
    void getByRecord_shouldCallPolicyCanView() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(commentRepository.findByMedicalRecordIdOrderByCreatedAtAsc(recordId)).thenReturn(List.of());

        commentService.getByRecord(recordId, currentUser);

        verify(commentPolicy).canView(currentUser, record);
    }

    @Test
    void getByRecord_whenRecordNotFound_shouldThrowResourceNotFoundException() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getByRecord(recordId, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).findByMedicalRecordIdOrderByCreatedAtAsc(any());
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_shouldSaveAndReturnUpdatedComment() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.update(commentId, "Uppdaterad text.", currentUser);

        assertThat(result.getBody()).isEqualTo("Uppdaterad text.");
        verify(commentRepository).save(comment);
    }

    @Test
    void update_shouldCallPolicyCanUpdate() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        commentService.update(commentId, "Uppdaterad text.", currentUser);

        verify(commentPolicy).canUpdate(currentUser, comment);
    }

    @Test
    void update_shouldLogActivity() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        commentService.update(commentId, "Uppdaterad text.", currentUser);

        verify(activityLogService).log(ActivityType.UPDATED, "Kommentar uppdaterad", currentUser, record);
    }

    @Test
    void update_whenCommentNotFound_shouldThrowResourceNotFoundException() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.update(commentId, "Uppdaterad text.", currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldDeleteComment() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.delete(commentId, currentUser);

        verify(commentRepository).delete(comment);
    }

    @Test
    void delete_shouldCallPolicyCanDelete() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.delete(commentId, currentUser);

        verify(commentPolicy).canDelete(currentUser, comment);
    }

    @Test
    void delete_shouldLogActivity() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.delete(commentId, currentUser);

        verify(activityLogService).log(ActivityType.UPDATED, "Kommentar borttagen", currentUser, record);
    }

    @Test
    void delete_whenCommentNotFound_shouldThrowResourceNotFoundException() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.delete(commentId, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).delete(any());
    }

    // -------------------------------------------------------------------------
    // countByRecord
    // -------------------------------------------------------------------------

    @Test
    void countByRecord_shouldReturnCount() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(commentRepository.countByMedicalRecordId(recordId)).thenReturn(3L);

        long result = commentService.countByRecord(recordId, currentUser);

        assertThat(result).isEqualTo(3L);
    }

    @Test
    void countByRecord_shouldCallPolicyCanView() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(commentRepository.countByMedicalRecordId(recordId)).thenReturn(0L);

        commentService.countByRecord(recordId, currentUser);

        verify(commentPolicy).canView(currentUser, record);
    }

    @Test
    void countByRecord_whenRecordNotFound_shouldThrowResourceNotFoundException() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.countByRecord(recordId, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).countByMedicalRecordId(any());
    }
}
