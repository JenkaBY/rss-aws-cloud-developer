package by.jenka.rss.backend.cartservice;

import by.jenka.rss.backend.cartservice.config.AwsConfig;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Map;

public class TaskRunner {
    private static final String CART_SERVICE_STACK_NAME = "RssAwsCloudDeveloperCartServiceStack";

    public static void main(String[] args) {

        System.out.println("-------------   Start product app stack");
        App app = new App(AppProps.builder()
                .context(Map.ofEntries(
                        Map.entry("school", "RSS")
                ))
                .build());
        var props = StackProps.builder()
                .env(Environment.builder()
                        .account(AwsConfig.DEFAULT_ACCOUNT)
                        .region(AwsConfig.DEFAULT_REGION)
                        .build())
                .build();
        new CartServiceStack(app, CART_SERVICE_STACK_NAME, props)
                .loadEnvVariables()
                .initVpcAndSecurityGroups()
                .initPostgresDb()
                .createAndTriggerDbChangelogLambda()
                .createCartApiLambda()
                .createApiGateway()
                .grantPermissions()
                .outputVariables();

        app.synth();
        System.out.println("---------------  End app");
    }
}
