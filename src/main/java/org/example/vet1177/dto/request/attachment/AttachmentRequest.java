package org.example.vet1177.dto.request.attachment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AttachmentRequest(
        @NotNull(message = "Journal-ID får inte vara tomt")
        UUID recordId,

        @Size(max = 500, message = "Beskrivningen får vara max 500 tecken")
        String description
) {}
