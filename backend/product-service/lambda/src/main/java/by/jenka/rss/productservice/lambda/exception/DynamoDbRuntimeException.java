package by.jenka.rss.productservice.lambda.exception;

public class DynamoDbRuntimeException extends RuntimeException {

    public DynamoDbRuntimeException(String message) {
        super(message);
    }

    public DynamoDbRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
