package by.jenka.rss.backend.productservice.config;

import software.amazon.awssdk.regions.Region;

import java.util.Base64;
import java.util.List;

public class AwsConfig {

    private static final List<String> CREATE_PRODUCT_NOTIFICATION_EMAILS_BASE64 = List.of(
            "ZXZnZW4ua3V6bWljaEBnbWFpbC5jb20="
    );
    private static final List<String> CREATE_SPECIAL_PRODUCT_NOTIFICATION_EMAILS_BASE64 = List.of(
            "eWF1aGVuLmt1em1pY2hAZ21haWwuY29t"
    );
    public static Region DEFAULT_REGION = Region.EU_NORTH_1;
    public static String IAM_S3_POLICY_ID = "allowAccessToS3OnlyFromCloudFront";
    public static String CLOUDFRONT_OAC_POLICY_ID = "CloudFrontAccessToS3OnlyFromCloudFront";


    public static List<String> getCreateProductNotificationEmails() {
        return decode(CREATE_PRODUCT_NOTIFICATION_EMAILS_BASE64);
    }

    public static List<String> getCreateSpecialProductNotificationEmails() {
        return decode(CREATE_SPECIAL_PRODUCT_NOTIFICATION_EMAILS_BASE64);
    }

    private static List<String> decode(List<String> base64Values) {
        return base64Values.stream()
                .map(val -> Base64.getDecoder().decode(val))
                .map(String::new)
                .toList();
    }
}
