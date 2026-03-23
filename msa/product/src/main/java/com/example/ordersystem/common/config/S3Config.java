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
import org.springframework.util.StringUtils;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key:${AWS_ACCESS_KEY_ID:}}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:${AWS_SECRET_ACCESS_KEY:}}")
    private String secretKey;

    @Value("${cloud.aws.region.static:${AWS_REGION:ap-northeast-2}}")
    private String region;

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withRegion(region);

        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
        } else {
            AWSCredentialsProvider provider = DefaultAWSCredentialsProviderChain.getInstance();
            builder.withCredentials(provider);
        }

        return builder.build();
    }
}
