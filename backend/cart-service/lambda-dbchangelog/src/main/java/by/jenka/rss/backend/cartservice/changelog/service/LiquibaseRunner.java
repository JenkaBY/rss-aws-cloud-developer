package by.jenka.rss.backend.cartservice.changelog.service;

import by.jenka.rss.backend.cartservice.changelog.config.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@RequiredArgsConstructor
public class LiquibaseRunner {
    private static final String CHANGELOG_PATH = "./changelog/changelog-master.xml";

    private final DataSource dataSource;

    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            throw new RuntimeException("Error running Liquibase changelog", e);
        }
    }

    private void doRun() throws Exception {
        try (Liquibase liquibase = getLiquibase()) {
//            TODO replace deprecated update with a new approach
//            https://forum.liquibase.org/t/4-21-1-is-out-deprecates-liquibase-update/8087/5
            liquibase.update(new Contexts(), new LabelExpression());

        }
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                dataSource.getJdbcUrl(),
                dataSource.getUser(),
                dataSource.getRawPassword());
    }

    private Database getDatabase() throws SQLException, DatabaseException {
        Connection connection = getConnection();
        connection.prepareStatement(String.format("SET search_path TO '%s'", "public")).execute();

        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        database.setDefaultSchemaName("public");

        return database;
    }

    private Liquibase getLiquibase() throws DatabaseException, SQLException {
        // If the changelog is not on the classpath, use a liquibase.resource.FileSystemResourceAccessor or other appropriate accessor
        return new Liquibase(CHANGELOG_PATH, new ClassLoaderResourceAccessor(), getDatabase());
    }
}
