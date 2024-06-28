package by.jenka.rss.lambda.handler;

import by.jenka.rss.lambda.config.DBConfig;
import by.jenka.rss.lambda.exception.ResourceNotFoundException;
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

import java.util.Optional;
import java.util.UUID;


public class GetProductByIdFromDbHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String PRODUCT_PATH_PARAM_NAME = "productId";

    private ProductRepository productRepository = new DefaultProductRepository(DBConfig.enhancedClient);


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var logger = context.getLogger();
        logger.log("Incoming request: %s".formatted(request), LogLevel.INFO);
        var productId = request.getPathParameters().get(PRODUCT_PATH_PARAM_NAME);

        var foundProductResponse = getProductResponse(productId, context.getLogger());

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(foundProductResponse.getCode());
        response.setHeaders(Headers.DEFAULT_API_HEADERS);
        response.setBody(foundProductResponse.getBody());
        logger.log("Response %s".formatted(response), LogLevel.INFO);
        return response;
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseCodeAndBody<T> getProductResponse(String productIdValue, LambdaLogger logger) {

        try {
            var productId = UUID.fromString(productIdValue);
            return Optional.ofNullable(productRepository.findByIdFromDb(productId))
                    .map(ProductResponse::new)
                    .map(product -> new ResponseCodeAndBody(200, product))
                    .orElseGet(() ->
                            new ResponseCodeAndBody<>(
                                    404,
                                    HttpError.notFound("Product", productId.toString()
                                    ))
                    );
        } catch (ResourceNotFoundException rne) {
            logger.log("%s occurred.  %s".formatted(rne.getClass().getSimpleName(), rne.getMessage()), LogLevel.WARN);
            return new ResponseCodeAndBody(404, new HttpError(rne.getMessage()));
        } catch (NullPointerException | IllegalArgumentException any) {
            logger.log("%s occurred.  %s".formatted(any.getClass().getSimpleName(), any.getMessage()), LogLevel.WARN);
            return new ResponseCodeAndBody(400, new HttpError("Provided '%s' productId is incorrect".formatted(productIdValue)));
        } catch (Throwable any) {
            logger.log("%s occurred.  %s".formatted(any.getClass().getSimpleName(), any.getMessage()), LogLevel.WARN);
            return new ResponseCodeAndBody(500, new HttpError(any.getMessage()));
        }
    }

    //    only for testing purpose
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}
