package org.example.vet1177.policy;

import org.example.vet1177.entities.Attachment;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class AttachmentPolicy {

    private final MedicalRecordPolicy medicalRecordPolicy;

    // Tillåtna filtyper (MIME-types)
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    // Maxstorlek satt till 10 MB
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    public AttachmentPolicy(MedicalRecordPolicy medicalRecordPolicy) {
        this.medicalRecordPolicy = medicalRecordPolicy;
    }

    public void canUpload(User user, MedicalRecord record, String contentType, long fileSize) {

        validateFileType(contentType);
        validateFileSize(fileSize);

        medicalRecordPolicy.canUpdate(user, record);
    }


     // Kontrollerar om användaren får se eller ladda ner en bilaga.
    public void canDownload(User user, Attachment attachment) {
        medicalRecordPolicy.canView(user, attachment.getMedicalRecord());
    }


    //Kontrollerar om användaren får radera en bilaga från systemet.
    public void canDelete(User user, Attachment attachment) {

        medicalRecordPolicy.canUpdate(user, attachment.getMedicalRecord());

        switch (user.getRole()) {
            case ADMIN -> {
                // Admin har fulla rättigheter att radera.
            }
            case VET -> {
                // En veterinär får endast radera bilagor som de själva har laddat upp.
                if (attachment.getUploadedBy() == null ||
                        !attachment.getUploadedBy().getId().equals(user.getId())) {
                    throw new ForbiddenException("Du kan endast radera bilagor du själv har laddat upp.");
                }
            }
            case OWNER ->
                    throw new ForbiddenException("Djurägare har inte behörighet att radera medicinsk dokumentation.");
        }
    }

    // Hjälpmetoder för validering

    private void validateFileType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleException("Otillåtet filformat. Endast JPG, PNG och PDF accepteras.");
        }
    }

    private void validateFileSize(long fileSize) {
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException("Filen är för stor. Maxgränsen är 10 MB.");
        }
    }
}