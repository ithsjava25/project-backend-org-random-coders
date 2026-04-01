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

import java.net.URI;

@Configuration
public class MinioConfig {

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
    public CommandLineRunner initializeBucket(S3Client s3Client) {
        return args -> {
            try {
                s3Client.headBucket(HeadBucketRequest.builder()
                        .bucket(bucketName)
                        .build());

                System.out.println("S3/MinIO: Ansluten till bucket '" + bucketName + "'.");
            } catch(NoSuchBucketException e) {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build());
                System.out.println("S3/MinIO: Bucket '" + bucketName + "' skapades automatiskt.");
            } catch (Exception e) {
                System.err.println("S3/MinIO Error: Kunde inte verifiera bucket. Är Docker-containern startad?");
            }
        };

    }

}


