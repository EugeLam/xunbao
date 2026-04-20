package com.shop.shop.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutBucketPolicyRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

    @Value("${oss.endpoint}")
    private String endpoint;

    @Value("${oss.access-key}")
    private String accessKey;

    @Value("${oss.secret-key}")
    private String secretKey;

    @Value("${oss.bucket}")
    private String bucket;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        S3ClientBuilder builder = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.US_EAST_1);

        builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)));

        this.s3Client = builder.build();

        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket {} exists", bucket);
            setBucketPublicRead();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.info("Bucket {} not found, creating...", bucket);
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                    log.info("Bucket {} created successfully", bucket);
                    setBucketPublicRead();
                } catch (Exception ex) {
                    log.error("Failed to create bucket {}: {}", bucket, ex.getMessage());
                }
            } else {
                log.warn("Error checking bucket {}: {}", bucket, e.getMessage());
            }
        }
    }

    private void setBucketPublicRead() {
        try {
            String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Sid": "PublicReadGetObject",
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": "s3:GetObject",
                            "Resource": "arn:aws:s3:::%s/*"
                        }
                    ]
                }
                """, bucket);

            s3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(bucket)
                    .policy(policy)
                    .build());
            log.info("Bucket {} policy set to public read", bucket);
        } catch (Exception e) {
            log.warn("Failed to set bucket {} policy: {}", bucket, e.getMessage());
        }
    }

    public String uploadFile(String objectKey, MultipartFile file) throws IOException {
        ensureBucketExists();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(file.getContentType())
                .build();

        try (InputStream input = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(input, file.getSize()));
        }

        return getFileUrl(objectKey);
    }

    public String getFileUrl(String objectKey) {
        return endpoint + "/" + bucket + "/" + objectKey;
    }
}
