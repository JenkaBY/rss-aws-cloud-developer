package by.jenka.rss.importservice.lambda.config;

import software.amazon.awssdk.services.sqs.SqsClient;

public class MessagingConfig {

    public static SqsClient getSqsClient() {
        return SqsClient.builder()
                .region(AwsConfig.DEFAULT_REGION)
                .build();
    }
}
