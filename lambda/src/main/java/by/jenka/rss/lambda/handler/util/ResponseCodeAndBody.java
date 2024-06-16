package by.jenka.rss.lambda.handler.util;

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.IOException;

public class ResponseCodeAndBody<T> {
    private final Integer code;
    private final String body;

    public ResponseCodeAndBody(Integer code, T body) {
        this.code = code;
        try {
            this.body = JSON.std.asString(body);
        } catch (IOException e) {
            throw new RuntimeException("Cannot serialize JSON", e);
        }
    }

    public Integer getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }
}
