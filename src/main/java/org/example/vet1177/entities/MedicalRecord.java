package org.example.vet1177.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "medical_record")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordStatus status = RecordStatus.OPEN;

    // Relationer — Many-to-One
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_vet_id")
    private User assignedVet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Attachment> attachments = new ArrayList<>();

    // Timestamps
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    // Lifecycle hooks — sätter timestamps automatiskt
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Tom konstruktor krävs av JPA
    public MedicalRecord() {}

    // Getters och setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RecordStatus getStatus() { return status; }
    public void setStatus(RecordStatus status) { this.status = status;
    if (status==RecordStatus.CLOSED && this.closedAt==null){
    this.closedAt=Instant.now();
    } else if (status !=RecordStatus.CLOSED){
        this.closedAt=null;
    }
    }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Clinic getClinic() { return clinic; }
    public void setClinic(Clinic clinic) { this.clinic = clinic; }

    public User getAssignedVet() { return assignedVet; }
    public void setAssignedVet(User assignedVet) { this.assignedVet = assignedVet; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }


    public List<Attachment> getAttachments() {
        return java.util.Collections.unmodifiableList(attachments);
    }

    public void setAttachments(List<Attachment> newAttachments) {
        this.attachments.clear();
        if (newAttachments != null) {
            for (Attachment attachment : newAttachments) {
                this.addAttachment(attachment);
            }
        }
    }


    public void addAttachment(Attachment attachment) {
        if (attachment != null) {
            this.attachments.add(attachment);
            // Sätt baksidan av relationen om den inte redan är satt
            if (attachment.getMedicalRecord() != this) {
                attachment.setMedicalRecord(this);
            }
        }
    }

    public void removeAttachment(Attachment attachment) {
        if (attachment != null) {
            this.attachments.remove(attachment);
            attachment.setMedicalRecord(null);
        }
    }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
}