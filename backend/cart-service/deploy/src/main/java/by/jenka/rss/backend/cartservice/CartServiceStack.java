package by.jenka.rss.backend.cartservice;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.events.targets.ApiGateway;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartServiceStack extends Stack {

    public static final @NotNull Duration TWENTY_SECONDS = Duration.seconds(20);
    private Function cartApiHandler;
    private static final Map<String, String> lambdaEnvMap = new HashMap<>(Map.of("ENV", "PROD"));


    public CartServiceStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);
    }

    public CartServiceStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public CartServiceStack createCartApiLambda() {
        System.out.println("Creating Cart Service Api lambda");

//        var nodeModules = LayerVersion.Builder.create(this, "CartServiceNodeModules")
//                .removalPolicy(RemovalPolicy.DESTROY)
//                .code(AssetCode.fromAsset("../lambda/node_modules"))
//                .compatibleRuntimes(List.of(Runtime.NODEJS_20_X))
//                .build();

        cartApiHandler = Function.Builder.create(this, "CartServiceApi")
                .description("Created via java cdk. Task 8")
                .functionName("cartServiceApi")
                .code(AssetCode.fromAsset("../lambda/dist"))
                .handler("main.handler")
                .runtime(Runtime.NODEJS_20_X)
                .memorySize(256)
                .timeout(TWENTY_SECONDS)
                .environment(lambdaEnvMap)
//                .layers(List.of(nodeModules))
                .build();
        return this;
    }

    public CartServiceStack createApiGateway() {
        System.out.println("Create Cart Service Api Gateway");
        var api = ApiGateway.Builder.create(
                        RestApi.Builder
                                .create(this, "RssCartServiceGatewayApi")
                                .description("Created by java cdk. It's a Cart Service API for Task 8")
                                .restApiName("RssCartServiceApi")
                                .build())

                .build();

        var root = api.getIRestApi().getRoot();

        root.addMethod("ANY",
                LambdaIntegration.Builder
                        .create(cartApiHandler)
                        .timeout(TWENTY_SECONDS)
                        .build()
        );

        var other = root.addResource("{proxy+}");
        other.addMethod("ANY",
                LambdaIntegration.Builder
                        .create(cartApiHandler)
                        .timeout(TWENTY_SECONDS)
                        .build()
        );

        addCorsOptions(root);
        addCorsOptions(other);

        System.out.println("Created Cart Service Api Gateway");
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
