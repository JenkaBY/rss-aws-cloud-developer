package by.jenka.rss.backend.productservice;

import by.jenka.rss.backend.productservice.config.AwsConfig;
import by.jenka.rss.backend.productservice.sdk.task2.Utils;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.cloudfront.BehaviorOptions;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.OriginAccessIdentity;
import software.amazon.awscdk.services.cloudfront.origins.S3Origin;
import software.amazon.awscdk.services.cloudfront.origins.S3OriginProps;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.events.targets.ApiGateway;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.amazon.awscdk.services.sns.NumericConditions;
import software.amazon.awscdk.services.sns.SubscriptionFilter;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductServiceStack extends Stack {

    private static final String INDEX_HTML = "index.html";
    private static final String FE_APP_BUCKET_NAME = "rss-aws-frontend-app";
    private static final String WEB_APP_RESOURCES = "./build/resources/main/static";
    private static final String GET_ALL_PRODUCTS = "GetProducts";
    private static final String GET_PRODUCT_BY_ID = "GetProductsById";
    private static final String POST_PRODUCTS = "PostProducts";

    private static final String RSS_PRODUCT_GATEWAY_API = "RSS-cloud-product-automated-api";
    private static final String AWS_CLOUDFRONT_URL = "cloudfront.amazonaws.com";
    private static final String CLOUDFRONT_DISTRIBUTION_PATTERN = "arn:aws:cloudfront::%s:distribution/%s";

    private static final AssetCode LAMBDA_JAR = Code.fromAsset("../lambda/build/libs/lambda-products-all.jar");
    private static final String OAI_ID = "OAI-for-FE-hosting-in-S3";
    private static final Map<String, String> lambdaEnvMap = new HashMap<>(Map.of("ENV", "PROD"));
    private static final Duration TWENTY_SEC = Duration.seconds(20);
    private static final String CREATE_PRODUCT_TOPIC_ARN = "CREATE_PRODUCT_TOPIC_ARN";

    private Function getProductsHandler;
    private Function getProductByIdHandler;
    private Function postProductHandler;
    private Function catalogBatchProcessHandler;

    private Bucket feS3Hosting;
    private OriginAccessIdentity oai;
    private Distribution feDistribution;
    private Table productTable;
    private Table stockTable;

    private Queue catalogItemsQueue;
    private Topic createProductTopic;

    public ProductServiceStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);
    }

    public ProductServiceStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public ProductServiceStack createS3BucketToHostFrontEnd() {
        System.out.println("Create S3 Bucket for hosting");
        feS3Hosting = Bucket.Builder.create(this, FE_APP_BUCKET_NAME)
                .bucketName(FE_APP_BUCKET_NAME)
                .removalPolicy(RemovalPolicy.DESTROY)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .autoDeleteObjects(true)
                .build();
        oai = OriginAccessIdentity.Builder.create(this, OAI_ID)
                .comment("Created via Java CDK, It belongs to Product stack")
                .build();
        return this;
    }

    public ProductServiceStack createCloudFront() {
        System.out.println("Create CloudFront");
        final var s3OriginProps = S3OriginProps.builder()
                .originAccessIdentity(oai)
                .build();
        final var s3Origin = new S3Origin(feS3Hosting, s3OriginProps);
        final var behaviourOptions = BehaviorOptions.builder()
                .origin(s3Origin)
                .build();
        feDistribution = Distribution.Builder.create(this, "RSS-FE-product-distribution")
                .comment("Created from java CDK and belongs to stack")
                .defaultBehavior(behaviourOptions)
                .defaultRootObject(INDEX_HTML)
                .build();
        return this;
    }

    public ProductServiceStack grantReadPermissionFromCloudFront() {
        System.out.println("Grant permission to CloudFront at S3 Hosting");
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

    public ProductServiceStack initFrontEndDeployment() {
        System.out.println("Deploy FE to S3");
        final var source = Source.asset(WEB_APP_RESOURCES);
        final var sources = List.of(source);
        final var distributionPath = List.of("/*");

        BucketDeployment.Builder.create(this, "RssFeProductServiceBucketDeployment")
                .sources(sources)
                .destinationBucket(feS3Hosting)
                .distribution(feDistribution)
                .distributionPaths(distributionPath)
                .build();
        return this;
    }

    public ProductServiceStack createProductApiGateway() {
        System.out.println("Create Product Api Gateway");
        var api = ApiGateway.Builder.create(
                        RestApi.Builder
                                .create(this, "RssProductGatewayApi")
                                .description("Created by java cdk. It's a product API for Task 3")
                                .restApiName(RSS_PRODUCT_GATEWAY_API)
                                .build())

                .build();

        var root = api.getIRestApi().getRoot();
        var products = root.addResource("products");
        // get all products
        products.addMethod("GET",
                LambdaIntegration.Builder
                        .create(getProductsHandler)
                        .timeout(TWENTY_SEC)
                        .build()
        );
        products.addMethod("POST",
                LambdaIntegration.Builder
                        .create(postProductHandler)
                        .timeout(TWENTY_SEC)
                        .build()
        );

        var productById = products.addResource("{productId}");
        productById.addMethod("GET",
                LambdaIntegration.Builder
                        .create(getProductByIdHandler)
                        .timeout(TWENTY_SEC)
                        .build()
        );

        addCorsOptions(products);
        addCorsOptions(productById);

        CfnOutput.Builder.create(this, "product-ui").value(feDistribution.getDistributionDomainName()).build();
        System.out.println("Created Product Api Gateway");
        return this;
    }

    public ProductServiceStack createProductsListLambda() {
        System.out.println("Create GetProductsFromDbHandler lambda from DynamoDB");
        getProductsHandler = Function.Builder.create(this, GET_ALL_PRODUCTS)
                .description("Created via java cdk")
                .functionName("getProductListFromDbAutomated")
                .code(LAMBDA_JAR)
                .handler("by.jenka.rss.productservice.lambda.handler.GetProductsHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(512) // Java loves memory
                .timeout(TWENTY_SEC) // Class loading can take some time
                .environment(lambdaEnvMap)
                .build();
        System.out.println("Created GetProductsFromDbHandler lambda");
        return this;
    }

    public ProductServiceStack createGetProductByIdLambda() {
        System.out.println("Create GetProductByIdFromDbHandler lambda from DynamoDB");
        getProductByIdHandler = Function.Builder.create(this, GET_PRODUCT_BY_ID)
                .description("Created via java cdk")
                .functionName("getProductByIdFromDbHandler")
                .code(LAMBDA_JAR)
                .handler("by.jenka.rss.productservice.lambda.handler.GetProductByIdHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(512)
                .timeout(TWENTY_SEC)
                .environment(lambdaEnvMap)
                .build();
        System.out.println("Created GetProductsFromDbHandler lambda");
        return this;
    }

    public ProductServiceStack createPostProductLambda() {
        System.out.println("Create CreateProduct lambda from DynamoDB");
        postProductHandler = Function.Builder.create(this, POST_PRODUCTS)
                .description("Created via java cdk")
                .functionName("postProducts")
                .code(LAMBDA_JAR)
                .handler("by.jenka.rss.productservice.lambda.handler.PostProductHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(512)
                .timeout(TWENTY_SEC)
                .environment(lambdaEnvMap)
                .build();
        System.out.println("Created PostProductFromDbHandler lambda");
        return this;
    }

    public ProductServiceStack createCatalogBatchProcessLambda() {
        System.out.println("Create CatalogBatchProcess lambda");
        catalogBatchProcessHandler = Function.Builder.create(this, "CatalogBatchProcessor")
                .description("Created via java cdk. Task 6")
                .functionName("catalogBatchProcess")
                .code(LAMBDA_JAR)
                .handler("by.jenka.rss.productservice.lambda.handler.CatalogBatchProcessHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(512)
                .timeout(TWENTY_SEC)
                .environment(lambdaEnvMap)
                .events(
                        List.of(SqsEventSource.Builder.create(catalogItemsQueue)
                                .batchSize(5)
                                .enabled(true)
                                .maxBatchingWindow(Duration.seconds(5))
                                .build()
                        )
                )
                .build();
        System.out.println("Created CatalogBatchProcess lambda");
        return this;
    }

    public ProductServiceStack createProductTable() {
        TableProps tableProps;
        Attribute partitionKey = Attribute.builder()
                .name("id")
                .type(AttributeType.STRING)
                .build();
        var tableName = "products";
        tableProps = TableProps.builder()
                .tableName(tableName)
                .partitionKey(partitionKey)
                .removalPolicy(RemovalPolicy.DESTROY)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
        productTable = new Table(this, tableName, tableProps);
        lambdaEnvMap.put("PRODUCT_TABLE", productTable.getTableName());
        return this;
    }

    public ProductServiceStack createStockTable() {
        TableProps tableProps;
        Attribute partitionKey = Attribute.builder()
                .name("productId")
                .type(AttributeType.STRING)
                .build();
        var tableName = "stocks";
        tableProps = TableProps.builder()
                .tableName(tableName)
                .partitionKey(partitionKey)
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
        stockTable = new Table(this, tableName, tableProps);
        lambdaEnvMap.put("STOCK_TABLE", stockTable.getTableName());
        return this;
    }

    public ProductServiceStack grantFullAccessToDbForLambdas() {
        List<Function> allDbFunctions = List.of(getProductByIdHandler, getProductsHandler, postProductHandler, catalogBatchProcessHandler);
        allDbFunctions.forEach(f -> {
                    productTable.grantFullAccess(f);
                    stockTable.grantFullAccess(f);
                }
        );
        return this;
    }

    public ProductServiceStack createCatalogItemsSqs() {
        System.out.println("Create Catalog Items SQS");
        catalogItemsQueue = Queue.Builder.create(this, "RssCatalogItemsQueue")
                .queueName("catalogItemsQueue")
                .removalPolicy(RemovalPolicy.DESTROY)
                .visibilityTimeout(Duration.seconds(1))
                .build();
        System.out.println("Created Catalog Items SQS");
        return this;
    }

    public ProductServiceStack createProductTopic() {
        System.out.println("Create createProductTopic SNS");
        createProductTopic = Topic.Builder.create(this, "RssCreateProductTopic")
                .displayName("RssCreateProductTopic")
                .topicName("createProductTopic")
                .build();

//        notify all
        Utils.convertToEmailSubscriptions(AwsConfig.getCreateProductNotificationEmails()).forEach(
                createProductTopic::addSubscription
        );

//        With FilterPolicy
        var emailsForFilter = AwsConfig.getCreateSpecialProductNotificationEmails();
        var priceGreaterThan100 = Map.of(
                "price", SubscriptionFilter.numericFilter(
                        NumericConditions.builder()
                                .greaterThan(100)
                                .build()
                )
        );
        Utils.convertToEmailSubscriptions(emailsForFilter, priceGreaterThan100)
                .forEach(createProductTopic::addSubscription);
        lambdaEnvMap.put(CREATE_PRODUCT_TOPIC_ARN, createProductTopic.getTopicArn());
        System.out.println("Created createProductTopic SNS");
        return this;
    }

    public ProductServiceStack grantPermissionsToMessagingProcessing() {
        System.out.println("Grand permissions for publishing and consuming");
        catalogItemsQueue.grantConsumeMessages(catalogBatchProcessHandler);

        createProductTopic.grantPublish(catalogBatchProcessHandler);
        System.out.println("Permissions granted for publishing and consuming");
        return this;
    }

    public ProductServiceStack outputStackVariables() {
        CfnOutput.Builder.create(this, "CatalogItemsQueueTopicArn")
                .exportName("CatalogItemsQueueTopicArn")
                .value(catalogItemsQueue.getQueueArn())
                .build();
        return this;
    }

    private void addCorsOptions(software.amazon.awscdk.services.apigateway.IResource item) {
        List<MethodResponse> methodResponses = new ArrayList<>();

        Map<String, Boolean> responseParameters = new HashMap<>();
        responseParameters.put("method.response.header.Access-Control-Allow-Headers", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Methods", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Credentials", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Origin", Boolean.TRUE);
        methodResponses.add(MethodResponse.builder()
                .responseParameters(responseParameters)
                .statusCode("200")
                .build());
        MethodOptions methodOptions = MethodOptions.builder()
                .methodResponses(methodResponses)
                .build();

        Map<String, String> requestTemplate = new HashMap<>();
        requestTemplate.put("application/json", "{\"statusCode\": 200}");
        List<IntegrationResponse> integrationResponses = new ArrayList<>();

        Map<String, String> integrationResponseParameters = new HashMap<>();
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,X-Amz-User-Agent'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Origin", "'*'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Credentials", "'false'");
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Methods", "'OPTIONS,GET,PUT,POST,DELETE'");
        integrationResponses.add(IntegrationResponse.builder()
                .responseParameters(integrationResponseParameters)
                .statusCode("200")
                .build());
        Integration methodIntegration = MockIntegration.Builder.create()
                .integrationResponses(integrationResponses)
                .passthroughBehavior(PassthroughBehavior.NEVER)
                .requestTemplates(requestTemplate)
                .build();

        item.addMethod("OPTIONS", methodIntegration, methodOptions);
    }

}
