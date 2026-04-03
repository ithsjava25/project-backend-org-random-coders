package org.example.vet1177.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(AwsS3Properties.class)
@ConditionalOnExpression("#{!'${aws.s3.endpoint:}'.isBlank()}")
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    private final AwsS3Properties props;

    public MinioConfig(AwsS3Properties props) {
        validateProps(props);
        this.props = props;
    }

    private static void validateProps(AwsS3Properties props) {
        java.util.Map<String, String> fields = java.util.Map.of(
                "aws.s3.endpoint (S3_ENDPOINT)",       nullToEmpty(props.getEndpoint()),
                "aws.s3.access-key (S3_ACCESS_KEY)",   nullToEmpty(props.getAccessKey()),
                "aws.s3.secret-key (S3_SECRET_KEY)",   nullToEmpty(props.getSecretKey()),
                "aws.s3.region (S3_REGION)",            nullToEmpty(props.getRegion()),
                "aws.s3.bucket-name (S3_BUCKET)",       nullToEmpty(props.getBucketName())
        );
        long presentCount = fields.values().stream().filter(v -> !v.isBlank()).count();
        if (presentCount > 0 && presentCount < fields.size()) {
            String missing = fields.entrySet().stream()
                    .filter(e -> e.getValue().isBlank())
                    .map(java.util.Map.Entry::getKey)
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Incomplete S3/MinIO configuration: the following properties are missing: " + missing +
                    ". Either set all five S3 properties or none of them.");
        }
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())))
                .region(Region.of(props.getRegion()))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())))
                .region(Region.of(props.getRegion()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean
    public CommandLineRunner initializeBucket(S3Client s3Client) {
        return args -> {
            try {
                log.debug("S3/MinIO: Verifying bucket: {}", props.getBucketName());

                s3Client.headBucket(HeadBucketRequest.builder()
                        .bucket(props.getBucketName())
                        .build());

                log.info("S3/MinIO: Successfully connected to bucket '{}'.", props.getBucketName());

            } catch (NoSuchBucketException e) {
                log.warn("S3/MinIO: Bucket '{}' not found. Attempting to create it...", props.getBucketName());

                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(props.getBucketName())
                        .build());

                log.info("S3/MinIO: Bucket '{}' created automatically.", props.getBucketName());

            } catch (S3Exception e) {
                log.error("S3/MinIO FATAL ERROR: {} (Status Code: {})",
                        e.awsErrorDetails().errorMessage(),
                        e.statusCode());

                throw new RuntimeException("Could not initialize storage on startup. Check credentials and MinIO status.", e);
            }
        };
    }
}
