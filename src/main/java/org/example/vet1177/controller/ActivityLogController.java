package org.example.vet1177.controller;

import org.example.vet1177.dto.response.activitylog.ActivityLogResponse;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.ActivityLogService;
import org.example.vet1177.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final UserService userService;

    public ActivityLogController(ActivityLogService activityLogService,
                                 UserService userService) {
        this.activityLogService = activityLogService;
        this.userService = userService;
    }

    // Hämta alla logs för ett MedicalRecord
    @GetMapping("/record/{recordId}")
    public List<ActivityLogResponse> getLogsByRecord(
            @PathVariable UUID recordId,
            @RequestHeader("userId") UUID userId
    ) {
        User user = userService.getById(userId);

        return activityLogService.getByRecord(recordId, user)
                .stream()
                .map(ActivityLogResponse::from)
                .toList();
    }
}