package by.jenka.rss.importservice.lambda.service;

import by.jenka.rss.importservice.lambda.config.MessagingConfig;
import by.jenka.rss.importservice.lambda.model.ProductAvailable;
import by.jenka.rss.importservice.lambda.service.utils.BatchUtils;
import by.jenka.rss.importservice.lambda.service.utils.JsonSerializer;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;

import java.util.List;
import java.util.UUID;

public class ProductMessageSender {

    private static final String CATALOG_ITEM_QUEUE_TOPIC_URL = System.getenv().getOrDefault("CATALOG_ITEM_QUEUE_TOPIC_URL", "catalog-items-queue-replace-me");

    public void sendByBatches(List<ProductAvailable> products, int batchSize, LambdaLogger logger) {
        logger.log("Sending product messages by batch");
        try (SqsClient sqsClient = MessagingConfig.getSqsClient()) {
            logger.log("Sending product messages to queue %s".formatted(CATALOG_ITEM_QUEUE_TOPIC_URL));
            var groupedBatchEntries = BatchUtils.getBatches(products, batchSize).stream()
                    .map(this::convertToBatchRequestEntries)
                    .toList();
            groupedBatchEntries.forEach(batchEntries -> {
                var sentResult = sqsClient.sendMessageBatch(
                        SendMessageBatchRequest.builder()
                                .entries(batchEntries)
                                .queueUrl(CATALOG_ITEM_QUEUE_TOPIC_URL)
                                .build()
                );
                logger.log("Batch size[%s] was sent with result %s".formatted(batchEntries.size(), sentResult));
            });
            logger.log("Sent product messages");
        }
    }

    public List<SendMessageBatchRequestEntry> convertToBatchRequestEntries(List<ProductAvailable> products) {
        return products.stream()
                .map(p -> SendMessageBatchRequestEntry.builder()
                        .id(UUID.randomUUID().toString())
                        .messageBody(JsonSerializer.serialize(p))
                        .build())
                .toList();
    }
}
