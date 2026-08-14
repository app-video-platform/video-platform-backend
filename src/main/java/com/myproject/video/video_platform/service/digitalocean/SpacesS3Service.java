package com.myproject.video.video_platform.service.digitalocean;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
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

    public String generatePresignedUrlForGet(String key, Duration expiration) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketMedia)
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getRequest)
                .signatureDuration(expiration)
                .build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url().toString();
    }
}
