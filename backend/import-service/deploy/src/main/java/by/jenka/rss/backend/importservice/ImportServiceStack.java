package by.jenka.rss.backend.importservice;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.IResource;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.events.targets.ApiGateway;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.eventsources.S3EventSource;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.sqs.IQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.util.ArrayList;
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

    private Function importFileParserHandler;
    private Function importProductsFileHandler;
    private IFunction basicAuthorizerHandler;
    private Bucket importFilesBucket;
    private IQueue catalogItemsQueue;

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
                                .defaultCorsPreflightOptions(
                                        CorsOptions.builder()
                                                .allowOrigins(Cors.ALL_ORIGINS)
                                                .allowMethods(Cors.ALL_METHODS)
                                                .allowHeaders(Cors.DEFAULT_HEADERS)
                                                .build()
                                )
                                .build())
                .build();

        var root = api.getIRestApi().getRoot();
        var importResource = root.addResource("import");

        var apiGatewayServiceRole = Role.Builder.create(this, "ApiGatewayServiceRole")
                .assumedBy(new ServicePrincipal("apigateway.amazonaws.com"))
                .build();
        var basicAuthorizer = RequestAuthorizer.Builder.create(this, "basicAuthRequestAuthorizer")
                .authorizerName("BasicAuthRequestAuthorizer")
                .handler(basicAuthorizerHandler)
                .identitySources(
                        List.of(
                                IdentitySource.header("Authorization")
                        ))
                .resultsCacheTtl(Duration.seconds(0))
                .assumeRole(apiGatewayServiceRole)
                .build();

        importResource.addMethod("GET",
                LambdaIntegration.Builder
                        .create(importProductsFileHandler)
                        .timeout(TWENTY_SEC)
                        .build(),
                MethodOptions.builder()
                        .requestParameters(Map.of(
                                "method.request.querystring.name", true,
                                "method.request.header.Authorization", false
                        ))
                        .authorizer(basicAuthorizer)
                        .authorizationType(AuthorizationType.CUSTOM)
                        .methodResponses(
                                List.of(
                                        MethodResponse.builder()
                                                .statusCode("403")
                                                .responseModels(Map.of("application/json", Model.ERROR_MODEL))
                                                .responseParameters(getCorsResponseParams())
                                                .build(),
                                        MethodResponse.builder()
                                                .statusCode("401")
                                                .responseModels(Map.of("application/json", Model.ERROR_MODEL))
                                                .responseParameters(getCorsResponseParams())
                                                .build()
                                )
                        )
                        .build()
        );
//        This allows to handle properly CORS requests on a client's side. Otherwise, status on the client is 0
        var default4xxRss = GatewayResponse.Builder.create(this, "DEFAULT_4xx_RSS")
                .restApi(importResource.getApi())
                .type(ResponseType.DEFAULT_4_XX)
                .responseHeaders(
                        Map.of("Access-Control-Allow-Origin", "'*'")
                )
                .build();
        var default5xxRss = GatewayResponse.Builder.create(this, "DEFAULT_5xx_RSS")
                .restApi(importResource.getApi())
                .type(ResponseType.DEFAULT_5_XX)
                .responseHeaders(
                        Map.of("Access-Control-Allow-Origin", "'*'")
                )
                .build();
        System.out.println("Created import-files apiGateway");
        return this;
    }

    public ImportServiceStack grantPermissionsToQueueProcessing() {
        System.out.println("Grand permissions for publishing");

        catalogItemsQueue.grantSendMessages(importFileParserHandler);
        System.out.println("Permissions granted for publishing and consuming");
        return this;
    }

    public ImportServiceStack initCatalogItemsQueue() {
        System.out.println("Init external resources");
        var catalogItemsQueueTopicArn = Fn.importValue("CatalogItemsQueueTopicArn");
        catalogItemsQueue = Queue.fromQueueArn(this, "CatalogItemsQueueTopic", catalogItemsQueueTopicArn);
        lambdaEnvMap.put("CATALOG_ITEM_QUEUE_TOPIC_URL", catalogItemsQueue.getQueueUrl());

        var basicAuthorizerHandlerArn = Fn.importValue("BasicAuthFunctionArn");

        basicAuthorizerHandler = Function.fromFunctionArn(this, "BasicAuthFunctionArn", basicAuthorizerHandlerArn);
        System.out.println("Initialisation external resources completed");
        return this;
    }

    private void addCorsOptions(IResource item) {
        List<MethodResponse> methodResponses = new ArrayList<>();

        Map<String, Boolean> responseParameters = getCorsResponseParams();
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
        integrationResponseParameters.put("method.response.header.Access-Control-Allow-Credentials", "'true'");
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

    private static Map<String, Boolean> getCorsResponseParams() {
        Map<String, Boolean> responseParameters = new HashMap<>();
        responseParameters.put("method.response.header.Access-Control-Allow-Headers", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Methods", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Credentials", Boolean.TRUE);
        responseParameters.put("method.response.header.Access-Control-Allow-Origin", Boolean.TRUE);
        return responseParameters;
    }

}
