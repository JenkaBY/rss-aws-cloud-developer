package by.jenka.rss.lambda.handler;

import by.jenka.rss.lambda.handler.util.Headers;
import by.jenka.rss.lambda.handler.util.ResponseCodeAndBody;
import by.jenka.rss.lambda.model.HttpError;
import by.jenka.rss.lambda.repository.DefaultProductRepository;
import by.jenka.rss.lambda.repository.ProductRepository;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;


public class GetProductsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private ProductRepository productRepository = new DefaultProductRepository();


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

        var logger = context.getLogger();
        logger.log("Request: %s".formatted(request), LogLevel.INFO);

        var bodyAndCode = getProductResponse();

        var response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(bodyAndCode.getCode());
        response.setBody(bodyAndCode.getBody());
        response.setHeaders(
                Headers.DEFAULT_API_HEADERS
        );
        logger.log("Response: %s".formatted(response), LogLevel.INFO);
        return response;
    }

    private <T> ResponseCodeAndBody<T> getProductResponse() {

        try {
            return new ResponseCodeAndBody(200, productRepository.findAll());
        } catch (Throwable any) {
            return new ResponseCodeAndBody(500, new HttpError(any.getMessage()));
        }
    }

//    only for testing purpose
    public void setProductRepository(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}
