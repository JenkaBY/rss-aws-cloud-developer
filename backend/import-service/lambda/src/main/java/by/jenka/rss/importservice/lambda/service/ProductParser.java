package by.jenka.rss.importservice.lambda.service;

import by.jenka.rss.importservice.lambda.model.ProductAvailable;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.io.InputStreamReader;
import java.util.List;

public interface ProductParser {
    List<ProductAvailable> parse(InputStreamReader inputStream, LambdaLogger logger);
}
