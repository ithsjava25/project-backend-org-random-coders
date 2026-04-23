package org.example.vet1177.policy;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MedicalRecordPolicy {

    public void canView(User user, MedicalRecord record) {
        switch (user.getRole()) {
            case OWNER -> {
                if (!user.getId().equals(record.getOwner().getId()))
                    throw new ForbiddenException("Du har inte tillgång till detta ärende");
            }
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canCreate(User user, Pet pet, Clinic clinic) {
        switch (user.getRole()) {
            case OWNER -> {
                if (!pet.getOwner().getId().equals(user.getId()))
                    throw new ForbiddenException("Du kan bara skapa ärenden för egna djur");
            }
            case VET -> {
                if (!user.getClinic().getId().equals(clinic.getId()))
                    throw new ForbiddenException("Du kan inte skapa ärende för en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    // Auth-kontroll (roll/ägarskap) körs före isFinal-kontrollen — annars skulle en
    // OWNER som försöker uppdatera någon annans stängda ärende få "Stängda ärenden
    // kan inte uppdateras" i stället för 403 (CodeRabbit-kommentar på #216).
    // OWNER får uppdatera eget ärendes titel/beskrivning (DTO:n exponerar bara de fälten).
    // Status/close/assign-vet gatas i separata policy-metoder och URL-regler.
    public void canUpdate(User user, MedicalRecord record) {
        switch (user.getRole()) {
            case OWNER -> {
                if (!user.getId().equals(record.getOwner().getId()))
                    throw new ForbiddenException("Du kan bara uppdatera egna ärenden");
            }
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }

        if (record.getStatus().isFinal())
            throw new BusinessRuleException("Stängda ärenden kan inte uppdateras");
    }

    public void canUpdateStatus(User user, MedicalRecord record, RecordStatus newStatus) {
        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Ägare får inte ändra status på ärenden");
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }

        if (record.getStatus().isFinal())
            throw new BusinessRuleException("Stängda ärenden kan inte uppdateras");
    }


    public boolean isAllowed(User user, MedicalRecord record) {
        return switch (user.getRole()) {
            case OWNER -> user.getId().equals(record.getOwner().getId());
            case VET   -> user.getClinic() != null &&
                    user.getClinic().getId().equals(record.getClinic().getId());
            case ADMIN -> true;
        };
    }

    public void canAssignVet(User user, MedicalRecord record, User vetToAssign) {
        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Ägare får inte tilldela handläggare");
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
                if (!sameClinicAsRecord(vetToAssign, record))
                    throw new ForbiddenException("Kan inte tilldela vet från en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canClose(User user, MedicalRecord record) {
        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Ägare får inte stänga ärenden");
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }

        if (record.getStatus().isFinal())
            throw new BusinessRuleException("Ärendet är redan stängt");
    }

    public void canViewClinic(User user, UUID clinicId) {
        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Ägare har inte tillgång till klinikvy");
            case VET -> {
                if (!user.getClinic().getId().equals(clinicId))
                    throw new ForbiddenException("Du har inte tillgång till en annan kliniqs ärenden");
            }
            case ADMIN -> {}
        }
    }

    // ── Hjälpmetoder ─────────────────────────────────────────

    private boolean sameClinic(User user, MedicalRecord record) {
        return user.getClinic() != null &&
                user.getClinic().getId().equals(record.getClinic().getId());
    }

    private boolean sameClinicAsRecord(User vet, MedicalRecord record) {
        return vet.getClinic() != null &&
                vet.getClinic().getId().equals(record.getClinic().getId());
    }
}