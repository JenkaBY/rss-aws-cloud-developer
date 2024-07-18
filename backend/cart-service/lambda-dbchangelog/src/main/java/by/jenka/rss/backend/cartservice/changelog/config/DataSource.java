package by.jenka.rss.backend.cartservice.changelog.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ToString
public class DataSource {
    private final String host;
    private final String dbName;
    private final int port;
    @Getter
    private final String rawPassword;
    @Getter
    private final String user;

    public static  DataSource localDataSource() {
        System.out.println("Load env variables");
        var dotenv = Dotenv.configure()
                .filename(".env.local-db-credentials")
                .load();
        var lambdaEnvMap = new HashMap<String, String>();
        dotenv.entries(Dotenv.Filter.DECLARED_IN_ENV_FILE).stream()
                .forEach(e -> lambdaEnvMap.put(e.getKey(), e.getValue()));
        return new DataSource(lambdaEnvMap);
    }

    public DataSource(Map<String, String> env) {
        this.host = Objects.requireNonNull(env.get("PG_URL"));
        this.user = Objects.requireNonNull(env.get("PG_USER"));
        this.rawPassword = Objects.requireNonNull(env.get("PG_PASSWORD"));
        this.dbName = Objects.requireNonNull(env.get("PG_DBNAME"));
        this.port = Objects.requireNonNull(Integer.valueOf(env.get("PG_DBPORT")));
    }

    public String getJdbcUrl() {
        return "jdbc:postgresql://%s:%s/%s".formatted(host, port, dbName);
    }
}
