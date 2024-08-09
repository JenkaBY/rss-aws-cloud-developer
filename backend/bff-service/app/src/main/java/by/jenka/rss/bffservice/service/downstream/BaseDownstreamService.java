package by.jenka.rss.bffservice.service.downstream;

import by.jenka.rss.bffservice.service.dto.DownstreamRequestContext;
import by.jenka.rss.bffservice.service.dto.DownstreamServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class BaseDownstreamService implements DownstreamServiceProvider {

    private final RestTemplate restTemplate;

    private final String baseUrl;

    public BaseDownstreamService(RestTemplate restTemplate, String baseUrl) {
        log.info("Initialize a client for {}", baseUrl);
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public DownstreamServiceResponse invoke(DownstreamRequestContext request) {

        var uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path(request.partUrl())
                .queryParams(request.queryParams())
                .encode()
                .build()
                .toUri();
        var requestHeaders = filterRequestHeaders(request);
        log.info("-->>[{}] request [{}] with headers: '{}' with body '{}'", request.httpMethod(), uri, requestHeaders, request.body());
        var httpEntity = new HttpEntity<>(request.body(), requestHeaders);
        var response = restTemplate.exchange(uri, request.httpMethod(), httpEntity, String.class);
        log.info("<<--Response: headers: {}, status: {}, body {}", response.getHeaders(), response.getStatusCode(), response.getBody());

        var responseHeaders = getHttpResponseHeaders();

        log.info("<<--Response headers: {}", responseHeaders);
        return DownstreamServiceResponse.builder()
                .statusCode(response.getStatusCode())
                .body(response.getBody())
                .headers(responseHeaders)
                .build();

    }

    private static HttpHeaders getHttpResponseHeaders() {
        var responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        responseHeaders.add("Proxied-By", "BFF-Service");
        responseHeaders.setAccessControlAllowOrigin("*");
        responseHeaders.setAccessControlAllowCredentials(true);
        responseHeaders.setAccessControlAllowMethods(Arrays.asList(
                HttpMethod.GET,
                HttpMethod.DELETE,
                HttpMethod.POST,
                HttpMethod.PUT,
                HttpMethod.OPTIONS));
        responseHeaders.setAccessControlAllowHeaders(
                List.of("Content-Type", "Authorization", "Proxied-By"));
        return responseHeaders;
    }

    private static HttpHeaders filterRequestHeaders(DownstreamRequestContext request) {
        var requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        if (request.headers().containsKey("Authorization")) {
            log.info("Authorization header value {}", request.headers().get("Authorization"));
            requestHeaders.setBasicAuth(request.headers().get("Authorization").getFirst().split(" ")[1]);
        }
        return requestHeaders;
    }
}
