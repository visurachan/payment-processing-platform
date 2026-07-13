package com.payment_processing_platform.webhook_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class WebhookService {
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    public WebhookService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void send(String callbackUrl, Map<String, Object> payload) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                restClient.post()
                        .uri(callbackUrl)
                        .header("Content-Type", "application/json")
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();

                log.info("Webhook delivered to {} for payment {}",
                        callbackUrl, payload.get("paymentId"));
                return;

            } catch (Exception e) {
                log.warn("Webhook attempt {}/{} failed for {} — {}",
                        attempt, maxAttempts, callbackUrl, e.getMessage());

                if (attempt < maxAttempts) {
                    try { Thread.sleep(1000L * attempt); }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }

        log.error("Webhook delivery failed after {} attempts for payment {}",
                maxAttempts, payload.get("paymentId"));
    }

}
