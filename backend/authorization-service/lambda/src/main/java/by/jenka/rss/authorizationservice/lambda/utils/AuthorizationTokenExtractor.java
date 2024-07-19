package by.jenka.rss.authorizationservice.lambda.utils;

import java.util.Base64;
import java.util.Map;

public class AuthorizationTokenExtractor {

    private static final String BASIC_TOKEN_PREFIX = "Basic ";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static String getBasicAuthorizationToken(Map<String, String> headers) {
        var headerValue = headers.get(HEADER_AUTHORIZATION);
        if (headerValue == null) {
            System.out.printf("WARN. No %s header is present%n", HEADER_AUTHORIZATION);
            throw new RuntimeException("401 Unauthorized. Authorization is missed");
        }

        if (headerValue.startsWith(BASIC_TOKEN_PREFIX)) {
            var pairBase64 = headerValue.substring(BASIC_TOKEN_PREFIX.length());
            var credentials = Base64.getDecoder().decode(pairBase64);
            return new String(credentials);
        }
        throw new RuntimeException("403 Access is denied");
    }
}
