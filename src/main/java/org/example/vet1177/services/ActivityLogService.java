package org.example.vet1177.services;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.ActivityType;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository){
        this.repository = repository;
    }

    public void log(ActivityType action, String description, User user, MedicalRecord record){

        // Null-skydd
        if (action == null || description == null || record == null) {
            throw new BusinessRuleException("Invalid activity log data");
        }

        ActivityLog log = new ActivityLog(action, description, user, record);

        repository.save(log);
    }
}
