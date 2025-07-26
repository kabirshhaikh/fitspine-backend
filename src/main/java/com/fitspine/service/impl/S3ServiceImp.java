package com.fitspine.service.impl;

import com.fitspine.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

@Service
public class S3ServiceImp implements S3Service {
    @Value("${AWS_BUCKET_NAME}")
    private String bucket;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public S3ServiceImp(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            return path;
        } catch (IOException exception) {
            throw new RuntimeException("Failed to upload the file to S3: ", exception);
        }
    }

    @Override
    public byte[] downloadFile(String path) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    @Override
    public void deleteFile(String path) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        s3Client.deleteObject(request);
    }

    @Override
    public String generatePreSignedUrl(String path) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        GetObjectPresignRequest preSignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(request)
                .build();

        return s3Presigner.presignGetObject(preSignRequest).url().toString();
    }
}
