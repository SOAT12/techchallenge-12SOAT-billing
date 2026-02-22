package com.fiap.fase4.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;

class AwsConfigTest {

    private AwsConfig awsConfig;

    @BeforeEach
    void setUp() {
        awsConfig = new AwsConfig();
        ReflectionTestUtils.setField(awsConfig, "region", "us-east-1");
    }

    @Test
    void sqsAsyncClient_withDummyCredentialsAndNoEndpoint() {
        ReflectionTestUtils.setField(awsConfig, "accessKey", "dummy");
        ReflectionTestUtils.setField(awsConfig, "secretKey", "dummy");
        ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

        SqsAsyncClient client = awsConfig.sqsAsyncClient();
        assertThat(client).isNotNull();
    }

    @Test
    void snsAsyncClient_withDummyCredentialsAndNoEndpoint() {
        ReflectionTestUtils.setField(awsConfig, "accessKey", "dummy");
        ReflectionTestUtils.setField(awsConfig, "secretKey", "dummy");
        ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

        SnsAsyncClient client = awsConfig.snsAsyncClient();
        assertThat(client).isNotNull();
    }

    @Test
    void sqsAsyncClient_withRealCredentialsAndEndpoint() {
        ReflectionTestUtils.setField(awsConfig, "accessKey", "realKey");
        ReflectionTestUtils.setField(awsConfig, "secretKey", "realSecret");
        ReflectionTestUtils.setField(awsConfig, "endpointUrl", "http://localhost:4566");

        SqsAsyncClient client = awsConfig.sqsAsyncClient();
        assertThat(client).isNotNull();
    }

    @Test
    void snsAsyncClient_withRealCredentialsAndEndpoint() {
        ReflectionTestUtils.setField(awsConfig, "accessKey", "realKey");
        ReflectionTestUtils.setField(awsConfig, "secretKey", "realSecret");
        ReflectionTestUtils.setField(awsConfig, "endpointUrl", "http://localhost:4566");

        SnsAsyncClient client = awsConfig.snsAsyncClient();
        assertThat(client).isNotNull();
    }
}
