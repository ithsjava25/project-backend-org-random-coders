package org.example.vet1177.services;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.ActivityType;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.repository.ActivityLogRepository;
import org.example.vet1177.repository.PetRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;
    private final PetRepository petRepository;

    public ActivityLogService(ActivityLogRepository repository, PetRepository petRepository){
        this.repository = repository;
        this.petRepository = petRepository;
    }

    public void log(ActivityType action, String description, String user, MedicalRecord record){
        ActivityLog log = new ActivityLog(action, description, user, record);
        repository.save(log);
    }
}
