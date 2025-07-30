package com.fitspine.service;

public interface WearableService {
    String buildAuthUrl (Long userId);
    void exchangeCodeForToken(String code, Long userId);
}
