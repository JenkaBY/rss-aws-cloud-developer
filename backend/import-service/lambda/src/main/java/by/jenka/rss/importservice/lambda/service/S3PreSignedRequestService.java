package by.jenka.rss.importservice.lambda.service;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

public class S3PreSignedRequestService {

    public PresignedPutObjectRequest preSignedPutRequest(String bucketName, String keyName, String contentType) {
        try (var preSigner = S3Presigner.builder().build()) {
            var objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(contentType)
                    .build();

            var preSignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();


            var preSignedRequest = preSigner.presignPutObject(preSignRequest);
            System.out.println("PreSigned PUT url :" + preSignedRequest);
            return preSignedRequest;
        }
    }
}
