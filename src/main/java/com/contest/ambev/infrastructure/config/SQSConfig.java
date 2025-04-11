package com.contest.ambev.infrastructure.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class SQSConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint}")
    private String endpoint;

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    
    @Bean
    @Primary
    public AmazonSQS amazonSQS() {
        return AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

    
    @Bean
    public boolean createQueues(AmazonSQS amazonSQS, 
                            @Value("${aws.sqs.queue.order-received}") String orderReceivedQueue,
                            @Value("${aws.sqs.queue.order-processed}") String orderProcessedQueue) {
        try {
            amazonSQS.createQueue(orderReceivedQueue);
            amazonSQS.createQueue(orderProcessedQueue);
            return true;
        } catch (Exception e) {
            
            return false;
        }
    }
} 
