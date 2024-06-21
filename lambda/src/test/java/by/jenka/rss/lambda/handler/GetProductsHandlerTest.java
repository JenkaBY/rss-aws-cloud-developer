package by.jenka.rss.lambda.handler;

import by.jenka.rss.lambda.handler.util.Headers;
import by.jenka.rss.lambda.model.Product;
import by.jenka.rss.lambda.repository.DefaultProductRepository;
import by.jenka.rss.lambda.repository.ProductRepository;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class GetProductsHandlerTest {
    private ProductRepository productRepository;
    private GetProductsHandler underTest = new GetProductsHandler();
    private Context context;

    @BeforeEach
    void setUp() {
        productRepository = mock(DefaultProductRepository.class);
        underTest.setProductRepository(productRepository);
        context = mock(Context.class);
        when(context.getLogger()).thenReturn(mock(LambdaLogger.class));
    }


    @Nested
    class Success {

        @Test
        void shouldReturn200WhenNoExceptionThrownByRepo() {
            when(productRepository.findAll())
                    .thenReturn(
                            List.of(
                                    new Product(UUID.randomUUID(), "test", "test", 1, 2),
                                    new Product(UUID.randomUUID(), "test", "test", 2, 3.1)
                            ));

            var input = mock(APIGatewayProxyRequestEvent.class);
            var actual = underTest.handleRequest(input, context);

            assertNotNull(actual);
            assertEquals(Headers.DEFAULT_API_HEADERS, actual.getHeaders());
            assertEquals(200, actual.getStatusCode());
            assertNotNull(actual.getBody());
            var actualBody = JsonPath.parse(actual.getBody());
            assertEquals(2, (Integer) actualBody.read("$.length()"));
        }
    }

    @Nested
    class Fail {

        @Test
        void shouldReturn500WhenExceptionThrownByRepo() {
            doThrow(new RuntimeException("Test")).when(productRepository).findAll();

            var input = mock(APIGatewayProxyRequestEvent.class);
            var actual = underTest.handleRequest(input, context);

            assertNotNull(actual);
            assertEquals(Headers.DEFAULT_API_HEADERS, actual.getHeaders());
            assertEquals(500, actual.getStatusCode());
            assertNotNull(actual.getBody());
            var actualBody = JsonPath.parse(actual.getBody());
            assertEquals("Test", actualBody.read("$.message"));
        }
    }
}