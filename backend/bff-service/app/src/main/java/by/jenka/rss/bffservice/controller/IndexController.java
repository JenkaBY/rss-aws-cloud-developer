package by.jenka.rss.bffservice.controller;

import by.jenka.rss.bffservice.controller.support.ControllerUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

@Slf4j
@Controller
@RequestMapping(path = "**",
        consumes = MediaType.ALL_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class IndexController {

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE,
                    RequestMethod.OPTIONS,
                    RequestMethod.TRACE,
                    RequestMethod.HEAD})
    public ResponseEntity<String> index(@RequestBody(required = false) String body,
                                        @RequestHeader HttpHeaders headers,
                                        @RequestParam MultiValueMap<String, String> requestParams,
                                        HttpServletRequest request) {
        var restOfTheUrl = ControllerUtils.getRestOfTheUrl(request);

        log.info("Requested url: '{}'\n with Headers: {}\n with Body: {}\n with Params: {}",
                restOfTheUrl, headers, body, requestParams);
        return ResponseEntity.status(HttpStatusCode.valueOf(HttpStatus.BAD_GATEWAY.value()))
                .contentType(MediaType.APPLICATION_JSON)
                .body("Cannot process request");
    }
}
