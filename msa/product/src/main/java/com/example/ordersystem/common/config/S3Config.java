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

        if (isBlankOrPlaceholder(accessKey) || isBlankOrPlaceholder(secretKey)) {
            credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        } else {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        }

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .build();
    }

    private boolean isBlankOrPlaceholder(String value) {
        return value == null
                || value.isBlank()
                || "YOUR_ACCESS_KEY".equalsIgnoreCase(value)
                || "YOUR_SECRET_KEY".equalsIgnoreCase(value)
                || "${AWS_ACCESS_KEY_ID}".equals(value)
                || "${AWS_SECRET_ACCESS_KEY}".equals(value);
    }
}
