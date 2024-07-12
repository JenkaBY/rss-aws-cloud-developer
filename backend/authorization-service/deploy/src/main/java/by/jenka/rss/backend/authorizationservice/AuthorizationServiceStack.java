package by.jenka.rss.backend.authorizationservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class AuthorizationServiceStack extends Stack {
    private static final AssetCode LAMBDA_AUTHORIZATION_JAR = Code.fromAsset("../lambda/build/libs/lambda-authorization-all.jar");
    private static final Map<String, String> lambdaEnvMap = new HashMap<>(Map.of("ENV", "PROD"));
    private static final Duration TWENTY_SEC = Duration.seconds(20);

    private Function basicAuthorizerHandler;

    public AuthorizationServiceStack(@Nullable Construct scope, @Nullable String id, @Nullable StackProps props) {
        super(scope, id, props);
    }

    public AuthorizationServiceStack(Construct scope, String id) {
        this(scope, id, null);
    }

    public AuthorizationServiceStack createBasicAuthLambda() {
        System.out.println("Create BasicAuthorization lambda");

        basicAuthorizerHandler = Function.Builder.create(this, "BasicAuthorizerHandler")
                .description("Created via java cdk. Task 7")
                .functionName("basicAuthorizerHandler")
                .code(LAMBDA_AUTHORIZATION_JAR)
                .handler("by.jenka.rss.authorizationservice.lambda.handler.BasicAuthorizerHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(256) // Java loves memory
                .timeout(TWENTY_SEC) // Class loading can take some time
                .environment(lambdaEnvMap)
                .build();
        System.out.println("Created BasicAuthorization lambda");
        return this;
    }

    public AuthorizationServiceStack outputStackVariables() {
        System.out.println("Output stack variables");
        CfnOutput.Builder.create(this, "BasicAuthFunctionArn")
                .exportName("BasicAuthFunctionArn")
                .value(basicAuthorizerHandler.getFunctionArn())
                .build();
        return this;
    }

    public AuthorizationServiceStack loadEnvVariables() {
        System.out.println("Load env variables");
        Dotenv dotenv = Dotenv.configure()
                .filename(".env.prod")
                .load();
        dotenv.entries(Dotenv.Filter.DECLARED_IN_ENV_FILE)
                .forEach(e -> lambdaEnvMap.put(e.getKey(), e.getValue()));
        return this;
    }
}
