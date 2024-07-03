package by.jenka.rss.productservice.lambda.entity;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProductStockEntityJoiner {

    public List<ProductStockEntity> join(List<ProductEntity> products, List<StockEntity> stocks) {
        var stocksMap = stocks.stream().collect(Collectors.toMap(StockEntity::getProductId, Function.identity()));
        return products.stream()
                .map(product -> new ProductStockEntity(product, stocksMap.get(product.getId())))
                .toList();
    }
}
