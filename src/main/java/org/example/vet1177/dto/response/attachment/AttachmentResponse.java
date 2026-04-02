package org.example.vet1177.dto.response.attachment;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(

        UUID id,
        UUID recordId,
        String fileName,
        String description,
        String fileType,
        Long fileSizeBytes,
        Instant uploadedAt,
        String uploadedBy,
        String downloadUrl  // Presigned link

) {
}
