package org.example.vet1177.services;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.ActivityType;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.policy.ActivityLogPolicy;
import org.example.vet1177.repository.ActivityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityLogService {

    private static final Logger LOG = LoggerFactory.getLogger(ActivityLogService.class);

    private final ActivityLogRepository repository;
    private final ActivityLogPolicy activityLogPolicy;

    public ActivityLogService(ActivityLogRepository repository,
                              ActivityLogPolicy activityLogPolicy) {
        this.repository = repository;
        this.activityLogPolicy = activityLogPolicy;
    }

    // CREATE

    public void log(ActivityType action, String description, User user, MedicalRecord record) {

        if (action == null || description == null || user == null || record == null) {
            LOG.warn("Invalid activity log data");
            throw new BusinessRuleException("Invalid activity log data");
        }


        ActivityLog log = new ActivityLog(action, description, user, record);
        repository.save(log);
        LOG.info("Activity logged action={} recordId={}", action, record.getId());
    }

    // READ

    public List<ActivityLog> getByRecord(UUID recordId, User currentUser) {

        LOG.debug("Fetching activity logs recordId={}", recordId);
        if (recordId == null || currentUser == null) {
            LOG.warn("Access denied to activity logs - missing recordId or user");
            throw new ForbiddenException("Åtkomst nekad");
        }

        List<ActivityLog> logs =
                repository.findByMedicalRecordIdOrderByCreatedAtDesc(recordId);

        // Filtrera istället för att kasta exception
        return logs.stream()
                .filter(log -> canView(currentUser, log))
                .toList();
    }

    // HELPER

    private boolean canView(User user, ActivityLog log) {
        try {
            activityLogPolicy.canView(user, log);
            return true;
        } catch (ForbiddenException e) {
            return false;
        }
    }
}