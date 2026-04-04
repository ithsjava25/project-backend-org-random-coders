package org.example.vet1177.policy;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogPolicy {

    private static final String FORBIDDEN_MSG = "Åtkomst nekad";
    private static final String ADMIN_ONLY_MSG = "Endast admin/system får skapa loggar";

    // VIEW
    public void canView(User user, ActivityLog log){

        if (user == null || user.getRole() == null || log == null) {
            throw new ForbiddenException(FORBIDDEN_MSG);
        }

        MedicalRecord record = log.getMedicalRecord();

        if (record == null) {
            throw new ForbiddenException(FORBIDDEN_MSG);
        }

        switch (user.getRole()){

            case OWNER -> {
                if (record.getOwner() == null || record.getOwner().getId() == null ||
                        !record.getOwner().getId().equals(user.getId())) {
                    throw new ForbiddenException(FORBIDDEN_MSG);
                }
            }

            case VET -> {
                if (user.getClinic() == null || record.getClinic() == null ||
                        !user.getClinic().getId().equals(record.getClinic().getId())) {
                    throw new ForbiddenException(FORBIDDEN_MSG);
                }
            }

            case ADMIN -> {}

            default -> throw new ForbiddenException(FORBIDDEN_MSG);
        }
    }


    public void canUpdate(){
        throw new ForbiddenException("Activity logs kan inte uppdateras");
    }

    public void canDelete(){
        throw new ForbiddenException("Activity logs kan inte tas bort");
    }
}