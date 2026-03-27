package org.example.vet1177.services;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.ActivityType;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository){
        this.repository = repository;
    }

    public void log(ActivityType action, String description, String user, MedicalRecord record){
        ActivityLog log = new ActivityLog(action, description, user, record);
        repository.save(log);
    }
}
