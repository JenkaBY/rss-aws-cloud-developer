package by.jenka.rss.backend.productservice.config;

import software.amazon.awssdk.regions.Region;

public class AwsConfig {

    public static Region DEFAULT_REGION = Region.EU_NORTH_1;
    public static String IAM_S3_POLICY_ID = "allowAccessToS3OnlyFromCloudFront";
    public static String CLOUDFRONT_OAC_POLICY_ID = "CloudFrontAccessToS3OnlyFromCloudFront";

}
