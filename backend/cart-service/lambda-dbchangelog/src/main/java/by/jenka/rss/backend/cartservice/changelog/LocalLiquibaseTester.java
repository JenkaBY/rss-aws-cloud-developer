package by.jenka.rss.backend.cartservice.changelog;

import by.jenka.rss.backend.cartservice.changelog.config.DataSource;
import by.jenka.rss.backend.cartservice.changelog.service.LiquibaseRunner;

public class LocalLiquibaseTester {

    /**
     * Start postgres server from docker-compose before. Then just run this method.
     * You'll ensure that liquibase is run and creates tables in the local env.
     * @param args
     */
    public static void main(String[] args) {
        var datasource = DataSource.localDataSource();

        LiquibaseRunner liquibaseRunner = new LiquibaseRunner(datasource);
        liquibaseRunner.run();
    }
}
