package org.example.vet1177.policy;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogPolicy {

    //VIEW- vem får läsa loggar
    public void canView(User user, ActivityLog log){

        MedicalRecord record = log.getMedicalRecord();

        switch (user.getRole()){

            case OWNER -> {
                if(!record.getOwner().getId().equals(user.getId())){
                    throw new ForbiddenException("Åtkomst nekad");
                }
            }

            case VET -> {
                if(user.getClinic() == null || record.getClinic() == null){
                    throw new ForbiddenException("Åtkomst nekad");
                }

                if(!user.getClinic().getId().equals(record.getClinic().getId())){
                    throw new ForbiddenException("Åtkomst nekad");
                }
            }
            //full access
            case ADMIN -> {}

            default -> throw new ForbiddenException("Åtkomst nekad");
        }
    }

    //CREATE- loggar ska skapas av systemet(inte användare)
    public void canCreate(User user){

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
