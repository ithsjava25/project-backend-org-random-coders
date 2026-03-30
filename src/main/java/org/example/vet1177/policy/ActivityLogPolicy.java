package org.example.vet1177.policy;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogPolicy {

    private static final String FORBIDDEN_MSG = "Åtkomst nekad";

    //VIEW- vem får läsa loggar
    public void canView(User user, ActivityLog log){

        // Null-skydd (viktigt!)
        if (user == null || user.getRole() == null || log == null) {
            throw new ForbiddenException(FORBIDDEN_MSG);
        }

        MedicalRecord record = log.getMedicalRecord();

        if (record == null) {
            throw new ForbiddenException(FORBIDDEN_MSG);
        }

        switch (user.getRole()){

            case OWNER -> {
                if (record.getOwner() == null || record.getOwner().getId() == null) {
                    throw new ForbiddenException(FORBIDDEN_MSG);
                }

                if(!record.getOwner().getId().equals(user.getId())){
                    throw new ForbiddenException(FORBIDDEN_MSG);
                }
            }

            case VET -> {
                if(user.getClinic() == null || record.getClinic() == null){
                    throw new ForbiddenException(FORBIDDEN_MSG);
                }

                if(!user.getClinic().getId().equals(record.getClinic().getId())){
                    throw new ForbiddenException(FORBIDDEN_MSG);
                }
            }

            //full access
            case ADMIN -> {}

            default -> throw new ForbiddenException(FORBIDDEN_MSG);
        }
    }

    //CREATE- loggar ska skapas av systemet(inte användare)
    public void canCreate(User user){

        // Null-skydd
        if (user == null || user.getRole() == null) {
            throw new ForbiddenException("Endast admin/system får skapa loggar");
        }

        switch (user.getRole()){
            case ADMIN -> {}
            default -> throw new ForbiddenException("Endast admin/system får skapa loggar");
        }
    }

    //UPDATE- aldrig tillåtet
    public void canUpdate(){
        throw new ForbiddenException("Activity logs kan inte uppdateras");
    }

    //DELETE- aldrig tillåtet
    public void canDelete(){
        throw new ForbiddenException("Activity logs kan inte tas bort");
    }
}
