package by.jenka.rss.bffservice.controller.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerMapping;

public class ControllerUtils {

    public static String getRestOfTheUrl(HttpServletRequest request) {
        return new AntPathMatcher()
                .extractPathWithinPattern(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
                        request.getRequestURI());
    }
}
