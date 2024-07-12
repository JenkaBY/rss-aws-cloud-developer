package by.jenka.rss.importservice.lambda.model;

import com.opencsv.bean.CsvBindByName;
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

    @CsvBindByName
    private UUID id;

    @CsvBindByName
    private String title;

    @CsvBindByName
    private String description;

    @CsvBindByName
    private int count;

    @CsvBindByName
    private double price;
}
