package by.jenka.rss.productservice.lambda.repository;

import by.jenka.rss.productservice.lambda.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductRepository {

    Product upsert(Product input);

    Product findById(UUID id);

    List<Product> findAll();
}
