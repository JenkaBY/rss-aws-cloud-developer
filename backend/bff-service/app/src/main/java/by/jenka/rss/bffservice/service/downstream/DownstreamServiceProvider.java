package by.jenka.rss.bffservice.service.downstream;

import by.jenka.rss.bffservice.service.dto.DownstreamRequestContext;
import by.jenka.rss.bffservice.service.dto.DownstreamServiceResponse;

public interface DownstreamServiceProvider {

    DownstreamServiceResponse invoke(DownstreamRequestContext request);
}
