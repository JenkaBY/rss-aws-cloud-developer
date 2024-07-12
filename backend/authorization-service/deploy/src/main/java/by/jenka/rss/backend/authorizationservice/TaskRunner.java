package by.jenka.rss.backend.authorizationservice;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;

import java.util.Map;

public class TaskRunner {

    private static final String RSS_ALL_TASKS_STACK_NAME = "RssAwsCloudDeveloperAuthorizationServiceStack";

    public static void main(String[] args) {
        System.out.println("-------------   Start authorization service app stack");
        App app = new App(AppProps.builder()
                .context(Map.ofEntries(
                        Map.entry("school", "RSS")
                ))
                .build());
        new AuthorizationServiceStack(app, RSS_ALL_TASKS_STACK_NAME)
                .loadEnvVariables()
                .createBasicAuthLambda()
                .outputStackVariables();

        app.synth();
        System.out.println("---------------  End app");
    }
}
