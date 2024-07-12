package by.jenka.rss.productservice.lambda.handler;

import by.jenka.rss.productservice.lambda.service.CatalogBatchProcessor;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class CatalogBatchProcessHandlerTest {

    private CatalogBatchProcessor processor;
    private CatalogBatchProcessHandler underTest = new CatalogBatchProcessHandler();
    ;

    @BeforeEach
    void setUp() {
        processor = mock(CatalogBatchProcessor.class);
        underTest.setProcessor(processor);
    }

    @Test
    void handleRequest_Should_ReturnNullAndInvokeProcessor() {
        var input = mock(SQSEvent.class);
        when(input.getRecords()).thenReturn(List.of());
        Context context = mock(Context.class);
        LambdaLogger logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);

        var actual = underTest.handleRequest(input, context);

        assertNull(actual);
        verify(processor).processMessages(List.of(), logger);
    }
}