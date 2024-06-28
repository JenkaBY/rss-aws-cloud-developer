package by.jenka.rss.lambda.config;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

public class DBConfig {

    private static final DynamoDbClient standardClient;

    static {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .httpClient(UrlConnectionHttpClient.builder().build())
                .region(Region.EU_NORTH_1);
        standardClient = ("PROD".equals(System.getenv("ENV"))
                ? builder : builder.endpointOverride(URI.create("http://localhost:8000")))
                .build();

    }

    // Use the configured standard client with the enhanced client.
    public static final DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(standardClient)
            .build();
}
