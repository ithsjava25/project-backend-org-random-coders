package org.example.vet1177.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attachment")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User uploadedBy;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    // Den unika nyckeln/sökvägen i S3-bucket
    @Column(name = "s3_key", nullable = false, unique = true, length = 1000)
    private String s3Key;

    @Column(name = "s3_bucket", nullable = false, length = 255)
    private String s3Bucket;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    public Attachment() {
    }

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = Instant.now();
    }

    // Getters och Setters
    public UUID getId() { return id; }

    public MedicalRecord getMedicalRecord() { return medicalRecord; }

    public void setMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord; }

    public User getUploadedBy() {
        return uploadedBy; }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy; }

    public String getFileName() {
        return fileName; }

    public void setFileName(String fileName) {
        this.fileName = fileName; }

    public String getS3Key() {
        return s3Key; }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key; }

    public String getS3Bucket() {
        return s3Bucket; }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSizeBytes() {
        return fileSizeBytes; }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes; }

    public Instant getUploadedAt() {
        return uploadedAt; }
}