package by.jenka.rss.productservice.lambda.handler;

import by.jenka.rss.productservice.lambda.service.CatalogBatchProcessor;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import lombok.Setter;

@Setter
public class CatalogBatchProcessHandler implements RequestHandler<SQSEvent, Void> {
    private CatalogBatchProcessor processor = new CatalogBatchProcessor();

    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        var logger = context.getLogger();
        logger.log("SQS Event received. Total records " + input.getRecords().size());
        try {
            processor.processMessages(input.getRecords(), logger);
        } catch (Throwable any) {
            logger.log("Error occurred " + any.getClass() + ". " + any.getMessage() + ". The creation of products has been skipped", LogLevel.ERROR);
        }
        logger.log("All messages have been processed");
        return null;
    }
}
