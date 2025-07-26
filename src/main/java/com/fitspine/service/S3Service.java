package com.fitspine.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String path);

    byte[] downloadFile(String path);

    void deleteFile(String path);

    String generatePreSignedUrl(String path);
}
