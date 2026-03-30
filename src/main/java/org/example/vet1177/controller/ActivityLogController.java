package org.example.vet1177.controller;

import org.example.vet1177.dto.response.activitylog.ActivityLogResponse;
import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.ActivityLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    // Hämta alla logs för ett MedicalRecord
    @GetMapping("/record/{recordId}")
    public List<ActivityLogResponse> getLogsByRecord(
            @PathVariable UUID recordId,
            @RequestAttribute("currentUser") User currentUser // eller från security senare
    ) {
        List<ActivityLog> logs = activityLogService.getByRecord(recordId, currentUser);

        return logs.stream()
                .map(ActivityLogResponse::from)
                .toList();
    }
}