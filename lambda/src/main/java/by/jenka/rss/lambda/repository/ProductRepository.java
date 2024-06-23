package by.jenka.rss.lambda.repository;

import by.jenka.rss.lambda.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Optional<Product> findProductById(UUID productId);

    List<Product> findAll();

    Product upsert(Product input);

    Product findByIdFromDb(UUID id);

    List<Product> findAllFromDb();
}
