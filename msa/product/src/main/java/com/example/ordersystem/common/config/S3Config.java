package com.example.ordersystem.common.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Bean
    public AmazonS3 amazonS3() {
        AWSCredentialsProvider credentialsProvider;

        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        } else {
            credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        }

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .build();
    }
}
