package by.jenka.rss.importservice.lambda.service.utils;

import by.jenka.rss.importservice.lambda.model.ProductAvailable;
import com.fasterxml.jackson.jr.ob.JSON;

import java.io.IOException;

public class JsonSerializer {

    public static String serialize(ProductAvailable productAvailable) {
        try {
            return JSON.std.asString(productAvailable);
        } catch (IOException e) {
            throw new RuntimeException("Cannot serialize JSON", e);
        }
    }
}
