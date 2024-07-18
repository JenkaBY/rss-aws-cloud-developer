package by.jenka.rss.backend.cartservice.changelog;

import by.jenka.rss.backend.cartservice.changelog.config.DataSource;
import by.jenka.rss.backend.cartservice.changelog.service.LiquibaseRunner;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ChangelogHandler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        var logger = context.getLogger();
        try {
            logger.log("Input " + input);
            final DataSource datasource = new DataSource(System.getenv());
            LiquibaseRunner liquibaseRunner = new LiquibaseRunner(datasource);
            liquibaseRunner.run();

            return "OK";
        } catch (RuntimeException e) {
            return "Ups. BAD. " + e.getMessage();
        }
    }
}
