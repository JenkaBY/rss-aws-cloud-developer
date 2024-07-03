package by.jenka.rss.importservice.lambda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductAvailable {
    private UUID id;
    private String title;
    private String description;
    private int count;
    private double price;
}
