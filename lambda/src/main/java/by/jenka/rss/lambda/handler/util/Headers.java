package by.jenka.rss.lambda.handler.util;

import java.util.Map;

public class Headers {

    public static final Map<String, String> DEFAULT_API_HEADERS =
            Map.ofEntries(
                    Map.entry("contentType", "application/json"),
                    Map.entry("Access-Control-Allow-Origin", "*"),
                    Map.entry("Access-Control-Allow-Methods", "GET")
            );
}
