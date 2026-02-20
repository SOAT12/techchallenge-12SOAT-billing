package com.fiap.fase4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import java.net.URI;

@Configuration
@EnableScheduling
public class AwsConfig {

    @Value("${spring.cloud.aws.region.static:us-east-1}")
    private String region;

    @Value("${spring.cloud.aws.credentials.access-key:dummy}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key:dummy}")
    private String secretKey;

    @Value("${app.sqs.endpoint-url:}")
    private String endpointUrl;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        var builder = SqsAsyncClient.builder().region(Region.of(region));
        
        if (!"dummy".equals(accessKey) && !"dummy".equals(secretKey)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
        }

        return builder.build();
    }

    @Bean
    public SnsAsyncClient snsAsyncClient() {
        var builder = SnsAsyncClient.builder().region(Region.of(region));
        
        if (!"dummy".equals(accessKey) && !"dummy".equals(secretKey)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl));
        }

        return builder.build();
    }
}
