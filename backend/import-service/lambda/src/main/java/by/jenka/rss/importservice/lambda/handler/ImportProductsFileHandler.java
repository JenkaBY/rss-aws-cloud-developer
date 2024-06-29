package by.jenka.rss.importservice.lambda.handler;

import by.jenka.rss.importservice.lambda.config.S3Config;
import by.jenka.rss.importservice.lambda.service.S3PreSignedRequestService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;

import java.util.Map;

import static by.jenka.rss.importservice.lambda.handler.util.Headers.DEFAULT_API_HEADERS;

public class ImportProductsFileHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String FOLDER_FOR_UPLOAD = System.getenv().getOrDefault("FOLDER_FOR_UPLOAD", "replace-me");
    private S3PreSignedRequestService preSignedRequestService = new S3PreSignedRequestService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        var logger = context.getLogger();
        logger.log("Incoming request: \n%s".formatted(input), LogLevel.INFO);
        var fileName = input.getQueryStringParameters().get("name");
        if (fileName == null) {
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setStatusCode(400);
            response.setHeaders(DEFAULT_API_HEADERS);
            response.setBody("{\"message\":\"name query parameter must present\"}");
            logger.log("Response %s".formatted(response), LogLevel.WARN);
            return response;
        }

        var key = "%s/%s".formatted(FOLDER_FOR_UPLOAD, uniqueFileName(fileName));
        var bucketName = S3Config.IMPORT_BUCKET_NAME;
        logger.log("Create preSignedURL bucket [%s], key=[%s]".formatted(bucketName, key));
        var preSignedPutRequest = preSignedRequestService.preSignedPutRequest(bucketName, key, "text/csv");
        logger.log("PreSigned URL: [%s] for [%s] method"
                .formatted(preSignedPutRequest.url().toExternalForm(),
                        preSignedPutRequest.httpRequest().method()));

        var response = new APIGatewayProxyResponseEvent();
        response.setHeaders(Map.ofEntries(
                Map.entry("Content-Type", "application/json"),
                Map.entry("Access-Control-Allow-Origin", "*"),
                Map.entry("Access-Control-Allow-Methods", "*"),
                Map.entry("Access-Control-Allow-Headers",
                        "Content-Type,Authorization,X-Api-Key,X-Amz-Date,X-Amz-Security-Token")
        ));
        response.setStatusCode(200);
        response.setBody(
//                language=json
                """
                        {"url": "%s"}
                        """.formatted(
                        preSignedPutRequest.url().toExternalForm()));
        return response;
    }


    private String uniqueFileName(String fileNameWithExt) {
        var nameAndExt = fileNameWithExt.split("\\.");
        return nameAndExt[0]
                + "-" + System.currentTimeMillis()
                + "." + nameAndExt[1];
    }
}
