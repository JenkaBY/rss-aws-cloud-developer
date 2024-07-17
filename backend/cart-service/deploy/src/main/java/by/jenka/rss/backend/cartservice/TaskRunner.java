package by.jenka.rss.backend.cartservice;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;

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
        new CartServiceStack(app, CART_SERVICE_STACK_NAME)
                .createCartApiLambda()
                .createApiGateway();

        app.synth();
        System.out.println("---------------  End app");
    }
}
