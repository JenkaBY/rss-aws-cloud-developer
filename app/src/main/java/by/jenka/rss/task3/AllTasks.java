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
                .grantReadPermissionFromCloudFront()
                .initFrontEndDeployment()
//                Task 4
                .createProductTable()
                .createStockTable()
//                Task 3
                .createProductByIdLambda()
                .createProductListLambda()
//                  Task 4
                .createProductListDynamoDbLambda()
                .createProductByIdDynamoDbLambda()
                .createPostProductDynamoDbLambda()
//                Grant permission to DB for Functions
                .grantFullAccessToDbForFunctions()
//                  Task 3
                .createApiGateway();
        app.synth();
    }
}
