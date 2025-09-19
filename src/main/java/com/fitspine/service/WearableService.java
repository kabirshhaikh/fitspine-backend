package com.fitspine.service;

public interface WearableService {
    String buildAuthUrl(String email);

    void exchangeCodeForToken(String code, Long userId);

    String getProvider();
}
