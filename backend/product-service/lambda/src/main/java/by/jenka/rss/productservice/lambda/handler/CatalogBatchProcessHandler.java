package by.jenka.rss.productservice.lambda.handler;

import by.jenka.rss.productservice.lambda.config.DBConfig;
import by.jenka.rss.productservice.lambda.repository.DefaultProductRepository;
import by.jenka.rss.productservice.lambda.repository.ProductRepository;
import by.jenka.rss.productservice.lambda.service.CatalogBatchProcessor;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.Setter;

@Setter
public class CatalogBatchProcessHandler implements RequestHandler<SQSEvent, Void> {
    private CatalogBatchProcessor processor = new CatalogBatchProcessor();

    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        var logger = context.getLogger();
        logger.log("SQS Event received. Total records " + input.getRecords().size());
        for (SQSEvent.SQSMessage msg : input.getRecords()) {
            processor.processMessage(msg, logger);
        }
        logger.log("All messages have been processed");
        return null;
    }
}
