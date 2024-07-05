package by.jenka.rss.importservice.lambda.service;

import by.jenka.rss.importservice.lambda.model.ProductAvailable;
import by.jenka.rss.importservice.lambda.service.utils.BatchUtils;
import by.jenka.rss.importservice.lambda.service.utils.JsonSerializer;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;

import java.util.List;
import java.util.UUID;

public class ProductMessageSender {

    private static final String CATALOG_ITEM_QUEUE = System.getenv().getOrDefault("CATALOG_ITEM_QUEUE_TOPIC_NAME", "replace-me");
    private AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();

    public void sendByBatches(List<ProductAvailable> products, int batchSize, LambdaLogger logger) {
        logger.log("Sending product messages by batch");
        BatchUtils.getBatches(products, batchSize).forEach(
                batch -> send(batch, logger)
        );
    }

    public void send(List<ProductAvailable> products, LambdaLogger logger) {
        logger.log("Sending product messages");
        for (var product : products) {
            logger.log("Sending message: " + product);
        }
        var batchEntries = products.stream()
                .peek(p -> logger.log("Sending message: " + p + "in the %s".formatted(CATALOG_ITEM_QUEUE)))
                .map(p -> new SendMessageBatchRequestEntry(UUID.randomUUID().toString(), JsonSerializer.serialize(p)))
                .toList();
        var sendMessageBatchRequest = new SendMessageBatchRequest()
                .withQueueUrl(CATALOG_ITEM_QUEUE)
                .withEntries(batchEntries);
        var result = sqsClient.sendMessageBatch(sendMessageBatchRequest);
        logger.log("Messages were sent with result %s".formatted(result));
    }
}
