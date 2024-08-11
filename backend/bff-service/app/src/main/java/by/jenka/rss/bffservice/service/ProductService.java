package by.jenka.rss.bffservice.service;

import by.jenka.rss.bffservice.service.downstream.DownstreamServiceProvider;
import by.jenka.rss.bffservice.service.dto.DownstreamRequestContext;
import by.jenka.rss.bffservice.service.dto.DownstreamServiceResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService implements DownstreamServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final DownstreamServiceProvider productProvider;

    @Override
    @Cacheable(value = "products",
            key = "#request.partUrl()",
            condition = "'products'.equalsIgnoreCase(#request.partUrl()) && #request.httpMethod() == T(org.springframework.http.HttpMethod).GET")
    public DownstreamServiceResponse invoke(DownstreamRequestContext request) {
        log.info("Invoke product by rest url '{}'", request.partUrl());
        return productProvider.invoke(request);
    }
}
