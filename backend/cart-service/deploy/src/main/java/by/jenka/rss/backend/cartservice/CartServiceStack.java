package by.jenka.rss.backend.cartservice;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.events.targets.ApiGateway;
import software.amazon.awscdk.services.lambda.AssetCode;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.triggers.TriggerFunction;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CartServiceStack extends Stack {

    private static final @NotNull Duration TWENTY_SECONDS = Duration.seconds(20);
    private static final Map<String, String> lambdaEnvMap = new HashMap<>(Map.of("ENV", "PROD"));
    private Function cartApiHandler;
    private TriggerFunction cartServiceDbChangelogHandler;
    private DataSource dataSource;
    private DatabaseInstance postgresInstance;
    private ISecurityGroup securityGroupPgWithInternalAccess;
    private IVpc defaultVpc;

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

        cartApiHandler = Function.Builder.create(this, "CartServiceApiHandler")
                .description("Created via java cdk. Task 8")
                .functionName("cartServiceApiHandler")
                .code(AssetCode.fromAsset("../lambda/dist"))
                .handler("main.handler")
                .runtime(Runtime.NODEJS_20_X)
                .memorySize(256)
                .timeout(TWENTY_SECONDS)
                .environment(lambdaEnvMap)
                .allowPublicSubnet(true)
                .securityGroups(List.of(securityGroupPgWithInternalAccess))
                .vpc(defaultVpc)
                .vpcSubnets(
                        SubnetSelection.builder()
                                .subnetFilters(List.of(
                                        SubnetFilter.availabilityZones(List.of("eu-north-1b", "eu-north-1a", "eu-north-1c")))
                                )
                                .build()
                )
//                .layers(List.of(nodeModules))
                .build();

        cartApiHandler.addToRolePolicy(Utils.ec2RolesStatement());
        return this;
    }

    public CartServiceStack createAndTriggerDbChangelogLambda() {
        System.out.println("Creating DB Changlelog lambda");

        cartServiceDbChangelogHandler = TriggerFunction.Builder.create(this, "CartServiceDbChangeLogHandler")
                .description("Created via java cdk. Task 8")
                .functionName("cartServiceDbChangeLogHandler")
                .code(AssetCode.fromAsset("../lambda-dbchangelog/build/libs/lambda-dbchangelog-all.jar"))
                .handler("by.jenka.rss.backend.cartservice.changelog.ChangelogHandler")
                .runtime(Runtime.JAVA_17)
                .memorySize(256)
                .timeout(TWENTY_SECONDS)
                .environment(lambdaEnvMap)
                .allowPublicSubnet(true)
                .securityGroups(List.of(securityGroupPgWithInternalAccess))
                .vpc(defaultVpc)
                .vpcSubnets(
                        SubnetSelection.builder()
                                .subnetFilters(List.of(
                                        SubnetFilter.availabilityZones(List.of("eu-north-1b", "eu-north-1a", "eu-north-1c")))
                                )
                                .build())
                .build();
        cartServiceDbChangelogHandler.executeAfter(postgresInstance);
        cartServiceDbChangelogHandler.addToRolePolicy(Utils.ec2RolesStatement());
        postgresInstance.grantConnect(cartServiceDbChangelogHandler, dataSource.getUser());
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

        Utils.addCorsOptions(root);
        Utils.addCorsOptions(other);

        System.out.println("Created Cart Service Api Gateway");
        return this;
    }

    public CartServiceStack initPostgresDb() {
        System.out.println("Creating Postgres Db");
        var id = "postgres-db-rss";
//                FIXME. Avoid passing a raw password and user.
        var credentials = Credentials.fromPassword(dataSource.getUser(), SecretValue.unsafePlainText(dataSource.getRawPassword()));

        var postgres15 = DatabaseInstanceEngine.postgres(
                PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_15)
                        .build());
//      TODO avoid this. Use SSL connection instead - https://stackoverflow.com/a/77586796
        var parameterGroup = ParameterGroup.Builder.create(this, "postgres-15-parameter-group")
                .name("rss-postgres-15-parameter-group")
                .description("Disable ssl encryption")
                .engine(postgres15)
                .removalPolicy(RemovalPolicy.DESTROY)
                .parameters(Map.of("rds.force_ssl", "0"))
                .build();
        postgresInstance = DatabaseInstance.Builder.create(this, id)
                .engine(postgres15)
                .instanceIdentifier(id + "-01")
                .databaseName(dataSource.getDbName())
                .credentials(credentials)
                .parameterGroup(parameterGroup)
                .removalPolicy(RemovalPolicy.DESTROY)
                .instanceType(
                        InstanceType.of(
                                InstanceClass.T3, InstanceSize.MICRO
                        ))
//                Storage
                .storageType(StorageType.GP2)
                .allocatedStorage(20)
                .multiAz(false) // default false
                .caCertificate(CaCertificate.RDS_CA_RSA2048_G1)
//                access
                .vpc(defaultVpc)
                .securityGroups(List.of(securityGroupPgWithInternalAccess))
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        .build())
                .publiclyAccessible(false)
                .build();
        lambdaEnvMap.put("PG_URL", postgresInstance.getDbInstanceEndpointAddress());
        System.out.println("Created DB");
        return this;
    }

    public CartServiceStack grantPermissions() {
        System.out.println("Grand permission");
        postgresInstance.grantConnect(cartApiHandler, dataSource.getUser());
        return this;
    }

    public CartServiceStack loadEnvVariables() {
        System.out.println("Load env variables");
        lambdaEnvMap.putAll(Utils.loadEnv());
        dataSource = new DataSource(lambdaEnvMap);
        System.out.println("Datasource: " + dataSource);
        return this;
    }

    public CartServiceStack outputVariables() {
        System.out.println("Output variables");
        CfnOutput.Builder.create(this, "CartServicePostgresEndpoint")
                .exportName("CartServicePostgresEndpoint")
                .value(postgresInstance.getDbInstanceEndpointAddress())
                .build();
        return this;
    }

    public CartServiceStack initVpcAndSecurityGroups() {
        defaultVpc = Vpc.fromLookup(this, "rss-rds-VPC", VpcLookupOptions.builder()
                .isDefault(true)
                .build());
        securityGroupPgWithInternalAccess = SecurityGroup.Builder.create(this, "rss-rds-sec-group")
                .vpc(defaultVpc)
                .allowAllOutbound(true)
                .securityGroupName("rss-rds-sec-group")
                .build();

        securityGroupPgWithInternalAccess.addIngressRule(Peer.anyIpv4(), Port.POSTGRES, "PostgresOnly via cdk");
        securityGroupPgWithInternalAccess.getConnections().allowInternally(Port.POSTGRES);
        return this;
    }

    @ToString
    @Getter
    static class DataSource {
        private final String dbName;
        private final String rawPassword;
        private final String user;
        private final int port;

        public DataSource(Map<String, String> env) {
            this.user = Objects.requireNonNull(env.get("PG_USER"));
            this.rawPassword = Objects.requireNonNull(env.get("PG_PASSWORD"));
            this.dbName = Objects.requireNonNull(env.get("PG_DBNAME"));
            this.port = Objects.requireNonNull(Integer.valueOf(env.get("PG_DBPORT")));
        }
    }
}
