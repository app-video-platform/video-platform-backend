package com.myproject.video.video_platform.service.digitalocean;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

@Service
@Slf4j
public class SpacesS3Service {
    @Value("${digitalocean.spaces.accessKey}")
    private String accessKey;

    @Value("${digitalocean.spaces.secretKey}")
    private String secretKey;

    @Value("${digitalocean.spaces.originEndpointUrl}")
    private String endpoint;

    @Value("${digitalocean.spaces.region}")
    private String region;

    @Value("${digitalocean.spaces.bucket-media}")
    private String bucketMedia;

    private S3Presigner s3Presigner;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);


        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    /**
     * Generates a pre-signed PUT URL (origin endpoint).
     */
    public String generatePresignedUrlForPut(String key, Duration expiration) {
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketMedia)
                .key(key)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .putObjectRequest(putReq)
                .signatureDuration(expiration)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignReq);
        return presigned.url().toString();
    }
}
