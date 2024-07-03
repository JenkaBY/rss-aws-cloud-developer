package by.jenka.rss.backend.productservice.sdk.task2;

import by.jenka.rss.backend.productservice.config.AwsConfig;
import by.jenka.rss.backend.productservice.sdk.task2.distribution.DistributionDto;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class CloudfrontClient {

    private final CloudFrontClient client;

    public CloudfrontClient() {
        client = CloudFrontClient.builder()
                .region(AwsConfig.DEFAULT_REGION)
                .build();
    }


    public List<DistributionSummary> getDistributions() {
        var response = client.listDistributions();
        System.out.println("Distributions list response: " + response);
        return response.distributionList().items();
    }

    public Optional<DistributionSummary> getDistribution(String originDomain) {
        return getDistributions().stream()
                .filter(d -> d.enabled() && d.origins().items().stream()
                        .anyMatch(i -> i.domainName().equals(originDomain)))
                .findFirst();
    }

    public Distribution updateDistributionOac(DistributionDto distribution, String originDomain, String oacId) {
        var dist = client.getDistribution(
                GetDistributionRequest.builder()
                        .id(distribution.id())
                        .build()
        );
        var config = dist.distribution().distributionConfig();
        DistributionConfig updatedConfig = DistributionConfig.builder()
                .aliases(config.aliases())
                .comment(config.comment())
                .cacheBehaviors(config.cacheBehaviors())
                .priceClass(config.priceClass())
                .defaultCacheBehavior(config.defaultCacheBehavior())
                .enabled(config.enabled())
                .callerReference(config.callerReference())
                .logging(config.logging())
                .originGroups(config.originGroups())
                .origins(Origins.builder()
                        .quantity(1)
                        .items(Origin.builder()
                                .id(originDomain)
                                .originPath(config.origins().items().iterator().next().originPath())
                                .customHeaders(config.origins().items().iterator().next().customHeaders())
                                .domainName(originDomain)
                                .s3OriginConfig(
                                        S3OriginConfig.builder()
                                                .originAccessIdentity("")
                                                .build())
                                .originAccessControlId(oacId)
                                .build())
                        .build())
                .restrictions(config.restrictions())
                .defaultRootObject(config.defaultRootObject())
                .webACLId(config.webACLId())
                .httpVersion(config.httpVersion())
                .viewerCertificate(config.viewerCertificate())
                .customErrorResponses(config.customErrorResponses())
                .build();

        UpdateDistributionResponse updateDistributionResponse = client.updateDistribution(UpdateDistributionRequest.builder()
                .id(distribution.id())
                .distributionConfig(updatedConfig)
                .ifMatch(dist.eTag())
                .build());
        System.out.println("Updated distribution " + updateDistributionResponse);
        return updateDistributionResponse.distribution();
    }

    public String createOrGetOAC() {
        var oacs = client.listOriginAccessControls(ListOriginAccessControlsRequest.builder()
                .maxItems("50")
                .build());
        return oacs.originAccessControlList().items().stream()
                .filter(a -> AwsConfig.CLOUDFRONT_OAC_POLICY_ID.equals(a.name()))
                .map(OriginAccessControlSummary::id)
                .findFirst()
                .orElseGet(() -> {
                    var res = client.createOriginAccessControl(
                            CreateOriginAccessControlRequest.builder()
                                    .originAccessControlConfig(OriginAccessControlConfig.builder()
                                            .originAccessControlOriginType(OriginAccessControlOriginTypes.S3)
                                            .signingProtocol(OriginAccessControlSigningProtocols.SIGV4)
                                            .signingBehavior(OriginAccessControlSigningBehaviors.NO_OVERRIDE)
                                            .name(AwsConfig.CLOUDFRONT_OAC_POLICY_ID)
                                            .build())
                                    .build()
                    );
                    System.out.println("Create oac : " + res.originAccessControl());
                    return res.originAccessControl().id();
                });
    }

    public Invalidation createInvalidation(String distributionId) {
        CreateInvalidationResponse invalidation = client.createInvalidation(CreateInvalidationRequest.builder()
                .distributionId(distributionId)
                .invalidationBatch(InvalidationBatch.builder()
                        .callerReference(Instant.now() + "_WSL")
                        .paths(Paths.builder()
                                .items("/*")
                                .quantity(1)
                                .build())
                        .build())
                .build());
        System.out.println("Invalidation for distribution " + distributionId + " has been created " + invalidation.invalidation());
        return invalidation.invalidation();
    }

    public CreateDistributionResponse createDistribution(String originDomain) {
        var originId = originDomain;
        return client.createDistribution(
                CreateDistributionRequest.builder()
                        .distributionConfig(
                                getDistributionConfig(originDomain, originId, null)
                        )
                        .build()
        );
    }

    private static DistributionConfig getDistributionConfig(String originDomain, String originId, String oacId) {
        return DistributionConfig.builder()
                .origins(Origins.builder()
                        .quantity(1)
                        .items(Origin.builder()
                                .id(originId)
                                .domainName(originDomain)
                                .s3OriginConfig(
                                        S3OriginConfig.builder()
                                                .originAccessIdentity("")
                                                .build())
                                .originAccessControlId(oacId)
                                .build())
                        .build())
                .defaultCacheBehavior(DefaultCacheBehavior.builder()
                        .viewerProtocolPolicy(ViewerProtocolPolicy.ALLOW_ALL)
                        .targetOriginId(originId)
                        .minTTL(200L)
                        .forwardedValues(ForwardedValues.builder()
                                .cookies(CookiePreference.builder()
                                        .forward(ItemSelection.NONE)
                                        .build()
                                )
                                .queryString(true)
                                .build()
                        )
                        .allowedMethods(AllowedMethods.builder()
                                .quantity(2)
                                .items(Method.GET, Method.HEAD)
                                .cachedMethods(CachedMethods
                                        .builder()
                                        .quantity(2)
                                        .items(Method.GET, Method.HEAD)
                                        .build())
                                .build())
                        .build()
                )
                .enabled(true)
                .comment("Distribution built with java sdk-v2")
                .callerReference(Instant.now().toString())
                .defaultRootObject("index.html")
                .build();
    }
}
