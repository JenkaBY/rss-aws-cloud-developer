package by.jenka.rss.task2;

import by.jenka.rss.config.AwsConfig;
import by.jenka.rss.verification.ClasspathResourceChecker;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class S3BucketClient {
    private final S3Client s3;
    private final S3BucketClient.S3Uploader s3Uploader;

    public S3BucketClient() {
        var region = AwsConfig.DEFAULT_REGION;
        s3 = S3Client.builder()
                .region(region)
                .build();
        s3Uploader = new S3Uploader();
    }

    public DeleteBucketResponse deleteBucket(String bucketName) {
        var response = s3.deleteBucket(toDeleteBucketRequest(bucketName));
        System.out.println("Delete bucket " + bucketName + ":" + response);
        return response;
    }

    public CreateBucketResponse createBucket(String bucketName) {
        var response = s3.createBucket(CreateBucketRequest.builder()
                .bucket(bucketName)
                .build());
        System.out.println("Create bucket " + bucketName + ":" + response);
        return response;
    }

    public Optional<Bucket> getBucket(String bucketName) {
        return getBuckets().stream()
                .filter(bucket -> bucketName.equals(bucket.name()))
                .findFirst();
    }

    public void uploadFiles(String bucket, String directoryPath) {
        File sourceDirectory = Paths.get(directoryPath).toFile();
        System.out.println("Source Dir: " + sourceDirectory);
        s3Uploader.uploadDirectory(bucket, sourceDirectory.toURI());
    }

    public void deleteAllFiles(String bucketName) {
        ListObjectsResponse objectListing = s3.listObjects(ListObjectsRequest.builder()
                .bucket(bucketName)
                .build());

        Iterator<S3Object> objIter = objectListing.contents().iterator();
        List<ObjectIdentifier> toDelete = new ArrayList<>();
        while (objIter.hasNext()) {
            var next = objIter.next();
            toDelete.add(ObjectIdentifier.builder()
                    .key(next.key())
                    .build());
        }
        System.out.println("To delete: " + toDelete);
        if (toDelete.isEmpty()) {
            return;
        }
        s3.deleteObjects(DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder()
                        .objects(toDelete)
                        .build())
                .build());
    }

    public void updatePolicy(String bucketName, String policyJson) {
        var response = s3.putBucketPolicy(PutBucketPolicyRequest.builder()
                .bucket(bucketName)
                .policy(policyJson)
                .build());
        System.out.println("Policy updated " + response);
    }

    public String getBucketPolicy(String bucketName) {
        GetBucketPolicyResponse bucketPolicy = s3.getBucketPolicy(GetBucketPolicyRequest.builder()
                .bucket(bucketName)
                .build());
        System.out.println("Bucket Policy: " + bucketPolicy.policy());
        return bucketPolicy.policy();
    }

    public List<Bucket> getBuckets() {
        try {
            ListBucketsResponse response = s3.listBuckets();
            System.out.println("S3 response " + response);
            return response.buckets();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            throw new RuntimeException(e);
        }
    }

    private DeleteBucketRequest toDeleteBucketRequest(String bucketName) {
        return DeleteBucketRequest.builder()
                .bucket(bucketName)
                .build();
    }

    public static class S3Uploader {
        private final S3TransferManager transferManager;

        public S3Uploader() {
            transferManager = S3TransferManager
                    .create();
        }

        public Integer uploadDirectory(String bucketName, URI sourceDirectory) {
            var uploadDirectoryRequest = UploadDirectoryRequest.builder()
                    .source(Paths.get(sourceDirectory))
                    .bucket(bucketName)
                    .build();
            System.out.println("Upload dir request " + uploadDirectoryRequest);
            DirectoryUpload directoryUpload = transferManager.uploadDirectory(uploadDirectoryRequest);

            CompletedDirectoryUpload completedDirectoryUpload = directoryUpload.completionFuture().join();
            completedDirectoryUpload.failedTransfers()
                    .forEach(fail -> System.out.println("Object [%s] failed to transfer".formatted(fail.toString())));
            return completedDirectoryUpload.failedTransfers().size();
        }
    }
}
