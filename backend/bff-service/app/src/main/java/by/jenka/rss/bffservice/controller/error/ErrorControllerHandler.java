package by.jenka.rss.bffservice.controller.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class ErrorControllerHandler {

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(Throwable.class)
    public String handle(RestClientException exception) {
        log.warn("External service is unavailable", exception);
        return "External service is unavailable";
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handleClientError(HttpStatusCodeException exception) {
        log.warn("Client responded with error: {}", exception.getMessage());
        var responseHeaders = Optional.ofNullable(exception.getResponseHeaders()).map(HttpHeaders::writableHttpHeaders)
                .orElse(new HttpHeaders());
        responseHeaders.add("Proxied-By", "BFF-Service");

        return ResponseEntity.status(exception.getStatusCode())
                .headers(exception.getResponseHeaders())
                .body(exception.getResponseBodyAsString(StandardCharsets.UTF_8));
    }
}
