package by.jenka.rss.importservice.lambda.service;

import by.jenka.rss.importservice.lambda.model.ProductAvailable;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class PrinterProductParser implements ProductParser {

    @Override
    public List<ProductAvailable> parse(InputStreamReader inputStream, LambdaLogger logger) {
        try {
            int linesRead = 0;
            try (var reader = new CSVReader(new BufferedReader(inputStream))) {
                // Skip headers
                reader.skip(1);
                linesRead++;
                String[] values;
                while ((values = reader.readNext()) != null) {
                    logger.log("line[%s] with product: %s".formatted(linesRead, Arrays.toString(values)));
                    linesRead++;
                }
            }
            logger.log("Total lines read: " + linesRead);
        } catch (IOException | CsvValidationException e) {
            System.err.println("Can't read file because of " + e.getMessage());
            throw new RuntimeException(e);
        }
        return List.of();
    }
}
