package by.jenka.rss.backend.productservice.sdk.task2;

import by.jenka.rss.backend.productservice.config.AwsConfig;
import software.amazon.awssdk.policybuilder.iam.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.ListPoliciesResponse;
import software.amazon.awssdk.services.iam.model.Policy;

import java.util.List;


public class AwsIamClient {
    private final IamClient client;

    public AwsIamClient() {
        client = IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .build();
    }

    public IamPolicy buildPrivateAccessToS3FromCloudFrontPolicy(String s3arn, String cloudFrontArn) {
        var iamPolicyStatement = IamPolicy.builder()
                .id(AwsConfig.IAM_S3_POLICY_ID)
                .addStatement(
                        IamStatement.builder()
                                .sid("AllowCloudFrontServicePrincipal")
                                .addPrincipal("Service", "cloudfront.amazonaws.com")
                                .effect(IamEffect.ALLOW)
                                .addAction("s3:GetObject")
                                .addResource(s3arn + "/*")
                                .addCondition(
                                        IamCondition.create(
                                                IamConditionOperator.STRING_EQUALS,
                                                IamConditionKey.create("AWS:SourceArn"),
                                                cloudFrontArn
                                        ))
                                .build()
                )
                .build();
        System.out.println("S3 Policy : " + iamPolicyStatement.toJson());
        return iamPolicyStatement;
    }


    public List<Policy> getPolicies() {
        ListPoliciesResponse listPoliciesResponse = client.listPolicies();
        System.out.println("Policies " + listPoliciesResponse);
        return listPoliciesResponse.policies();
    }
}
