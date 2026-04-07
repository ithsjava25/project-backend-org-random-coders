package org.example.vet1177.controller;

import jakarta.validation.Valid;
import org.example.vet1177.dto.request.comment.CreateCommentRequest;
import org.example.vet1177.dto.request.comment.UpdateCommentRequest;
import org.example.vet1177.dto.response.comment.CommentResponse;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // POST /api/comments
    @PostMapping
    @Transactional
    public ResponseEntity<CommentResponse> create(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("POST /api/comments recordId={}", request.recordId());
        return ResponseEntity.ok(
                CommentResponse.from(
                        commentService.create(
                                request.recordId(),
                                request.body(),
                                currentUser
                        )
                )
        );
    }

    // GET /api/comments/record/{recordId}
    @GetMapping("/record/{recordId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentResponse>> getByRecord(
            @PathVariable UUID recordId,
            @AuthenticationPrincipal User currentUser) {

        log.info("GET /api/comments/record/{}", recordId);
        return ResponseEntity.ok(
                commentService.getByRecord(recordId, currentUser)
                        .stream()
                        .map(CommentResponse::from)
                        .toList()
        );
    }

    // PUT /api/comments/{id}
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<CommentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("PUT /api/comments/{}", id);
        return ResponseEntity.ok(
                CommentResponse.from(
                        commentService.update(id, request.body(), currentUser)
                )
        );
    }
    // DELETE /api/comments/{id}
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        log.info("DELETE /api/comments/{}", id);
        commentService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // CommentController — lägg till currentUser
    @GetMapping("/record/{recordId}/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Long> countByRecord(
            @PathVariable UUID recordId,
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/comments/record/{}/count", recordId);
        return ResponseEntity.ok(
                commentService.countByRecord(recordId, currentUser)
        );
    }




}