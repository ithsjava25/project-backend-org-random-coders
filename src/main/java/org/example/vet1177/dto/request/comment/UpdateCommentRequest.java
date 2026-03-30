package org.example.vet1177.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(

        @NotBlank(message = "Kommentar får inte vara tom")
        @Size(max = 5000, message = "Kommentar får max vara 5000 tecken")
        String body

) {}