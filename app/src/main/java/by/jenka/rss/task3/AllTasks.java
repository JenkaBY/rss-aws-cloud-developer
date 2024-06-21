package by.jenka.rss.task3;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;

import java.util.Map;

public class AllTasks {

    private static final String RSS_ALL_TASKS_STACK_NAME = "RssAwsCloudDeveloperCdkStack";

    public void run() {
        App app = new App(AppProps.builder()
                .context(Map.ofEntries(
                        Map.entry("school", "RSS")
                ))
                .build());

        new RssCloudDeveloperStack(app, RSS_ALL_TASKS_STACK_NAME)
//                Task 2
                .createS3BucketToHostFrontEnd()
                .createCloudFront()
                .grandPermission()
                .initFrontEndDeployment()
//                Task 3
                .createProductByIdLambda()
                .createProductListLambda()
                .createApiGateway();

        app.synth();
    }
}
