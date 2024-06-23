package by.jenka.rss.lambda.handler;

import by.jenka.rss.lambda.config.DBConfig;
import by.jenka.rss.lambda.exception.ResourceNotFoundException;
import by.jenka.rss.lambda.handler.util.Headers;
import by.jenka.rss.lambda.handler.util.ResponseCodeAndBody;
import by.jenka.rss.lambda.model.HttpError;
import by.jenka.rss.lambda.model.Product;
import by.jenka.rss.lambda.model.ProductResponse;
import by.jenka.rss.lambda.repository.DefaultProductRepository;
import by.jenka.rss.lambda.repository.ProductRepository;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.fasterxml.jackson.jr.ob.JSON;

import java.util.Optional;


public class PostProductFromDbHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private ProductRepository productRepository = new DefaultProductRepository(DBConfig.enhancedClient);


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var logger = context.getLogger();
        logger.log("Incoming request: %s".formatted(request), LogLevel.INFO);
        var createRequest = request.getBody();

        var responseObject = getProductResponse(createRequest, context.getLogger());

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(responseObject.getCode());
        response.setHeaders(Headers.DEFAULT_API_HEADERS);
        response.setBody(responseObject.getBody());
        logger.log("Response %s".formatted(response), LogLevel.INFO);
        return response;
    }


    @SuppressWarnings("unchecked")
    private <T> ResponseCodeAndBody<T> getProductResponse(String body, LambdaLogger logger) {

        try {
            var product = JSON.std.beanFrom(Product.class, body);
            validate(product);
            return Optional.of(productRepository.upsert(product))
                    .map(ProductResponse::new)
                    .map(productResponse -> new ResponseCodeAndBody(201, productResponse))
                    .orElseGet(() ->
                            new ResponseCodeAndBody<>(
                                    500,
                                    new HttpError("Something went wrong"))
                    );
        } catch (ResourceNotFoundException rne) {
            logger.log("%s occurred.  %s".formatted(rne.getClass().getSimpleName(), rne.getMessage()), LogLevel.WARN);
            return new ResponseCodeAndBody(404, new HttpError(rne.getMessage()));
        }
        catch (NullPointerException | IllegalArgumentException any) {
            logger.log("%s occurred.  %s".formatted(any.getClass().getSimpleName(), any.getMessage()), LogLevel.WARN);
            return new ResponseCodeAndBody(400, new HttpError("The '%s' body is malformed. %s".formatted(body, any.getMessage())));
        } catch (Throwable any) {
            logger.log("%s occurred.  %s".formatted(any.getClass().getSimpleName(), any.getMessage()), LogLevel.WARN);
            return new ResponseCodeAndBody(500, new HttpError(any.getMessage()));
        }
    }

    private void validate(Product product) {
        if (product == null) throw new IllegalArgumentException("Body is missed");
        if (product.getId() != null) throw new IllegalArgumentException("Id must be null");
        if (product.getCount() < 0) throw new IllegalArgumentException("Count can't be less than 0");
        if (product.getPrice() <= 0) throw new IllegalArgumentException("Price must be positive");
        if (product.getTitle() == null || product.getTitle().isEmpty())
            throw new IllegalArgumentException("Title can't be empty");
    }

    //    only for testing purpose
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}
