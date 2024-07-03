package by.jenka.rss.importservice.lambda.config.bean;

import by.jenka.rss.importservice.lambda.config.AwsConfig;
import lombok.Getter;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Getter
public class S3Manager {

    private final S3Client s3;

    public S3Manager() {
        var region = AwsConfig.DEFAULT_REGION;
        s3 = S3Client.builder()
                .region(region)
                .build();
    }

    public String moveObject(String bucket, String sourceKey, String destinationKey) {
        System.out.printf("Move object %s/%s to %s/%s requested%n", bucket, sourceKey, bucket, destinationKey);
        copyObject(bucket, sourceKey, destinationKey);
        deleteObject(bucket, sourceKey);
        return "Ok";
    }

    public String copyObject(String bucket, String sourceKey, String destinationKey) {
        System.out.printf("Coping object %s:%s to %s:%s requested%n", bucket, sourceKey, bucket, destinationKey);
        CopyObjectResponse copyObjectResponse = s3.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(sourceKey)
                .destinationBucket(bucket)
                .destinationKey(destinationKey)
                .build());
        return copyObjectResponse.copyObjectResult().eTag();
    }

    public String deleteObject(String bucket, String sourceKey) {
        System.out.printf("Delete object %s:%s requested%n", bucket, sourceKey);

        var response = s3.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(sourceKey)
                .build());
        return response.toString();
    }
}
