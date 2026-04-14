package org.example.vet1177.controller;

import org.example.vet1177.dto.response.activitylog.ActivityLogResponse;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.ActivityLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogController.class);

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    // Hämta alla logs för ett MedicalRecord
    @GetMapping("/record/{recordId}")
    public List<ActivityLogResponse> getLogsByRecord(
            @PathVariable UUID recordId,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("GET /api/activity-logs/record/{}", recordId);

        return activityLogService.getByRecord(recordId, currentUser)
                .stream()
                .map(ActivityLogResponse::from)
                .toList();
    }
}
