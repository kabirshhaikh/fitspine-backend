package com.fitspine.service;

import com.fitspine.exception.GoogleAuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class GoogleTokenVerificationService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerificationService(
            @Value("${google.oauth.client-id}") String googleClientId
    ) {
        this.verifier =
                new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance()
                )
                        .setAudience(Collections.singletonList(googleClientId))
                        .build();
    }

    public GoogleIdToken.Payload verifyAndGetPayload(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new GoogleAuthException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new GoogleAuthException("Google email not verified");
            }

            log.info(
                    "Google payload: email={}, sub={}, aud={}",
                    payload.getEmail(),
                    payload.getSubject(),
                    payload.getAudience(),
                    payload.get("name")
                    );

            return payload;

        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new GoogleAuthException("Google token verification failed");
        }
    }
}
