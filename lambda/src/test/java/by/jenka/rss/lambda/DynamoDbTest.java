package by.jenka.rss.lambda;

import by.jenka.rss.lambda.entity.ProductEntity;
import by.jenka.rss.lambda.entity.ProductStockEntity;
import by.jenka.rss.lambda.entity.ProductStockEntityJoiner;
import by.jenka.rss.lambda.entity.StockEntity;
import by.jenka.rss.lambda.repository.DefaultProductRepository;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactGetItemsEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import java.util.List;
import java.util.UUID;

import static by.jenka.rss.lambda.config.DBConfig.enhancedClient;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DynamoDbTest {

    private static DynamoDBProxyServer server;

    @BeforeAll
    static void setup() {

        String port = "8000";
        String uri = "http://localhost:" + port;
        // Create an in-memory and in-process instance of DynamoDB Local that runs over HTTP
        final String[] localArgs = {"-inMemory", "-port", port};
        System.out.println("Starting DynamoDB Local...");
        try {
            server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void run() {
        var productTable = enhancedClient.table(ProductEntity.TABLE_NAME, ProductEntity.TABLE_SCHEMA);
        productTable.createTable();
        var product = ProductEntity.builder()
                .id(UUID.randomUUID())
                .title("Title 1")
                .description("Description")
                .price(15.9)
                .build();
        productTable.putItem(product);

        var stocksTable = enhancedClient.table(StockEntity.TABLE_NAME, StockEntity.TABLE_SCHEMA);
        stocksTable.createTable();
        var stock = StockEntity.builder()
                .productId(product.getId())
                .count(5)
                .build();
        stocksTable.putItem(stock);


        var productToSave = new DefaultProductRepository(enhancedClient).findAll().iterator().next();
        var productStock = ProductStockEntity.from(productToSave);
        enhancedClient.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                .addPutItem(productTable, productStock.product())
                .addPutItem(stocksTable, productStock.stock())
                .build());

        var productEntities = new DefaultProductRepository(enhancedClient).findAllFromDb();
        var stocks = stocksTable.scan(ScanEnhancedRequest.builder().build()).items().stream().toList();

        System.out.println("ITEMS Products " + productEntities);
        System.out.println("ITEMS Stocks " + stocks);

        var document = enhancedClient.transactGetItems(
                TransactGetItemsEnhancedRequest.builder()
                        .addGetItem(productTable,
                                Key.builder().partitionValue(product.getId().toString()).build())
                        .addGetItem(stocksTable,
                                Key.builder().partitionValue(product.getId().toString()).build())
                        .build());
        ProductEntity productEntity = document.get(0).getItem(productTable);
        StockEntity stockEntity = document.get(1).getItem(stocksTable);
        System.out.println("Found in transaction " + new ProductStockEntityJoiner().join(List.of(productEntity), List.of(stockEntity)));
        assertTrue(true);
    }

    @SneakyThrows
    @AfterAll
    static void tearDown() {
        server.stop();
    }

}
