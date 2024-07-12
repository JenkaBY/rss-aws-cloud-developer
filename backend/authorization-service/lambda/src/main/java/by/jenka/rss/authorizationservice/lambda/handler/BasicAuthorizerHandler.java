package by.jenka.rss.authorizationservice.lambda.handler;

import by.jenka.rss.authorizationservice.lambda.model.AuthPolicy;
import by.jenka.rss.authorizationservice.lambda.service.UserService;
import by.jenka.rss.authorizationservice.lambda.utils.AuthorizationTokenExtractor;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayCustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import lombok.Setter;

@Setter
public class BasicAuthorizerHandler implements RequestHandler<APIGatewayCustomAuthorizerEvent, AuthPolicy> {

    private UserService userService = new UserService();

    @Override
    public AuthPolicy handleRequest(APIGatewayCustomAuthorizerEvent input, Context context) {
        var logger = context.getLogger();
        
        var requestContext = input.getRequestContext();
        logger.log("INPUT : " + input);
        logger.log("REQUEST_CONTEXT : " + requestContext);
        logger.log("route arn " + input.getMethodArn());
        logger.log("Headers " + input.getHeaders());


        var principalId = input.getRequestContext().getAccountId();
        String methodArn = input.getMethodArn();
        String[] arnPartials = methodArn.split(":");
        String region = arnPartials[3];
        String awsAccountId = arnPartials[4];
        String[] apiGatewayArnPartials = arnPartials[5].split("/");
        String restApiId = apiGatewayArnPartials[0];
        String stage = apiGatewayArnPartials[1];
        String httpMethod = apiGatewayArnPartials[2];
        String resource = ""; // root resource
        if (apiGatewayArnPartials.length == 4) {
            resource = apiGatewayArnPartials[3];
        }
        try {
            var basicToken = AuthorizationTokenExtractor.getBasicAuthorizationToken(input.getHeaders());
            var credentialsPartial = basicToken.split(":");
            logger.log("Token secrets = %s".formatted(basicToken));

            var name = credentialsPartial[0];
            var password = credentialsPartial[1];
            var isValidCredentials = userService.isValidCredentials(name, password);

            if (isValidCredentials) {
                logger.log("User '%s' is allowed to execute URI".formatted(name));
                return new AuthPolicy(principalId,
                        AuthPolicy.PolicyDocument.getAllowOnePolicy(
                                region,
                                awsAccountId,
                                restApiId,
                                stage,
                                AuthPolicy.HttpMethod.valueOf(httpMethod),
                                resource));
            }
            logger.log("User '%s' is denied to access".formatted(name));
            return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
        } catch (Throwable throwable) {
            logger.log("Exception occurred: " + throwable.getMessage(), LogLevel.ERROR);
            return new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
        }
    }
}
