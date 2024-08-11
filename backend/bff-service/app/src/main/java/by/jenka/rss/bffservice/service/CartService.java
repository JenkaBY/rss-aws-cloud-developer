package by.jenka.rss.bffservice.service;

import by.jenka.rss.bffservice.service.downstream.DownstreamServiceProvider;
import by.jenka.rss.bffservice.service.dto.DownstreamRequestContext;
import by.jenka.rss.bffservice.service.dto.DownstreamServiceResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartService implements DownstreamServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private final DownstreamServiceProvider cartProvider;

    @Override
    public DownstreamServiceResponse invoke(DownstreamRequestContext request) {
        log.info("Invoke cart by rest url '{}'", request.partUrl());
        return cartProvider.invoke(request);
    }
}
