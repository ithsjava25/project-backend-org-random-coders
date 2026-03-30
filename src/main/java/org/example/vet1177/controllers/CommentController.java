package org.example.vet1177.controllers;

import jakarta.validation.Valid;
import org.example.vet1177.dto.request.comment.CreateCommentRequest;
import org.example.vet1177.dto.request.comment.UpdateCommentRequest;
import org.example.vet1177.dto.response.comment.CommentResponse;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

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

        return ResponseEntity.ok(
                CommentResponse.from(
                        commentService.update(id, request.body(), currentUser)
                )
        );
    }




}