package by.jenka.rss.productservice.lambda.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@DynamoDbBean
public class StockEntity {
    public static final String TABLE_NAME = System.getenv().getOrDefault("STOCK_TABLE", "stock-manually");

    public static final TableSchema<StockEntity> TABLE_SCHEMA = TableSchema.fromBean(StockEntity.class);

    @DynamoDbPartitionKey
    public UUID getProductId() {
        return productId;
    }

    private UUID productId;

    private int count;
}
