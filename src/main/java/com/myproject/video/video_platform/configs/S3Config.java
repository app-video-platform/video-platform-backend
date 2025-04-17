package com.myproject.video.video_platform.configs;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    public S3Presigner s3Presigner(@Value("${digitalocean.spaces.accessKey}") String accessKey,
                                   @Value("${digitalocean.spaces.secretKey}") String secretKey,
                                   @Value("${digitalocean.spaces.region}") String region,
                                   @Value("${digitalocean.spaces.originEndpointUrl}") String endpoint) {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(endpoint))
                .build();
    }
}
