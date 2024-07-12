package by.jenka.rss.importservice.lambda.service;

import by.jenka.rss.importservice.lambda.model.ProductAvailable;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;

class ProductParserTest {

    private final ProductParser underTest = new DefaultProductParser();

    @SneakyThrows
    @Test
    void parse() {
        try (var inputStream = this.getClass().getClassLoader().getResourceAsStream("products.csv")) {
            Objects.requireNonNull(inputStream);
            var isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            var actual = underTest.parse(isr, mock(LambdaLogger.class));

            System.out.println("Actual: " + actual);
            assertArrayEquals(List.of(
                    ProductAvailable.builder()
                            .id(UUID.fromString("6f004529-d043-40de-8189-aa7fcf1fa07b"))
                            .title("TestName1").description("Description 1").price(12.34).count(5).build(),
                    ProductAvailable.builder()
                            .title("TestName2").description("Description 2").price(1.56).count(1).build()
            ).toArray(ProductAvailable[]::new), actual.toArray(ProductAvailable[]::new));
        }
    }
}