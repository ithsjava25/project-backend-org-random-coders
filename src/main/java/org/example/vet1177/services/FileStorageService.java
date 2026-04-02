package org.example.vet1177.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public FileStorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Laddar upp en fil till S3/MinIO med fail-fast validering.
     * @param key Den unika sökvägen/namnet i bucketen
     * @param inputStream Dataströmmen från filen
     * @param size Storleken på filen i bytes
     * @param contentType MIME-typ (t.ex. image/jpeg)
     */
    public void upload(String key, InputStream inputStream, long size, String contentType) {
        // --- Fail-fast validering ---
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key får inte vara null eller tom vid uppladdning till S3");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream får inte vara null för key: " + key);
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Filstorleken måste vara större än 0 för key: " + key);
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("ContentType måste anges för korrekt filhantering av key: " + key);
        }
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("S3 bucketName är inte konfigurerad i applikationen");
        }

        try {
            log.debug("S3/MinIO: Uploading file to bucket '{}' with key: {}", bucketName, key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            // Anropar s3Client endast om alla valideringar passerat
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));

            log.info("S3/MinIO: Successfully uploaded file: {} to bucket: {}", key, bucketName);
        } catch (Exception e) {
            log.error("S3/MinIO: Failed to upload file {} to bucket {}: {}", key, bucketName, e.getMessage());
            throw new RuntimeException("Kunde inte ladda upp filen till lagringstjänsten", e);
        }
    }

    /**
     * Skapar en tidsbegränsad (presigned) URL för att hämta/visa en fil.
     * @param key Filens unika nyckel
     * @return En URL som är giltig i 15 minuter
     */
    public String generatePresignedUrl(String key) {
        // --- Fail-fast validering ---
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key får inte vara null eller tom vid generering av URL");
        }
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("S3 bucketName är inte konfigurerad i applikationen");
        }

        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(builder -> builder.bucket(bucketName).key(key))
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error("S3/MinIO: Failed to generate presigned URL for key {} in bucket {}: {}", key, bucketName, e.getMessage());
            throw new RuntimeException("Kunde inte generera åtkomstlänk för filen", e);
        }
    }

    /**
     * Tar bort en fil från S3/MinIO.
     * @param key Filens unika nyckel
     */
    public void delete(String key) {
        // --- Fail-fast validering ---
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key får inte vara null eller tom vid radering från S3");
        }
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("S3 bucketName är inte konfigurerad i applikationen");
        }

        try {
            log.debug("S3/MinIO: Deleting file with key: {} from bucket: {}", key, bucketName);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("S3/MinIO: Successfully deleted file: {} from bucket: {}", key, bucketName);
        } catch (Exception e) {
            log.error("S3/MinIO: Failed to delete file {} from bucket {}: {}", key, bucketName, e.getMessage());
            throw new RuntimeException("Kunde inte radera filen från lagringstjänsten", e);
        }
    }
}
