package by.jenka.rss.backend.productservice.sdk.task2;


import by.jenka.rss.backend.productservice.config.AwsConfig;
import by.jenka.rss.backend.productservice.sdk.task2.distribution.DistributionDto;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Optional;

public class Task2 {

    private static final String BUCKET_NAME = "rss-task-2-created-from-cdk";
    private static final String WEB_APP_RESOURCES = "./build/resources/main/static";
    private final S3BucketClient s3Client;
    private final CloudfrontClient cloudfrontClient;
    private final AwsIamClient iamClient;


    public Task2() {
        s3Client = new S3BucketClient();
        cloudfrontClient = new CloudfrontClient();
        iamClient = new AwsIamClient();
    }


    public void run() {
        s3Actions();
        var distribution = cloudFrontActions();
        // Update S3 bucket policies
        var bucketArn = buildS3Arn(BUCKET_NAME);
        var bucketPolicy = iamClient.buildPrivateAccessToS3FromCloudFrontPolicy(bucketArn, distribution.arn());

        s3Client.updatePolicy(BUCKET_NAME, bucketPolicy.toJson());
        var bucketPolicyResponse =  s3Client.getBucketPolicy(BUCKET_NAME);
        var originDomain = buildOriginDomain(BUCKET_NAME, AwsConfig.DEFAULT_REGION.id());

        String oacId = cloudfrontClient.createOrGetOAC();
        System.out.println("OAC id is " + oacId);
        cloudfrontClient.updateDistributionOac(distribution, originDomain, oacId);
        cloudfrontClient.createInvalidation(distribution.id());
        System.out.println("The SPA is available here - https://" + distribution.domainName());
    }


    private DistributionDto cloudFrontActions() {
        var originDomain = buildOriginDomain(BUCKET_NAME, AwsConfig.DEFAULT_REGION.id());
        System.out.println("Origin Domain: " + originDomain);


        var distribution = cloudfrontClient.getDistribution(originDomain)
                .map(d -> new DistributionDto(d.id(), d.arn(), d.domainName()))
                .orElseGet(
                        () -> Optional.of(cloudfrontClient.createDistribution(originDomain).distribution())
                                .map(d -> new DistributionDto(d.id(), d.arn(), d.domainName())).get());
        System.out.println("Distribution: " + distribution);


        // get all distributions
        cloudfrontClient.getDistributions().stream()
                .peek(dist -> System.out.println("\tid: " + dist.id() + "\n\tarn: " + dist.arn() + "\n\tdomainName: " + dist.domainName() + "\n-------"))
                .forEach(System.out::println);
        return distribution;

    }

    private String buildOriginDomain(String bucketName, String region) {
        return "%s.s3.%s.amazonaws.com".formatted(bucketName, region);
    }

    public String buildS3Arn(String bucketName) {
        return "arn:aws:s3:::%s".formatted(bucketName);
    }

    private void s3Actions() {
        s3Client.getBucket(BUCKET_NAME)
                .map(Bucket::name)
                .map(name -> {
                    System.out.println("Bucket already exists: " + name);
                    return name;
                })
                .orElseGet(() -> {
                    var location = s3Client.createBucket(BUCKET_NAME).location();
                    System.out.println("Bucket '%s' has been created".formatted(location));
                    return location;
                });

        s3Client.deleteAllFiles(BUCKET_NAME);
        s3Client.uploadFiles(BUCKET_NAME, WEB_APP_RESOURCES);
    }

    private void listBuckets() {
        try {
            s3Client.getBuckets().forEach(bucket -> {
                System.out.println("Bucket: " + bucket.name());
            });

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
        }
    }
}
