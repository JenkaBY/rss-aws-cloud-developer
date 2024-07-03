package by.jenka.rss.backend.productservice;

import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;

import java.util.Map;

public class TaskRunner {
    private static final String PRODUCT_SERVICE_STACK_NAME = "RssAwsCloudDeveloperProductServiceStack";

    public static void main(String[] args) {
        System.out.println("-------------   Start product app stack");
        App app = new App(AppProps.builder()
                .context(Map.ofEntries(
                        Map.entry("school", "RSS")
                ))
                .build());
        new ProductServiceStack(app, PRODUCT_SERVICE_STACK_NAME)
//                Task 2
                .createS3BucketToHostFrontEnd()
                .createCloudFront()
                .grantReadPermissionFromCloudFront()
                .initFrontEndDeployment()
//                Task 4
                .createProductTable()
                .createStockTable()
                .createProductsListLambda()
                .createGetProductByIdLambda()
                .createPostProductLambda()
                .grantFullAccessToDbForLambdas()
                .createApiGateway();

        app.synth();
        System.out.println("---------------  End app");
    }
}
