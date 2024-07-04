package by.jenka.rss.productservice.lambda.service;

import by.jenka.rss.productservice.lambda.config.DBConfig;
import by.jenka.rss.productservice.lambda.model.Product;
import by.jenka.rss.productservice.lambda.repository.DefaultProductRepository;
import by.jenka.rss.productservice.lambda.repository.ProductRepository;
import by.jenka.rss.productservice.lambda.service.validator.ProductValidator;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.jr.ob.JSON;

public class CatalogBatchProcessor {

    private ProductRepository productRepository = new DefaultProductRepository(DBConfig.enhancedClient);
    private ProductValidator productValidator = new ProductValidator();

    public void processMessage(SQSEvent.SQSMessage msg, LambdaLogger logger) {
        logger.log("----------NEW-MESSAGE--------");
        logger.log("Message " + msg);
        logger.log("Message body" + msg.getBody());
        logger.log("Message attrs" + msg.getMessageAttributes());
        logger.log("Message event source" + msg.getEventSource());
        persistProduct(msg, logger);
    }

    private void persistProduct(SQSEvent.SQSMessage msg, LambdaLogger logger) {
        try {
            var product = JSON.std.beanFrom(Product.class, msg.getBody());
            productValidator.validate(product);
            productRepository.upsert(product);
        } catch (Throwable any) {
            logger.log("Couldn't process the message: %s".formatted(msg.getBody()));
        }
    }
}
