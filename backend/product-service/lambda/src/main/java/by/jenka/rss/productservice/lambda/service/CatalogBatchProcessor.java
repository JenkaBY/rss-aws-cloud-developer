package by.jenka.rss.productservice.lambda.service;

import by.jenka.rss.productservice.lambda.config.DBConfig;
import by.jenka.rss.productservice.lambda.config.MessagingConfig;
import by.jenka.rss.productservice.lambda.model.Product;
import by.jenka.rss.productservice.lambda.repository.DefaultProductRepository;
import by.jenka.rss.productservice.lambda.repository.ProductRepository;
import by.jenka.rss.productservice.lambda.service.validator.ProductValidator;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.jr.ob.JSON;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishBatchRequest;
import software.amazon.awssdk.services.sns.model.PublishBatchRequestEntry;
import software.amazon.awssdk.services.sns.model.PublishBatchResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CatalogBatchProcessor {

    private static final String CREATE_PRODUCT_TOPIC_ARN = System.getenv().getOrDefault("CREATE_PRODUCT_TOPIC_ARN", "create-product-topic-arn-replace-me");
    private ProductRepository productRepository = new DefaultProductRepository(DBConfig.enhancedClient);
    private ProductValidator productValidator = new ProductValidator();

    public void processMessages(List<SQSEvent.SQSMessage> messages, LambdaLogger logger) {
        var persistedProducts = messages.stream()
                .map(msg -> processMessage(msg, logger))
                .toList();
        var sendResult = publishProductCreatedEvents(persistedProducts);
        logger.log("Messages were sent with result %s".formatted(sendResult));
    }

    private Product processMessage(SQSEvent.SQSMessage msg, LambdaLogger logger) {
        logger.log("----------NEW-MESSAGE--------");
        logger.log("Message: " + msg);
        logger.log("Message body: " + msg.getBody());
        logger.log("Message attrs: " + msg.getMessageAttributes());
        logger.log("Message event source: " + msg.getEventSource());
        return persistProduct(msg, logger);
    }

    private Product persistProduct(SQSEvent.SQSMessage msg, LambdaLogger logger) {
        try {
            var product = JSON.std.beanFrom(Product.class, msg.getBody());
            productValidator.validate(product);
            return productRepository.upsert(product);
        } catch (RuntimeException any) {
            logger.log("Couldn't process the message: %s".formatted(msg.getBody()));
            throw any;
        } catch (IOException e) {
            logger.log("Couldn't convert the message: %s".formatted(msg.getBody()));
            throw new RuntimeException(e);
        }
    }

    private static PublishBatchResponse publishProductCreatedEvents(List<Product> persistedProducts) {
        try (SnsClient snsClient = MessagingConfig.getDefaultSnsClient()) {
            return snsClient.publishBatch(
                    PublishBatchRequest.builder()
                            .topicArn(CREATE_PRODUCT_TOPIC_ARN)
                            .publishBatchRequestEntries(
                                    persistedProducts.stream().map(product -> PublishBatchRequestEntry.builder()
                                                    .id(product.getId().toString())
                                                    .subject("The Product id[%s], title[%s] was persisted".formatted(product.getId(), product.getTitle()))
                                                    .message("Product Details are here. %s".formatted(product))
                                                    .messageAttributes(
                                                            Map.of(
                                                                    "price", MessageAttributeValue.builder()
                                                                            .dataType(String.valueOf(product.getPrice()))
                                                                            .build()
                                                            )
                                                    )
                                                    .build())
                                            .toList()
                            )
                            .build()
            );
        }
    }
}
