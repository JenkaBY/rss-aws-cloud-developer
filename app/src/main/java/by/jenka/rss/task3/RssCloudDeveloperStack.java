package by.jenka.rss.task3;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.cloudfront.BehaviorOptions;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.OriginAccessIdentity;
import software.amazon.awscdk.services.cloudfront.origins.S3Origin;
import software.amazon.awscdk.services.cloudfront.origins.S3OriginProps;
import software.amazon.awscdk.services.events.targets.ApiGateway;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;


public class RssCloudDeveloperStack extends Stack {

    private static final String INDEX_HTML = "index.html";
    private static final String BUCKET_NAME = "rss-aws-cloud-developer-fe-hosting";
    private static final String WEB_APP_RESOURCES = "./build/resources/main/static";
    private static final String AUTOMATED_GET_PRODUCTS = "RSS-automatedGetProducts";
    private static final String AUTOMATED_GET_PRODUCT_BY_ID = "RSS-automatedGetProductById";
    private static final String RSS_AUTOMATED_PRODUCT_API_ID = "rssAutomatedProductApiId";
    private static final String RSS_AUTOMATED_PRODUCT_API_NAME = "RSS-cloud-product-automated-api";
    private static final String AWS_CLOUDFRONT_URL = "cloudfront.amazonaws.com";
    private static final String CLOUDFRONT_DISTRIBUTION_PATTERN = "arn:aws:cloudfront::%s:distribution/%s";

    private static final AssetCode LAMBDA_JAR = Code.fromAsset("../lambda/build/libs/lambda-all.jar");
    private static final String OAI_ID = "OAI-for-FE-hosting-in-S3";

    private Function getProductsHandler;
    private Function getProductByIdHandler;
    private Bucket feS3Hosting;
    private OriginAccessIdentity oai;
    private Distribution feDistribution;

    public RssCloudDeveloperStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);
    }

    public RssCloudDeveloperStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public RssCloudDeveloperStack createS3BucketToHostFrontEnd() {
        System.out.println("Create S3 Bucket for hosting");
        feS3Hosting = Bucket.Builder.create(this, BUCKET_NAME)
                .bucketName(BUCKET_NAME)
                .removalPolicy(RemovalPolicy.DESTROY)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .autoDeleteObjects(true)
                .build();
        oai = OriginAccessIdentity.Builder.create(this, OAI_ID)
                .comment("Created from CDK and belongs to stack")
                .build();
        return this;
    }

    public RssCloudDeveloperStack createCloudFront() {
        System.out.println("Create CloudFront");
        final var s3OriginProps = S3OriginProps.builder()
                .originAccessIdentity(oai)
                .build();
        final var s3Origin = new S3Origin(feS3Hosting, s3OriginProps);
        final var behaviourOptions = BehaviorOptions.builder()
                .origin(s3Origin)
                .build();
        feDistribution = Distribution.Builder.create(this, "RSS-FE-angular-distribution")
                .comment("Created from java CDK and belongs to stack")
                .defaultBehavior(behaviourOptions)
                .defaultRootObject(INDEX_HTML)
                .build();
        return this;
    }

    public RssCloudDeveloperStack grandPermission() {
        System.out.println("Grand permission to CloudFront at S3 Hosting");
        var actions = List.of("s3:GetObject");

        var resources = List.of(
                feS3Hosting.arnForObjects("*")
        );

        var principal = ServicePrincipal.Builder
                .create(AWS_CLOUDFRONT_URL)
                .build();
        var principals = List.of(principal);

        var sourceArnValue = CLOUDFRONT_DISTRIBUTION_PATTERN.formatted(getAccount(), feDistribution.getDistributionId());
        var sourceArnMap = Map.of("AWS:SourceArn", sourceArnValue);
        var conditions = Map.of("StringEquals", sourceArnMap);

        var policyPermissions = PolicyStatement.Builder.create()
                .actions(actions)
                .resources(resources)
                .principals((principals))
                .conditions(conditions)
                .build();

        feS3Hosting.addToResourcePolicy(policyPermissions);
        feS3Hosting.grantRead(oai);
        return this;
    }

    public RssCloudDeveloperStack initFrontEndDeployment() {
        System.out.println("Deploy FE to S3");
        final var source = Source.asset(WEB_APP_RESOURCES);
        final var sources = List.of(source);
        final var distributionPath = List.of("/*");

        BucketDeployment.Builder.create(this, "RSS_BUCKET_DEPLOYMENT")
                .sources(sources)
                .destinationBucket(feS3Hosting)
                .distribution(feDistribution)
                .distributionPaths(distributionPath)
                .build();
        return this;
    }

    public RssCloudDeveloperStack createProductListLambda() {
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

    public RssCloudDeveloperStack createProductByIdLambda() {
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

    public RssCloudDeveloperStack createApiGateway() {
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
