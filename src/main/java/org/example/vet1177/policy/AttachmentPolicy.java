package org.example.vet1177.policy;

import org.example.vet1177.entities.Attachment;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.RecordStatus;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;


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

    // VET får alltid ladda upp på öppna ärenden (oavsett klinik) — remiss- och konsultflöden
    // kräver att remitterande veterinär kan bifoga filer på en annan kliniks ärende.
    // OWNER får ladda upp på egna öppna ärenden. CLOSED spärrar både VET och OWNER.
    // ADMIN har inga restriktioner (t.ex. retroaktiv arkivering).
    public void canUpload(User user, MedicalRecord record, String contentType, long fileSize) {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("Filen kan inte vara tom (0 bytes).");
        }

        validateFileType(contentType);
        validateFileSize(fileSize);

        switch (user.getRole()) {
            case ADMIN -> {}
            case VET -> {
                if (record.getStatus() == RecordStatus.CLOSED)
                    throw new ForbiddenException("Bilagor kan inte laddas upp på stängda ärenden");
            }
            case OWNER -> {
                if (!record.getOwner().getId().equals(user.getId()))
                    throw new ForbiddenException("Du kan bara ladda upp bilagor på egna ärenden");
                if (record.getStatus() == RecordStatus.CLOSED)
                    throw new ForbiddenException("Bilagor kan inte laddas upp på stängda ärenden");
            }
        }
    }


     // Kontrollerar om användaren får se eller ladda ner en bilaga.
    public void canDownload(User user, Attachment attachment) {
        medicalRecordPolicy.canView(user, attachment.getMedicalRecord());
    }


    //Kontrollerar om användaren får radera en bilaga från systemet.
    public void canDelete(User user, Attachment attachment) {
        MedicalRecord record = attachment.getMedicalRecord();

        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Djurägare får inte radera bilagor");
            case VET -> {
                if (user.getClinic() == null ||
                        !user.getClinic().getId().equals(record.getClinic().getId()))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
                // VET får endast radera bilagor de själva har laddat upp.
                if (attachment.getUploadedBy() == null ||
                        !attachment.getUploadedBy().getId().equals(user.getId()))
                    throw new ForbiddenException("Du kan endast radera bilagor du själv har laddat upp.");
            }
            case ADMIN -> {
                // Admin har fulla rättigheter att radera.
            }
        }
    }

    // Hjälpmetoder för validering

    private void validateFileType(String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.trim().toLowerCase(Locale.ROOT))) {
            throw new BusinessRuleException("Otillåtet filformat. Endast JPG, PNG och PDF accepteras.");
        }
    }

    private void validateFileSize(long fileSize) {

        if (fileSize < 0) {
            throw new BusinessRuleException("Filstorlek kan inte vara negativ.");
        }

        if (fileSize > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException("Filen är för stor. Maxgränsen är 10 MB.");
        }
    }
}