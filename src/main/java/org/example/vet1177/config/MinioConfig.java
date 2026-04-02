package org.example.vet1177.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class MinioConfig {


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MinioConfig.class);


    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder().endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean
    public CommandLineRunner initializeBucket(S3Client s3Client) {
        return args -> {
            try {

                log.debug("S3/MinIO: Verifying bucket: {}", bucketName);

                s3Client.headBucket(HeadBucketRequest.builder()
                        .bucket(bucketName)
                        .build());

                log.info("S3/MinIO: Successfully connected to bucket '{}'.", bucketName);

            } catch (NoSuchBucketException e) {

                log.warn("S3/MinIO: Bucket '{}' not found. Attempting to create it...", bucketName);

                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build());

                log.info("S3/MinIO: Bucket '{}' created automatically.", bucketName);

            } catch (S3Exception e) {
                // ERROR används för kritiska fel som stoppar applikationen
                log.error("S3/MinIO FATAL ERROR: {} (Status Code: {})",
                        e.awsErrorDetails().errorMessage(),
                        e.statusCode());

                throw new RuntimeException("Could not initialize storage on startup. Check credentials and MinIO status.", e);
            }
        };
    }
}