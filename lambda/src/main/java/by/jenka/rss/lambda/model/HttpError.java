package by.jenka.rss.lambda.model;


public class HttpError {
    private String message;

    public HttpError(String message) {
        this.message = message;
    }


    public static HttpError notFound(String resource, String key) {
        return new HttpError("Resource '%s' not found by key '%s'".formatted(resource, key));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
