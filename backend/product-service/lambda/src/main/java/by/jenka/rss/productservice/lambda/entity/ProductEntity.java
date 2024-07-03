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
public class ProductEntity {
    public static final String TABLE_NAME = System.getenv().getOrDefault("PRODUCT_TABLE", "product-manually");
    public static final TableSchema<ProductEntity> TABLE_SCHEMA = TableSchema.fromBean(ProductEntity.class);

    private UUID id;

    private String title;

    private String description;

    private double price;

    @DynamoDbPartitionKey
    public UUID getId() {
        return id;
    }
}
