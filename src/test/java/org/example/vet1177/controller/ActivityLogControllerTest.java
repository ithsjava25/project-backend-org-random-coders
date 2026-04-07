package org.example.vet1177.controller;

import org.example.vet1177.entities.*;
import org.example.vet1177.services.ActivityLogService;
import org.example.vet1177.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// ✅ RÄTT (Boot 4)
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityLogController.class)
@AutoConfigureMockMvc(addFilters = false) // 🔥 stänger av security (fixar 401)
class ActivityLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityLogService activityLogService;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnLogsForRecord() throws Exception {

        // Arrange
        UUID recordId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        // Mock User
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn("Alice");

        // Mock MedicalRecord
        MedicalRecord record = mock(MedicalRecord.class);
        when(record.getId()).thenReturn(recordId);

        // Mock ActivityLog
        ActivityLog log = mock(ActivityLog.class);
        when(log.getId()).thenReturn(UUID.randomUUID());
        when(log.getAction()).thenReturn(ActivityType.CASE_CREATED);
        when(log.getDescription()).thenReturn("Created case");
        when(log.getPerformedBy()).thenReturn(user);
        when(log.getMedicalRecord()).thenReturn(record);
        when(log.getCreatedAt()).thenReturn(createdAt);

        // Mock service responses
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(activityLogService.getByRecord(recordId, user))
                .thenReturn(List.of(log));

        // Act + Assert
        mockMvc.perform(get("/api/activity-logs/record/" + recordId)
                        .header("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Created case"))
                .andExpect(jsonPath("$[0].action").value("CASE_CREATED"))
                .andExpect(jsonPath("$[0].performedByName").value("Alice"));
    }
}