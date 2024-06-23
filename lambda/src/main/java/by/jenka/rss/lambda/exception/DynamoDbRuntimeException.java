package by.jenka.rss.lambda.exception;

public class DynamoDbRuntimeException extends RuntimeException {

    public DynamoDbRuntimeException(String message) {
        super(message);
    }

    public DynamoDbRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
