package co.com.organization.dynamodb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;

import java.net.URI;

@Configuration
public class DynamoDBConfig {

    @Bean
    @Profile({"local"})
    public DynamoDbAsyncClient amazonDynamoDB(@Value("${aws.dynamodb.endpoint}") String endpoint,
                                              @Value("${aws.region}") String region,
                                              MetricPublisher publisher) {
        return DynamoDbAsyncClient.builder()
                .credentialsProvider(ProfileCredentialsProvider.create("default"))
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .overrideConfiguration(o -> o.addMetricPublisher(publisher))
                .build();
    }

    @Bean
    @Profile({"dev", "cer", "pdn"})
    public DynamoDbAsyncClient amazonDynamoDBAsync(
            MetricPublisher publisher,
            @Value("${aws.region}") String region,
            @Value("${aws.dynamodb.access-key}") String accessKey,
            @Value("${aws.dynamodb.secret-key}") String secretKey,
            @Value("${aws.dynamodb.role}") String role,
            @Value("${aws.session.name}") String sessionName) {
        return DynamoDbAsyncClient.builder()
                .credentialsProvider(createCredential(accessKey, secretKey, role, sessionName, Region.of(region)))
                .region(Region.of(region))
                .overrideConfiguration(o -> o.addMetricPublisher(publisher))
                .build();
    }

    @Bean
    public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient(DynamoDbAsyncClient client) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(client)
                .build();
    }

    private StsAssumeRoleCredentialsProvider createCredential(String accessKeyId, String secretAccessKey, String roleArn, String roleSessionName, Region region) {
        StaticCredentialsProvider userCredentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(((StsClient.builder().credentialsProvider(userCredentials))
                        .region(region))
                        .build())
                .refreshRequest(request -> request
                        .roleArn(roleArn)
                        .roleSessionName(roleSessionName)
                        .durationSeconds(3600))
                .build();
    }
}
