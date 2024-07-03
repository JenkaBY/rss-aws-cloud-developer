package by.jenka.rss.productservice.lambda.handler;

import by.jenka.rss.productservice.lambda.config.DBConfig;
import by.jenka.rss.productservice.lambda.handler.util.Headers;
import by.jenka.rss.productservice.lambda.handler.util.ResponseCodeAndBody;
import by.jenka.rss.productservice.lambda.model.HttpError;
import by.jenka.rss.productservice.lambda.repository.DefaultProductRepository;
import by.jenka.rss.productservice.lambda.repository.ProductRepository;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import lombok.Setter;


@Setter
public class GetProductsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private ProductRepository productRepository = new DefaultProductRepository(DBConfig.enhancedClient);


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var logger = context.getLogger();
        logger.log("Request %s".formatted(request));
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
}
