package by.jenka.rss.productservice.lambda.model;

import lombok.*;

import java.util.UUID;


@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {
    private UUID id;
    private String title;
    private String description;
    private int count;
    private double price;
}
