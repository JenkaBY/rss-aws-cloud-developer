package by.jenka.rss.productservice.lambda.config;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

public class MessagingConfig {

    public static SnsClient getDefaultSnsClient() {
        return SnsClient.builder()
                .region(Region.EU_NORTH_1)
                .build();
    }
}
