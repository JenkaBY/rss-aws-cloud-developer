package by.jenka.rss.bffservice.service.dto;

import jakarta.annotation.Nullable;
import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

@Builder
public record DownstreamRequestContext(String partUrl,
                                       HttpMethod httpMethod,
                                       HttpHeaders headers,
                                       MultiValueMap<String, String> queryParams,
                                       @Nullable String body) {

}
