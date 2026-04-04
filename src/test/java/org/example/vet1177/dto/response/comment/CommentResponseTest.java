package org.example.vet1177.dto.response.comment;

import org.example.vet1177.entities.Comment;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentResponseTest {

    private Comment comment;
    private User author;
    private MedicalRecord medicalRecord;
    private UUID commentId;
    private UUID authorId;
    private UUID recordId;


    @BeforeEach
    void setUp() throws Exception {
        commentId = UUID.randomUUID();
        authorId  = UUID.randomUUID();
        recordId  = UUID.randomUUID();

        author = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);
        setPrivateField(author, "id", authorId);

        medicalRecord = new MedicalRecord();
        medicalRecord.setId(recordId);

        comment = new Comment();
        setPrivateField(comment, "id", commentId);
        comment.setBody("Djuret är friskt och kan skrivas ut.");
        comment.setAuthor(author);
        comment.setMedicalRecord(medicalRecord);
        callProtectedMethod(comment, "onCreate");
    }

    // --- Happy path ---

    @Test
    void from_shouldMapAllFieldsCorrectly() {
        CommentResponse response = CommentResponse.from(comment);

        assertThat(response.id()).isEqualTo(commentId);
        assertThat(response.recordId()).isEqualTo(recordId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.authorName()).isEqualTo("Dr. Sara Lindqvist");
        assertThat(response.body()).isEqualTo("Djuret är friskt och kan skrivas ut.");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void from_createdAtAndUpdatedAtShouldReflectCommentTimestamps() {
        CommentResponse response = CommentResponse.from(comment);

        assertThat(response.createdAt()).isEqualTo(comment.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(comment.getUpdatedAt());
    }

    @Test
    void from_shouldMapAuthorNameFromUserEntity() {
        CommentResponse response = CommentResponse.from(comment);

        assertThat(response.authorName()).isEqualTo(author.getName());
    }

    // --- Sad paths ---

    @Test
    void from_shouldThrowWhenAuthorIsNull() {
        comment.setAuthor(null);

        assertThatThrownBy(() -> CommentResponse.from(comment))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void from_shouldThrowWhenMedicalRecordIsNull() {
        comment.setMedicalRecord(null);

        assertThatThrownBy(() -> CommentResponse.from(comment))
                .isInstanceOf(NullPointerException.class);
    }

    // --- Helpers ---

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void callProtectedMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }
}
