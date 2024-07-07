package by.jenka.rss.backend.productservice.sdk.task2;

import software.amazon.awscdk.services.sns.ITopicSubscription;
import software.amazon.awscdk.services.sns.SubscriptionFilter;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;

import java.util.List;
import java.util.Map;

public class Utils {

    public static List<? extends ITopicSubscription> convertToEmailSubscriptions(List<String> emails) {
        return emails.stream()
                .map(email -> EmailSubscription.Builder.create(email)
                        .filterPolicy(Map.of())
                        .build())
                .toList();
    }

    public static List<? extends ITopicSubscription> convertToEmailSubscriptions(List<String> emails, Map<String, ? extends SubscriptionFilter> filter) {
        return emails.stream()
                .map(email -> EmailSubscription.Builder.create(email)
                        .filterPolicy(filter)
                        .build())
                .toList();
    }
}
