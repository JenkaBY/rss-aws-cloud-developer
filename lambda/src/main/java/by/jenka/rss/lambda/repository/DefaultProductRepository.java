package by.jenka.rss.lambda.repository;

import by.jenka.rss.lambda.entity.ProductEntity;
import by.jenka.rss.lambda.entity.ProductStockEntity;
import by.jenka.rss.lambda.entity.ProductStockEntityJoiner;
import by.jenka.rss.lambda.entity.StockEntity;
import by.jenka.rss.lambda.exception.DynamoDbRuntimeException;
import by.jenka.rss.lambda.model.Product;
import com.amazonaws.services.dynamodbv2.model.CancellationReason;
import com.amazonaws.services.dynamodbv2.model.TransactionCanceledException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactGetItemsEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.utils.Logger;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DefaultProductRepository implements ProductRepository {

    private static final Logger logger = Logger.loggerFor(DefaultProductRepository.class);

    private final static ProductStockEntityJoiner JOINER = new ProductStockEntityJoiner();
    private DynamoDbEnhancedClient enhancedClient;
    private DynamoDbTable<ProductEntity> productTable;
    private DynamoDbTable<StockEntity> stockTable;

    private final static Map<UUID, Product> PRODUCTS = Stream.of(
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a1"), "Mock Product 1", "Short Product description", 1, 2.5),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a2"), "Mock Product 2", "Short Product description", 2, 1),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a3"), "Mock Product 3", "Short Product description", 3, 2.1),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a4"), "Mock Product 4", "Short Product description", 4, 10),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a5"), "Mock Product 5", "Short Product description", 5, 5.63)
    ).collect(Collectors.toMap(Product::getId, Function.identity()));

    public DefaultProductRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.productTable = enhancedClient.table(ProductEntity.TABLE_NAME, ProductEntity.TABLE_SCHEMA);
        this.stockTable = enhancedClient.table(StockEntity.TABLE_NAME, StockEntity.TABLE_SCHEMA);
    }

    @Override
    public Optional<Product> findProductById(UUID productId) {
        return Optional.of(productId)
                .map(PRODUCTS::get)
                .or(Optional::empty);
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(PRODUCTS.values());
    }

    @Override
    public Product upsert(Product input) {
        var productId = input.getId();
        Callable<Product> upsertProduct;
        if (productId == null) {
            upsertProduct = () -> {
                var productStock = ProductStockEntity.from(input);
                enhancedClient.transactWriteItems(
                        TransactWriteItemsEnhancedRequest.builder()
                                .addPutItem(productTable, productStock.product())
                                .addPutItem(stockTable, productStock.stock())
                                .build()
                );
                return productStock.model();
            };
        } else {
            var foundProduct = findByIdFromDb(productId);
            if (foundProduct == null) {
                throw new NullPointerException("Product Not Found %s in DB".formatted(productId));
            }
            upsertProduct = () -> {
                var productStock = ProductStockEntity.from(foundProduct);
                enhancedClient.transactWriteItems(
                        TransactWriteItemsEnhancedRequest.builder()
                                .addUpdateItem(productTable, productStock.product())
                                .addUpdateItem(stockTable, productStock.stock())
                                .build()
                );
                return input;
            };
        }
        return wrapByExceptionHandler(upsertProduct);
    }

    @Override
    public Product findByIdFromDb(UUID id) {
        Callable<Product> getById = () -> {
            var productId = id.toString();
            var document = enhancedClient.transactGetItems(
                    TransactGetItemsEnhancedRequest.builder()
                            .addGetItem(productTable,
                                    Key.builder().partitionValue(productId).build())
                            .addGetItem(stockTable,
                                    Key.builder().partitionValue(productId).build())
                            .build());
            if (document.get(0) == null && document.get(1) == null) {
                return null;
            }
            if (document.get(0) != null && document.get(1) == null
                    || document.get(1) != null && document.get(0) == null) {
                throw new DynamoDbRuntimeException("Product and Stock entities inconsistent. ProductId %s".formatted(productId));
            }
            var productEntity = document.get(0).getItem(productTable);
            var stockEntity = document.get(1).getItem(stockTable);
            if (Stream.of(productEntity, stockEntity).allMatch(Objects::isNull)) return null;
            if (Stream.of(productEntity, stockEntity).anyMatch(Objects::isNull))
                throw new DynamoDbRuntimeException("Product and Stock entities inconsistent. ProductId %s".formatted(productId));
            return JOINER.join(List.of(productEntity), List.of(stockEntity)).iterator().next().model();
        };
        return wrapByExceptionHandler(getById);
    }

    @Override
    public List<Product> findAllFromDb() {

        var fullScanRequest = ScanEnhancedRequest.builder()
                .build();

        Callable<List<Product>> callDbAndConvert = () -> {
            var products = collectFromPages(productTable.scan(fullScanRequest));
            var stocks = collectFromPages(stockTable.scan(fullScanRequest));
            return JOINER.join(products, stocks).stream()
                    .map(ProductStockEntity::model)
                    .toList();
        };

        return wrapByExceptionHandler(callDbAndConvert);
    }

    private <T> T wrapByExceptionHandler(Callable<T> execution) {
        try {
            return execution.call();
        } catch (TransactionCanceledException ex) {
            var cancellationReasons = ex.getCancellationReasons()
                    .stream()
                    .map(CancellationReason::toString)
                    .collect(Collectors.joining(","));
            System.out.println("Transaction cancelled. " + cancellationReasons);
            throw new DynamoDbRuntimeException("Transaction cancelled by " + cancellationReasons);
        } catch (Throwable any) {
            logger.error(() -> "WTF? {}", any);
            System.out.println("ERROR: " + any.getClass().getSimpleName() + ": " + any.getMessage());
            any.printStackTrace();
            throw new DynamoDbRuntimeException("Exception occurred during DynamoDB request", any);
        }
    }

    private <T> List<T> collectFromPages(PageIterable<T> pages) {
        return pages.stream()
                .flatMap(s -> s.items().stream())
                .toList();
    }
}
