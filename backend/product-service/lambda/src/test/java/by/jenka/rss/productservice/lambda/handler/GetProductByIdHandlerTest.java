package by.jenka.rss.productservice.lambda.handler;

import by.jenka.rss.productservice.lambda.handler.util.Headers;
import by.jenka.rss.productservice.lambda.model.Product;
import by.jenka.rss.productservice.lambda.repository.DefaultProductRepository;
import by.jenka.rss.productservice.lambda.repository.ProductRepository;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class GetProductByIdHandlerTest {
    private static final UUID EXISTING_ID = UUID.randomUUID();
    private ProductRepository productRepository;
    private GetProductByIdHandler underTest = new GetProductByIdHandler();
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
            var input = mock(APIGatewayProxyRequestEvent.class);
            when(input.getPathParameters()).thenReturn(Map.of("productId", EXISTING_ID.toString()));
            when(productRepository.findById(EXISTING_ID))
                    .thenReturn(new Product(EXISTING_ID, "test", "testDescription", 1, 2));

            var actual = underTest.handleRequest(input, context);

            assertNotNull(actual);
            assertEquals(Headers.DEFAULT_API_HEADERS, actual.getHeaders());
            assertEquals(200, actual.getStatusCode());
            assertNotNull(actual.getBody());
            var actualBody = JsonPath.parse(actual.getBody());
            assertEquals(EXISTING_ID.toString(), actualBody.read("$.product.id"));
            assertEquals("test", actualBody.read("$.product.title"));
            assertEquals("testDescription", actualBody.read("$.product.description"));
            assertEquals(1, (Integer) actualBody.read("$.product.count"));
            assertEquals(2, (Double) actualBody.read("$.product.price"));
        }
    }

    @Nested
    class Fail {

        @Nested
        class ShouldReturn400WhenInvalidProductId {

            @Test
            void productIdWasNotProvided() {
                var input = mock(APIGatewayProxyRequestEvent.class);
                when(input.getPathParameters()).thenReturn(Map.of());

                var actual = underTest.handleRequest(input, context);

                assertNotNull(actual);
                assertEquals(Headers.DEFAULT_API_HEADERS, actual.getHeaders());
                assertEquals(400, actual.getStatusCode());
                assertNotNull(actual.getBody());
                var actualBody = JsonPath.parse(actual.getBody());
                assertEquals("Provided 'null' productId is incorrect", actualBody.read("$.message"));
            }

            @Test
            void productIdIsNotUUID() {
                var input = mock(APIGatewayProxyRequestEvent.class);
                when(input.getPathParameters()).thenReturn(Map.of("productId", "NOT_UUID"));

                var actual = underTest.handleRequest(input, context);

                assertNotNull(actual);
                assertEquals(Headers.DEFAULT_API_HEADERS, actual.getHeaders());
                assertEquals(400, actual.getStatusCode());
                assertNotNull(actual.getBody());
                var actualBody = JsonPath.parse(actual.getBody());
                assertEquals("Provided 'NOT_UUID' productId is incorrect", actualBody.read("$.message"));
            }
        }


        @Test
        void should_Return500_WhenExceptionThrownByRepo() {
            var input = mock(APIGatewayProxyRequestEvent.class);
            when(input.getPathParameters()).thenReturn(Map.of("productId", EXISTING_ID.toString()));
            doThrow(new RuntimeException("Test")).when(productRepository).findById(EXISTING_ID);

            var actual = underTest.handleRequest(input, context);

            assertNotNull(actual);
            assertEquals(Headers.DEFAULT_API_HEADERS, actual.getHeaders());
            assertEquals(500, actual.getStatusCode());
            assertNotNull(actual.getBody());
            var actualBody = JsonPath.parse(actual.getBody());
            assertEquals("Test", actualBody.read("$.message"));
        }

        @Test
        void should_Return404_WhenExceptionThrownByRepo() {
            var input = mock(APIGatewayProxyRequestEvent.class);
            when(input.getPathParameters()).thenReturn(Map.of("productId", EXISTING_ID.toString()));
            when(productRepository.findById(EXISTING_ID))
                    .thenReturn(null);

            var actual = underTest.handleRequest(input, context);

            assertNotNull(actual);
            assertEquals(Headers.DEFAULT_API_HEADERS, actual.getHeaders());
            assertEquals(404, actual.getStatusCode());
            assertNotNull(actual.getBody());
            var actualBody = JsonPath.parse(actual.getBody());
            assertEquals("Resource '%s' not found by key '%s'".formatted("Product", EXISTING_ID), actualBody.read("$.message"));
        }
    }
}