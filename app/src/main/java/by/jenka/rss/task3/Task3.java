package by.jenka.rss.task3;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;

import java.util.Map;

public class Task3 {

    private static final String RSS_TASK3_STACK_NAME = "RssAwsTask3CdkStack";

    public void run() {
        App app = new App(AppProps.builder()
                .context(Map.ofEntries(
                        Map.entry("school", "RSS"),
                        Map.entry("task-id", "task-3")
                ))
                .build());
        new RssProductApiStack(app, RSS_TASK3_STACK_NAME)
                .createProductByIdLambda()
                .createProductListLambda()
                .createApiGateway();
        app.synth();
    }
}
