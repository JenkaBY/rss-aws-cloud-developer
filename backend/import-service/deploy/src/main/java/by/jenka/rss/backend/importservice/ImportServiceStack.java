package by.jenka.rss.backend.importservice;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.Deployment;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.events.targets.ApiGateway;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.S3EventSource;
import software.amazon.awscdk.services.s3.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ImportServiceStack extends Stack {

    private static final String BUCKET_NAME = "rss-aws-cloud-developer-import";
    private static final AssetCode LAMBDA_IMPORT_JAR = Code.fromAsset("../lambda/build/libs/lambda-import-all.jar");
    private static final Map<String, String> lambdaEnvMap = new HashMap<>(Map.of("ENV", "PROD"));
    private static final Duration TWENTY_SEC = Duration.seconds(20);
//    env variables
    private static final String UPLOADED_FOLDER_NAME = "uploaded";
    private static final String PARSED_FOLDER_NAME = "parsed";
    private static final int BATCH_SIZE = 5;
    private static final String CATALOG_ITEM_QUEUE_TOPIC_NAME = "catalogItemsQueue";

    private Function importFileParserHandler;
    private Function importProductsFileHandler;
    private Bucket importFilesBucket;

    public ImportServiceStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);
    }

    public ImportServiceStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public ImportServiceStack createS3BucketForImportFiles() {
        System.out.println("Create S3 Bucket for imported files");
        importFilesBucket = Bucket.Builder.create(this, BUCKET_NAME)
                .bucketName(BUCKET_NAME)
                .removalPolicy(RemovalPolicy.DESTROY)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .autoDeleteObjects(true)
                .cors(List.of(
                        CorsRule.builder()
                                .id("AllowAllHeadersAndMethods")
                                .allowedOrigins(List.of("*"))
                                .allowedHeaders(List.of("*"))
                                .allowedMethods(List.of(
                                        HttpMethods.GET,
                                        HttpMethods.HEAD,
                                        HttpMethods.POST,
                                        HttpMethods.PUT,
                                        HttpMethods.DELETE
                                ))
                                .exposedHeaders(List.of(
                                        "Access-Control-Allow-Origin",
                                        "Access-Control-Allow-Methods",
                                        "Content-Type",
                                        "Accept"
                                ))
                                .build()
                ))
                .build();
        lambdaEnvMap.put("IMPORT_BUCKET_NAME", importFilesBucket.getBucketName());
        lambdaEnvMap.put("FOLDER_FOR_UPLOAD", UPLOADED_FOLDER_NAME);
        lambdaEnvMap.put("FOLDER_FOR_PARSED", PARSED_FOLDER_NAME);
        lambdaEnvMap.put("CATALOG_ITEM_QUEUE", CATALOG_ITEM_QUEUE_TOPIC_NAME);
        lambdaEnvMap.put("BATCH_SIZE", String.valueOf(BATCH_SIZE));
        return this;
    }

    public ImportServiceStack grantRWPermissionsToS3() {
        System.out.println("Grant permissions");

        var allowFullAccessInTheBucket = PolicyStatement.Builder.create()
                .sid("AllowPutForImportProductLambda")
                .effect(Effect.ALLOW)
                .actions(List.of("s3:PutObject*"))
                .resources(List.of(importFilesBucket.arnForObjects(UPLOADED_FOLDER_NAME + "/*")))
                .principals(List.of(importProductsFileHandler.getGrantPrincipal()))
                .build();
        importFilesBucket.addToResourcePolicy(allowFullAccessInTheBucket);
        importFilesBucket.grantPut(importProductsFileHandler);

        importFilesBucket.addToResourcePolicy(PolicyStatement.Builder.create()
                .sid("AllowPutGetDeleteForFileParserLambda")
                .effect(Effect.ALLOW)
                .actions(List.of(
                        "s3:GetObject*",
                        "s3:DeleteObject*",
                        "s3:PutObject*"
                ))
                .resources(List.of(importFilesBucket.arnForObjects("*")))
                .principals(List.of(importFileParserHandler.getGrantPrincipal()))
                .build());

        importFilesBucket.grantRead(importFileParserHandler);
        importFilesBucket.grantPut(importFileParserHandler, PARSED_FOLDER_NAME + "/*");
        importFilesBucket.grantDelete(importFileParserHandler, UPLOADED_FOLDER_NAME + "/*");
        return this;
    }

    public ImportServiceStack createImportProductsFileHandlerLambda() {
        System.out.println("Create ImportProductsFileHandler lambda");
        importProductsFileHandler = Function.Builder.create(this, "ImportProductsFileHandler")
                .description("Created via java cdk. Task 5")
                .functionName("importProductsFileHandler")
                .code(LAMBDA_IMPORT_JAR)
                .handler("by.jenka.rss.importservice.lambda.handler.ImportProductsFileHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(256)
                .timeout(TWENTY_SEC)
                .environment(lambdaEnvMap)
                .build();
        System.out.println("Created ImportProductsFileHandler lambda");
        return this;
    }

    public ImportServiceStack createImportFileParserHandlerLambda() {
        System.out.println("Create ImportFileParserHandler lambda");

        importFileParserHandler = Function.Builder.create(this, "ImportFileParserHandler")
                .description("Created via java cdk. Task 5")
                .functionName("importFileParserHandler")
                .code(LAMBDA_IMPORT_JAR)
                .handler("by.jenka.rss.importservice.lambda.handler.ImportFileParserHandler")
                .events(List.of(
                        S3EventSource.Builder
                                .create(importFilesBucket)
                                .events(List.of(EventType.OBJECT_CREATED))
                                .filters(List.of(
                                        NotificationKeyFilter.builder()
                                                .prefix(UPLOADED_FOLDER_NAME)
                                                .build()
                                ))
                                .build()
                ))
                .runtime(Runtime.JAVA_17)
                .memorySize(512) // Java loves memory
                .timeout(TWENTY_SEC) // Class loading can take some time
                .environment(lambdaEnvMap)
                .build();
        System.out.println("Created ImportFileParserHandler lambda");
        return this;
    }

    public ImportServiceStack createImportFilesApiGateway() {
        System.out.println("Create import Files ApiGateway");
        var api = ApiGateway.Builder.create(
                        RestApi.Builder
                                .create(this, "ImportFilesApiGateway")
                                .description("Created by java cdk. It's a import files API (Task 5)")
                                .restApiName("RSS-import-files-api-gateway")
                                .cloudWatchRole(true)
                                .cloudWatchRoleRemovalPolicy(RemovalPolicy.DESTROY)
                                .build())
                .build();

        var root = api.getIRestApi().getRoot();
        var products = root.addResource("import");

        products.addMethod("GET",
                LambdaIntegration.Builder
                        .create(importProductsFileHandler)
                        .timeout(TWENTY_SEC)
                        .build()
//                TODO enable when issue with cdk is fixed
//                ,MethodOptions.builder()
//                        .requestParameters(Map.of("method.request.querystring.name", true))
//                        .build()
        );

        var importFileDeployment = Deployment.Builder.create(this, "RSS-import-file-api-deployment")
                .api(api.getIRestApi())
                .description("Created from Java CDK for RSS-import-files-api")
                .build();
//
//        var prodStage = Stage.Builder.create(this, "RSS-import-file-api-DEV-stage")
//                .stageName("dev")
//                .description("Created from Java CDK for import-file-api-DEV-stage")
//                .deployment(importFileDeployment)
//                .build();

        System.out.println("Created import-files apiGateway");
        return this;
    }
}
