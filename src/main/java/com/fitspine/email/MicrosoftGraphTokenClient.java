package com.fitspine.email;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MicrosoftGraphTokenClient {

    @Value("${ms.graph.tenant-id}")
    private String tenantId;

    @Value("${ms.graph.client-id}")
    private String clientId;

    @Value("${ms.graph.client-secret}")
    private String clientSecret;

    private final WebClient webClient = WebClient.create();

    public String getAccessToken() {
        return webClient.post()
                .uri("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(
                        "client_id=" + clientId +
                                "&scope=https://graph.microsoft.com/.default" +
                                "&client_secret=" + clientSecret +
                                "&grant_type=client_credentials"
                )
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block()
                .get("access_token")
                .asText();
    }
}
