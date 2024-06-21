package by.jenka.rss.lambda.repository;

import by.jenka.rss.lambda.model.Product;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultProductRepository implements ProductRepository {

    private static Map<UUID, Product> PRODUCTS = Stream.of(
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a1"), "Mock Product 1", "Short Product description", 1, 2.5),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a2"), "Mock Product 2", "Short Product description", 2, 1),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a3"), "Mock Product 3", "Short Product description", 3, 2.1),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a4"), "Mock Product 4", "Short Product description", 4, 10),
            new Product(UUID.fromString("7567ec4b-b10c-48c5-9345-fc73c48a80a5"), "Mock Product 5", "Short Product description", 5, 5.63)
            ).collect(Collectors.toMap(Product::id, Function.identity()));

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
}
