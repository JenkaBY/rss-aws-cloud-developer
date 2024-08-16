package by.jenka.rss.bffservice.config;

import by.jenka.rss.bffservice.service.downstream.BaseDownstreamService;
import by.jenka.rss.bffservice.service.downstream.DownstreamServiceProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DownStreamServiceConfig {

    @Bean
    public RestTemplate restTemplate() {
        // supports gzip
        final var clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().build());
        return new RestTemplate(clientHttpRequestFactory);
    }

    @Bean
    public DownstreamServiceProvider cartProvider(@Value("${downstream.cart-service.base-url}") String baseUrl) {
        return new BaseDownstreamService(restTemplate(), baseUrl);
    }

    @Bean
    public DownstreamServiceProvider productProvider(@Value("${downstream.product-service.base-url}") String baseUrl) {
        return new BaseDownstreamService(restTemplate(), baseUrl);
    }
}
