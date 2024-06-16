package by.jenka.rss.lambda.handler;

import by.jenka.rss.lambda.handler.util.Headers;
import by.jenka.rss.lambda.handler.util.ResponseCodeAndBody;
import by.jenka.rss.lambda.model.HttpError;
import by.jenka.rss.lambda.model.ProductResponse;
import by.jenka.rss.lambda.repository.DefaultProductRepository;
import by.jenka.rss.lambda.repository.ProductRepository;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;

import java.util.UUID;

public class GetProductByIdHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final String PRODUCT_PATH_PARAM_NAME = "productId";

    // Only for testing purpose
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    private ProductRepository productRepository = new DefaultProductRepository();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

        var productId = request.getPathParameters().get(PRODUCT_PATH_PARAM_NAME);

        var foundProductResponse = getProductResponse(productId, context.getLogger());

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(foundProductResponse.getCode());
        response.setHeaders(Headers.DEFAULT_API_HEADERS);
        response.setBody(foundProductResponse.getBody());

        return response;
    }

    private <T> ResponseCodeAndBody<T> getProductResponse(String productIdValue, LambdaLogger logger) {

        try {
            var productId = UUID.fromString(productIdValue);
            return productRepository.findProductById(productId)
                    .map(ProductResponse::new)
                    .map(product ->
                            new ResponseCodeAndBody(200, product))
                    .orElseGet(() ->
                            new ResponseCodeAndBody<>(
                                    404,
                                    HttpError.notFound("Product", productId.toString()
                                    ))
                    );
        } catch (NullPointerException | IllegalArgumentException any) {
            logger.log("%s occurred.  %s".formatted(any.getClass().getSimpleName(), any.getMessage()), LogLevel.WARN);
            return new ResponseCodeAndBody(404, new HttpError("Provided '%s' productId is incorrect".formatted(productIdValue)));
        }
        catch (Throwable any) {
            logger.log("%s occurred.  %s".formatted(any.getClass().getSimpleName(), any.getMessage()), LogLevel.WARN);
            return new ResponseCodeAndBody(500, new HttpError(any.getMessage()));
        }
    }
}
