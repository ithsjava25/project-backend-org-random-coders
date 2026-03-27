package org.example.vet1177.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private String description;

    private String performedBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    protected ActivityLog() {}

    public ActivityLog(String action, String description, String performedBy, MedicalRecord medicalRecord) {
        this.action = action;
        this.description = description;
        this.performedBy = performedBy;
        this.medicalRecord = medicalRecord;
        this.createdAt = Instant.now();
    }
}
