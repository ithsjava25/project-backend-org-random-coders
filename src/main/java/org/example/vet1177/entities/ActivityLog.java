package org.example.vet1177.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_log")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType action;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    protected ActivityLog() {}

    public ActivityLog(ActivityType action, String description, String performedBy, MedicalRecord medicalRecord) {
        this.action = action;
        this.description = description;
        this.performedBy = performedBy;
        this.medicalRecord = medicalRecord;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }

    public ActivityType getAction() { return action; }

    public String getDescription() { return description; }

    public String getPerformedBy() { return performedBy; }

    public Instant getCreatedAt() { return createdAt; }

    public MedicalRecord getMedicalRecord() { return medicalRecord; }
}
