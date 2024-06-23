package by.jenka.rss.lambda.entity;

import by.jenka.rss.lambda.model.Product;
import lombok.NonNull;

import java.util.Objects;
import java.util.UUID;

public record ProductStockEntity(@NonNull ProductEntity product, @NonNull StockEntity stock) {

    public Product model() {
        Objects.requireNonNull(product);
        Objects.requireNonNull(stock);
        if (!product.getId().equals(stock.getProductId())) {
            throw new IllegalArgumentException("Product.id %s and Stock.productId %s must be equal".formatted(product.getId(), stock.getProductId()));
        }
        return Product.builder()
                .id(product.getId())
                .count(stock.getCount())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

    public static ProductStockEntity from(Product input) {
        var id = input.getId()  == null ? UUID.randomUUID() : input.getId();
        return new ProductStockEntity(
                ProductEntity.builder()
                        .id(id)
                        .price(input.getPrice())
                        .description(input.getDescription())
                        .title(input.getTitle())
                        .build(),
                StockEntity.builder()
                        .productId(id)
                        .count(input.getCount())
                        .build()
        );
    }
}
