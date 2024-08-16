package by.jenka.rss.bffservice.controller;

import by.jenka.rss.bffservice.service.ProductService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = "/product/**", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductRestController extends DelegateRestController {


    public ProductRestController(ProductService productService) {
        super(productService);
    }

    @RequestMapping(method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.PATCH,
            RequestMethod.DELETE,
            RequestMethod.TRACE,
            RequestMethod.HEAD})
    public ResponseEntity<String> productRequest(@RequestBody(required = false) @Nullable String body,
                                              @RequestHeader HttpHeaders headers,
                                              @RequestParam MultiValueMap<String, String> requestParams,
                                              HttpServletRequest request) {
        log.info("Request Products: {}", request.getRequestURI());
        return super.delegateToProvider(body, headers, requestParams, request);
    }

}
