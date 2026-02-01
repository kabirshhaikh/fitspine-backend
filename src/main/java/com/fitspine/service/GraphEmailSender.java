package com.fitspine.service;

import com.fitspine.email.MicrosoftGraphTokenClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GraphEmailSender {

    private final MicrosoftGraphTokenClient tokenClient;
    private final WebClient webClient = WebClient.create();

    @Value("${ms.graph.sender-email}")
    private String senderEmail;

    public GraphEmailSender(MicrosoftGraphTokenClient tokenClient) {
        this.tokenClient = tokenClient;
    }

    public void sendPlainTextEmail(String to, String subject, String content) {
        String accessToken = tokenClient.getAccessToken();

        webClient.post()
                .uri("https://graph.microsoft.com/v1.0/users/" + senderEmail + "/sendMail")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildPayload(to, subject, content))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private Map<String, Object> buildPayload(String to, String subject, String content) {
        return Map.of(
                "message", Map.of(
                        "subject", subject,
                        "body", Map.of(
                                "contentType", "Text",
                                "content", content
                        ),
                        "toRecipients", List.of(
                                Map.of(
                                        "emailAddress", Map.of(
                                                "address", to
                                        )
                                )
                        )
                ),
                "saveToSentItems", true
        );
    }
}