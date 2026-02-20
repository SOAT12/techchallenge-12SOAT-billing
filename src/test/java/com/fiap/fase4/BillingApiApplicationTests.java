package com.fiap.fase4;

import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BillingApiApplicationTests {

    @MockitoBean
    private SqsAsyncClient sqsAsyncClient;

    @MockitoBean
    private SnsAsyncClient snsAsyncClient;

	@Test
	void contextLoads() {
	}

}
