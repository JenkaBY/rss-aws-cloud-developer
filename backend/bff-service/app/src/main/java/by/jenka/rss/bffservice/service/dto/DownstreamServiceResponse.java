package by.jenka.rss.bffservice.service.dto;

import jakarta.annotation.Nullable;
import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

@Builder
public record DownstreamServiceResponse(HttpStatusCode statusCode,
                                        @Nullable String body,
                                        HttpHeaders headers
) {
}
