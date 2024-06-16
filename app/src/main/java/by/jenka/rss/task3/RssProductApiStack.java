package by.jenka.rss.task3;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.events.targets.ApiGateway;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;


public class RssProductApiStack extends Stack {

    private static final String AUTOMATED_GET_PRODUCTS = "RSS-automatedGetProducts";
    private static final String AUTOMATED_GET_PRODUCT_BY_ID = "RSS-automatedGetProductById";
    private static final String RSS_AUTOMATED_PRODUCT_API_ID = "rssAutomatedProductApiId";
    private static final String RSS_AUTOMATED_PRODUCT_API_NAME = "RSS-cloud-product-automated-api";

    private static final AssetCode LAMBDA_JAR = Code.fromAsset("../lambda/build/libs/lambda-all.jar");

    private Function getProductsHandler;
    private Function getProductByIdHandler;

    public RssProductApiStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);
    }

    public RssProductApiStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public RssProductApiStack createProductListLambda() {
        System.out.println("Create GetProductList lambda");
        getProductsHandler = Function.Builder.create(this, AUTOMATED_GET_PRODUCTS)
                .description("Created via java cdk")
                .functionName("getProductListAutomated")
                .code(LAMBDA_JAR)
                .handler("by.jenka.rss.lambda.handler.GetProductsHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(256) // Java loves memory
                .timeout(Duration.seconds(5)) // Class loading can take some time
                .build();
        System.out.println("Created GetProductList lambda");
        return this;
    }

    public RssProductApiStack createProductByIdLambda() {
        System.out.println("Create GetProductByIdHandler lambda");
        getProductByIdHandler = Function.Builder.create(this, AUTOMATED_GET_PRODUCT_BY_ID)
                .description("Created via java cdk")
                .functionName("getProductByIdAutomated")
                .code(LAMBDA_JAR)
                .handler("by.jenka.rss.lambda.handler.GetProductByIdHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(256) // Java loves memory
                .timeout(Duration.seconds(5)) // Class loading can take some time
                .build();
        System.out.println("Created GetProductByIdHandler lambda");
        return this;
    }

    public RssProductApiStack createApiGateway() {
        System.out.println("Create createApiGateway");
        var api = ApiGateway.Builder.create(
                        RestApi.Builder
                                .create(this, RSS_AUTOMATED_PRODUCT_API_ID)
                                .description("Created by java cdk. It's a product API for Task 3")
                                .restApiName(RSS_AUTOMATED_PRODUCT_API_NAME)
                                .build())

                .build();

        var root = api.getIRestApi().getRoot();
        var products = root.addResource("products");
        // get all products
        products.addMethod("GET",
                LambdaIntegration.Builder
                        .create(getProductsHandler)
                        .timeout(Duration.seconds(5))
                        .build()
        );
        // get product by id
        products.addResource("{productId}")
                .addMethod("GET",
                        LambdaIntegration.Builder
                                .create(getProductByIdHandler)
                                .timeout(Duration.seconds(5))
                                .build()
                );
        CfnOutput.Builder.create(this, "URL-" + RSS_AUTOMATED_PRODUCT_API_NAME).value(root.getPath() + "products").build();
        System.out.println("Created createApiGateway");
        return this;
    }
}
