package by.jenka.rss.productservice.lambda.service.validator;

import by.jenka.rss.productservice.lambda.model.Product;

public class ProductValidator {

    public void validateCreate(Product product) {
        validate(product);
        if (product.getId() != null) throw new IllegalArgumentException("Id must be null");
    }

    public void validate(Product product) {
        if (product == null) throw new IllegalArgumentException("Body is missed");
        if (product.getCount() < 0) throw new IllegalArgumentException("Count can't be less than 0");
        if (product.getPrice() <= 0) throw new IllegalArgumentException("Price must be positive");
        if (product.getTitle() == null || product.getTitle().isEmpty())
            throw new IllegalArgumentException("Title can't be empty");
    }
}
