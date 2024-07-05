package by.jenka.rss.importservice.lambda.service;

import by.jenka.rss.importservice.lambda.model.ProductAvailable;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStreamReader;
import java.util.List;

public class DefaultProductParser implements ProductParser {

    @Override
    public List<ProductAvailable> parse(InputStreamReader inputStream, LambdaLogger logger) {

        return new CsvToBeanBuilder<ProductAvailable>(inputStream)
                .withType(ProductAvailable.class)
                .build()
                .parse();
    }
}
