package by.jenka.rss.bffservice.controller;


import by.jenka.rss.bffservice.controller.support.ControllerUtils;
import by.jenka.rss.bffservice.service.downstream.DownstreamServiceProvider;
import by.jenka.rss.bffservice.service.dto.DownstreamRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

@Slf4j
@RequiredArgsConstructor
class DelegateRestController {

    private final DownstreamServiceProvider provider;

    protected ResponseEntity<String> delegateToProvider(@Nullable String body,
                                           HttpHeaders headers,
                                           MultiValueMap<String, String> requestParams,
                                           HttpServletRequest request) {
        var restOfTheUrl = ControllerUtils.getRestOfTheUrl(request);

        log.info("\nRequested url: '{}'\n with Headers: {}\n with Body: {}\n with Params: {}",
                restOfTheUrl, headers, body, requestParams);
        var response = provider.invoke(DownstreamRequestContext.builder()
                .httpMethod(HttpMethod.valueOf(request.getMethod()))
                .body(body)
                .headers(headers)
                .queryParams(requestParams)
                .partUrl(restOfTheUrl)
                .build());
        return ResponseEntity.status(response.statusCode())
                .headers(response.headers())
                .body(response.body());
    }
}
